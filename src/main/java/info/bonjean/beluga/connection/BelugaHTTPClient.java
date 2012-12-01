/* Copyright (C) 2012 Julien Bonjean <julien@bonjean.info>
 * 
 * This file is part of Beluga.
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
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.gui.UI;

import java.io.InputStream;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class BelugaHTTPClient
{
	private static final int CONNECTION_TIMEOUT = 4000;
	private static final int SOCKET_TIMEOUT = 4000;
	private static final int MAX_RETRIES = 3;
	
	private HttpClient client;
	private static BelugaHTTPClient instance;

	private BelugaHTTPClient()
	{
		BelugaConfiguration configuration = BelugaConfiguration.getInstance();

		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);
		
		client = new DefaultHttpClient(httpParameters);
		if (!configuration.getDNSProxy().isEmpty())
		{
			BelugaDNSResolver dnsOverrider = new BelugaDNSResolver("tuner.pandora.com", configuration.getDNSProxy());
			client = new DefaultHttpClient(new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault(), dnsOverrider), httpParameters);
			
		} else if (!configuration.getProxyHost().isEmpty())
			ConnRouteParams.setDefaultProxy(client.getParams(), new HttpHost(configuration.getProxyHost(), configuration.getProxyPort(), "http"));
	}

	public static BelugaHTTPClient getInstance()
	{
		if (instance == null)
			instance = new BelugaHTTPClient();
		return instance;
	}

	public static void reset()
	{
		instance = null;
	}

	public InputStream httpRequest(HttpUriRequest request) throws CommunicationException
	{
		Exception e = null;
		for(int i = 0 ; i < MAX_RETRIES ; i++)
		{
			try
			{
				HttpResponse httpResponse = client.execute(request);
				return httpResponse.getEntity().getContent();
			} catch (Exception e1)
			{
				e = e1;
				UI.reportError("connection.problem");
			}
		}
		throw new CommunicationException(e);
	}
}
