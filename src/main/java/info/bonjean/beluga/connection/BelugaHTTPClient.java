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

import java.io.InputStream;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class BelugaHTTPClient
{
	private HttpClient client;
	private static BelugaHTTPClient instance;

	private BelugaHTTPClient()
	{
		BelugaConfiguration configuration = BelugaConfiguration.getInstance();
		client = new DefaultHttpClient();
		if (!configuration.getDNSProxy().isEmpty())
		{
			BelugaDNSResolver dnsOverrider = new BelugaDNSResolver("tuner.pandora.com", configuration.getDNSProxy());
			client = new DefaultHttpClient(new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault(), dnsOverrider));
			
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
		try
		{
			HttpResponse httpResponse = client.execute(request);
			return httpResponse.getEntity().getContent();
		} catch (Exception e)
		{
			throw new CommunicationException(e);
		}
	}
}
