/*
 * Copyright (C) 2012, 2013, 2014 Julien Bonjean <julien@bonjean.info>
 *
 * This file is part of Beluga Player.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package info.bonjean.beluga.connection;

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.configuration.DNSProxy;
import info.bonjean.beluga.exception.CommunicationException;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpInetConnection;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
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
	private static final int TIMEOUT = 4000;
	private static BelugaHTTPClient instance;

	private HttpClient httpClient;
	private PoolingHttpClientConnectionManager connectionManager;

	private BelugaHTTPClient()
	{
		BelugaConfiguration configuration = BelugaConfiguration.getInstance();
		HttpClientBuilder clientBuilder = HttpClients.custom();

		// timeout
		RequestConfig config = RequestConfig.custom().setConnectTimeout(TIMEOUT)
				.setSocketTimeout(TIMEOUT).setConnectionRequestTimeout(TIMEOUT).build();
		clientBuilder.setDefaultRequestConfig(config);

		switch (configuration.getConnectionType())
		{
			case DIRECT:
				connectionManager = new PoolingHttpClientConnectionManager();
				break;
			case HTTP_PROXY:
				HttpHost proxy = new HttpHost(configuration.getProxyHost(),
						configuration.getProxyPort(), "http");
				clientBuilder.setProxy(proxy);
			default:
				Registry<ConnectionSocketFactory> registry = RegistryBuilder
						.<ConnectionSocketFactory> create()
						.register("http", PlainConnectionSocketFactory.getSocketFactory())
						.register("https", SSLConnectionSocketFactory.getSocketFactory()).build();
				BelugaDNSResolver dnsOverrider = new BelugaDNSResolver(DNSProxy.get(configuration
						.getConnectionType().getId()));
				connectionManager = new PoolingHttpClientConnectionManager(registry, dnsOverrider);
				break;
		}

		// limit the pool size
		connectionManager.setDefaultMaxPerRoute(2);

		// add interceptor, currently for debugging only
		clientBuilder.addInterceptorFirst(new HttpResponseInterceptor()
		{
			@Override
			public void process(HttpResponse response, HttpContext context) throws HttpException,
					IOException
			{
				HttpInetConnection connection = (HttpInetConnection) context
						.getAttribute(HttpCoreContext.HTTP_CONNECTION);
				log.debug("Remote address: " + connection.getRemoteAddress());
				// TODO: reimplement blacklisting for DNS proxy by maintaining a
				// map [DNS IP,RESOLVED IP] in the DNS resolver for reverse
				// lookup
			}
		});

		// finally create the HTTP client
		clientBuilder.setConnectionManager(connectionManager);
		httpClient = clientBuilder.build();
	}

	public static BelugaHTTPClient getInstance()
	{
		if (instance == null)
			instance = new BelugaHTTPClient();
		return instance;
	}

	public static void reset()
	{
		if (instance != null)
		{
			instance.connectionManager.close();
			instance = null;
		}
	}

	public String requestPost(HttpPost post) throws CommunicationException,
			ClientProtocolException, IOException
	{
		HttpResponse httpResponse = httpClient.execute(post);
		String result = IOUtils.toString(httpResponse.getEntity().getContent());
		EntityUtils.consume(httpResponse.getEntity());
		return result;
	}

	public byte[] requestGet(HttpGet get) throws ClientProtocolException, IOException
	{
		HttpResponse httpResponse = httpClient.execute(get);
		byte[] result = IOUtils.toByteArray(httpResponse.getEntity().getContent());
		EntityUtils.consume(httpResponse.getEntity());
		return result;
	}

	public HttpResponse requestGetStream(HttpGet get) throws ClientProtocolException, IOException
	{
		// TODO: use a different client with BasicHttpClientConnectionManager
		// for the streaming connection

		// no socket timeout, this may improve pause support
		// TODO: check if it is working as expected (also depends of the server)
		RequestConfig config = RequestConfig.custom().setConnectTimeout(TIMEOUT)
				.setConnectionRequestTimeout(TIMEOUT).build();
		get.setConfig(config);
		HttpResponse httpResponse = httpClient.execute(get);
		if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
			throw new IOException("Server reply: " + httpResponse.getStatusLine().getReasonPhrase());
		return httpResponse;
	}
}
