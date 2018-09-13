/*
 * Copyright (C) 2012-2018 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.util;

import info.bonjean.beluga.connection.BelugaHTTPClient;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.exception.PandoraException;
import info.bonjean.beluga.request.JsonRequest;
import info.bonjean.beluga.request.Method;
import info.bonjean.beluga.request.ParameterMap;
import info.bonjean.beluga.response.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class HTTPUtil
{
	private static final Logger log = LoggerFactory.getLogger(HTTPUtil.class);

	private static final Gson gson = GsonUtil.getGsonInstance();
	private static final String SERVICE_URL = "http://tuner.pandora.com/services/json/?";

	public static <E> E request(Method method, ParameterMap params, JsonRequest jsonData,
			boolean encrypt, TypeToken<Response<E>> typeToken) throws BelugaException
	{
		String urlStr = createRequestUrl(method, params);
		String data = gson.toJson(jsonData);

		log.debug("Request: " + urlStr);
		log.debug("Data: " + data);

		if (encrypt)
			data = CryptoUtil.pandoraEncrypt(data);

		String requestResponse = HTTPUtil.jsonRequest(urlStr, data);
		log.debug("Response: " + requestResponse);

		Response<E> response;
		try
		{
			response = gson.fromJson(requestResponse, typeToken.getType());
		}
		catch (JsonSyntaxException e)
		{
			throw new CommunicationException("invalidPandoraResponse", e);
		}

		if (response.getStat().equals("fail"))
			throw new PandoraException(method, response.getCode());

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
			return BelugaHTTPClient.getInstance().requestPost(post);
		}
		catch (Exception e)
		{
			throw new CommunicationException("communicationProblem", e);
		}
	}

	public static byte[] request(String urlStr) throws CommunicationException
	{
		try
		{
			return BelugaHTTPClient.getInstance().requestGet(new HttpGet(urlStr));
		}
		catch (IOException e)
		{
			throw new CommunicationException("communicationProblem", e);
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
