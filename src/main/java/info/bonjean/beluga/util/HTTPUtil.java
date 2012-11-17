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

import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.exception.PandoraException;
import info.bonjean.beluga.log.Logger;
import info.bonjean.beluga.request.BelugaHTTPClient;
import info.bonjean.beluga.request.JsonData;
import info.bonjean.beluga.request.Method;
import info.bonjean.beluga.request.ParameterMap;
import info.bonjean.beluga.response.Response;
import info.bonjean.beluga.response.Result;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;

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
	private static final String SERVICE_URL = "http://tuner.pandora.com/services/json/?";

	public static Result request(Method method, ParameterMap params, JsonData jsonData, boolean encrypt) throws BelugaException
	{
		String urlStr = createRequestUrl(method, params);
		String data = gson.toJson(jsonData);

		log.info("===============================================");
		log.info("Request: " + urlStr);
		log.info("Data: " + data);

		if (encrypt)
			data = CryptoUtil.pandoraEncrypt(data);
		Response response;
		String requestResponse = HTTPUtil.jsonRequest(urlStr, data);
		log.info("Response: " + requestResponse);
		response = gson.fromJson(requestResponse, Response.class);

		if (response.getStat().equals("fail"))
			throw new PandoraException(method, response.getMessage(), response.getCode());

		return response.getResult();
	}

	private static String createRequestUrl(Method method, ParameterMap params)
	{
		StringBuffer url = new StringBuffer(SERVICE_URL);
		if (params == null)
			params = new ParameterMap();
		params.add("method", method.getName());

		url.append(URLEncodedUtils.format(params.getNameValuePairList(), "UTF-8"));
		return url.toString();
	}

	private static String jsonRequest(String urlStr, String data) throws CommunicationException
	{
		try
		{
			StringEntity json = new StringEntity(data.toString());
			json.setContentType("application/json");
			HttpPost post = new HttpPost(urlStr);
			post.addHeader("Content-Type", "application/json");
			post.setEntity(json);
			return IOUtils.toString(BelugaHTTPClient.getInstance().httpRequest(post));
		} catch (Exception e)
		{
			throw new CommunicationException(e);
		}
	}

	public static byte[] request(String urlStr) throws CommunicationException
	{
		try
		{
			HttpGet get = new HttpGet(urlStr);
			return IOUtils.toByteArray(BelugaHTTPClient.getInstance().httpRequest(get));
		} catch (IOException e)
		{
			throw new CommunicationException(e);
		}
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
