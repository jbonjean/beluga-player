/* Copyright (C) 2012 Julien Bonjean <julien@bonjean.info>
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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
package info.bonjean.beluga.util;

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.log.Logger;
import info.bonjean.beluga.request.JsonData;
import info.bonjean.beluga.request.Method;
import info.bonjean.beluga.request.ParameterMap;
import info.bonjean.beluga.response.Response;
import info.bonjean.beluga.response.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class HTTPUtil
{
	private static Logger log = new Logger(HTTPUtil.class);

	private static final Gson gson = GsonUtil.getGsonInstance();
	private static final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private static final String SERVICE_URL = "http://tuner.pandora.com/services/json/?";

	public static Result request(Method method, ParameterMap params, JsonData jsonData, boolean encrypt) throws ClientProtocolException, URISyntaxException, IOException
	{
		String urlStr = createRequestUrl(method, params);
		String data = gson.toJson(jsonData);

		log.info("===============================================");
		log.info("Request: " + urlStr);
		log.info("Data: " + data);

		if (encrypt)
			data = CryptoUtil.pandoraEncrypt(data);
		Response response;
		String requestResponse = HTTPUtil.request(urlStr, data);
		log.info("Response: " + requestResponse);
		response = gson.fromJson(requestResponse, Response.class);

		return response.getResult();
	}

	private static String createRequestUrl(Method method, ParameterMap params) throws UnsupportedEncodingException
	{
		StringBuffer url = new StringBuffer(SERVICE_URL);
		if (params == null)
			params = new ParameterMap();
		params.add("method", method.getName());
		url.append(URLEncodedUtils.format(params.getNameValuePairList(), "UTF-8"));
		return url.toString();
	}

	private static String request(String urlStr, String data) throws URISyntaxException, ClientProtocolException, IOException
	{
		URI url = new URI(urlStr);
		HttpClient client = new DefaultHttpClient();
		if (!configuration.getProxyServer().isEmpty())
			ConnRouteParams.setDefaultProxy(client.getParams(), new HttpHost(configuration.getProxyServer(), configuration.getProxyServerPort(), "http"));
		StringEntity json = new StringEntity(data.toString());
		json.setContentType("application/json");
		HttpPost post = new HttpPost(url);
		post.addHeader("Content-Type", "application/json");
		post.setEntity(json);
		HttpResponse httpResponse = client.execute(post);
		InputStream in = httpResponse.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		String line = null;
		StringBuffer sb = new StringBuffer();
		while ((line = reader.readLine()) != null)
			sb.append(line);

		return sb.toString();
	}

	public static Map<String, String> parseUrl(String url)
	{
		Map<String, String> parametersMap = new HashMap<String, String>();
		String parameters = url.substring(url.indexOf("?") + 1);

		StringTokenizer paramGroup = new StringTokenizer(parameters, "&");

		while (paramGroup.hasMoreTokens())
		{
			StringTokenizer value = new StringTokenizer(paramGroup.nextToken(), "=");
			parametersMap.put(value.nextToken(), value.nextToken());
		}

		return parametersMap;
	}
}
