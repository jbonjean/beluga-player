/*
 * Copyright (C) 2012-2021 Julien Bonjean <julien@bonjean.info>
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
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
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
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BelugaHTTPClient {
	public static final BelugaHTTPClient PANDORA_API_INSTANCE = new BelugaHTTPClient(4000, 4000, 4000, 2);
	public static final BelugaHTTPClient AUDIO_STREAM_INSTANCE = new BelugaHTTPClient(4000, 4000, 4000, 2);

	private HttpClient httpClient;
	private PoolingHttpClientConnectionManager connectionManager;
	private final int connectTimeout;
	private final int socketTimeout;
	private final int connectionRequestTimeout;
	private final int poolSize;

	public BelugaHTTPClient(int connectTimeout, int socketTimeout, int connectionRequestTimeout, int poolSize) {
		this.connectTimeout = connectTimeout;
		this.socketTimeout = socketTimeout;
		this.connectionRequestTimeout = connectionRequestTimeout;
		this.poolSize = poolSize;
		init();
	}

	private void init() {
		HttpClientBuilder clientBuilder = HttpClients.custom();

		RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout)
				.setConnectionRequestTimeout(connectionRequestTimeout).build();
		clientBuilder.setDefaultRequestConfig(config);

		switch (BelugaConfiguration.getInstance().getConnectionType()) {
		case DIRECT:
			connectionManager = new PoolingHttpClientConnectionManager();
			break;

		case HTTP_PROXY:
			HttpHost proxy = new HttpHost(BelugaConfiguration.getInstance().getHTTPProxyHost(),
					BelugaConfiguration.getInstance().getHTTPProxyPort(), "http");
			clientBuilder.setProxy(proxy);
			connectionManager = new PoolingHttpClientConnectionManager();
			break;

		case SOCKS5_PROXY:
			String socks5Host = BelugaConfiguration.getInstance().getSocks5ProxyHost();
			int socks5Port = BelugaConfiguration.getInstance().getSocks5ProxyPort();
			Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory> create()
					.register("http", new Socks5PlainConnectionSocketFactory(socks5Host, socks5Port))
					.register("https", new Socks5SSLConnectionSocketFactory(socks5Host, socks5Port)).build();
			connectionManager = new PoolingHttpClientConnectionManager(reg, new NoopDNSResolver());
			break;

		default:
			Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
					.register("http", PlainConnectionSocketFactory.getSocketFactory())
					.register("https", SSLConnectionSocketFactory.getSocketFactory()).build();
			BelugaDNSResolver dnsOverrider = new BelugaDNSResolver(
					DNSProxy.get(BelugaConfiguration.getInstance().getConnectionType().getId()));
			connectionManager = new PoolingHttpClientConnectionManager(registry, dnsOverrider);
			break;
		}

		connectionManager.setDefaultMaxPerRoute(poolSize);

		// finally create the HTTP client
		clientBuilder.setConnectionManager(connectionManager);
		httpClient = clientBuilder.build();
	}

	public void close() {
		if (connectionManager != null)
			connectionManager.close();
	}

	public void reset() {
		if (connectionManager != null)
			connectionManager.close();
		init();
	}

	public String post(HttpPost post) throws CommunicationException, ClientProtocolException, IOException {
		HttpResponse httpResponse = httpClient.execute(post);
		String result = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
		EntityUtils.consume(httpResponse.getEntity());
		return result;
	}

	public byte[] get(HttpGet get) throws ClientProtocolException, IOException {
		HttpResponse httpResponse = httpClient.execute(get);
		byte[] result = IOUtils.toByteArray(httpResponse.getEntity().getContent());
		EntityUtils.consume(httpResponse.getEntity());
		return result;
	}

	public HttpResponse getStream(HttpGet get) throws ClientProtocolException, IOException {
		HttpResponse httpResponse = httpClient.execute(get);
		if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
			throw new IOException("Server reply: " + httpResponse.getStatusLine().getReasonPhrase());
		return httpResponse;
	}
}
