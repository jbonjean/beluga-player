/* Copyright (C) 2012, 2013 Julien Bonjean <julien@bonjean.info>
 * 
 * This file is part of Beluga Player.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.bonjean.beluga.connection;

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.configuration.DNSProxy;
import info.bonjean.beluga.exception.CommunicationException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.Socket;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.EofSensorInputStream;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.io.ContentLengthInputStream;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class BelugaHTTPClient
{
	private static final Logger log = LoggerFactory.getLogger(BelugaHTTPClient.class);
	private static final int CONNECTION_TIMEOUT = 4000;
	private static final int SOCKET_TIMEOUT = 10000;
	private static final int MAX_RETRIES = 2;

	private HttpClient client;
	private BelugaDNSResolver dnsOverrider;
	private static BelugaHTTPClient instance;

	private BelugaHTTPClient()
	{
		BelugaConfiguration configuration = BelugaConfiguration.getInstance();

		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);

		PoolingClientConnectionManager poolingClientConnectionManager = null;
		if (configuration.getDNSProxy().isEmpty())
			poolingClientConnectionManager = new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault());
		else
		{
			DNSProxy dnsProxy = DNSProxy.get(configuration.getDNSProxy());
			dnsOverrider = new BelugaDNSResolver(dnsProxy);
			poolingClientConnectionManager = new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault(), dnsOverrider);
		}
		client = new DefaultHttpClient(poolingClientConnectionManager, httpParameters);
		if (configuration.getDNSProxy().isEmpty() && !configuration.getProxyHost().isEmpty())
			ConnRouteParams.setDefaultProxy(client.getParams(), new HttpHost(configuration.getProxyHost(), configuration.getProxyPort(), "http"));
	}

	public static BelugaHTTPClient getInstance()
	{
		if (instance == null)
			instance = new BelugaHTTPClient();
		return instance;
	}

	public HttpClient getClient()
	{
		return client;
	}

	public static void reset()
	{
		instance = null;
	}

	public InputStream httpRequest(HttpUriRequest request) throws CommunicationException
	{
		Exception e = null;
		for (int i = 0; i < MAX_RETRIES; i++)
		{
			try
			{
				HttpResponse httpResponse = client.execute(request);
				return httpResponse.getEntity().getContent();
			}
			catch (Exception e1)
			{
				e = e1;
				log.error("connectionProblem");
			}
		}
		throw new CommunicationException("communicationProblem", e);
	}

	public void blacklist(HttpResponse httpResponse) throws NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, IllegalStateException, IOException
	{
		// this piece of code is not very reliable (reflection) but not critical
		EofSensorInputStream eofSensorInputStream = (EofSensorInputStream) httpResponse.getEntity().getContent();

		Field wrappedStreamField = eofSensorInputStream.getClass().getDeclaredField("wrappedStream");
		wrappedStreamField.setAccessible(true);
		ContentLengthInputStream contentLengthInputStream = (ContentLengthInputStream) wrappedStreamField.get(eofSensorInputStream);

		Field inField = contentLengthInputStream.getClass().getDeclaredField("in");
		inField.setAccessible(true);
		SessionInputBuffer sessionInputBuffer = (SessionInputBuffer) inField.get(contentLengthInputStream);

		Field socketField = sessionInputBuffer.getClass().getDeclaredField("socket");
		socketField.setAccessible(true);
		Socket socket = (Socket) socketField.get(sessionInputBuffer);

		// blacklist address in the DNS resolver
		dnsOverrider.blacklistAddress(socket.getInetAddress());
	}
}
