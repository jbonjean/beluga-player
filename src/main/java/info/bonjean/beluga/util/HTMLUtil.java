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
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.exception.InternalException;
import info.bonjean.beluga.log.Logger;
import info.bonjean.beluga.response.Station;
import info.bonjean.beluga.statefull.BelugaState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class HTMLUtil
{
	private final static Logger log = new Logger(HTMLUtil.class);

	private static final BelugaState state = BelugaState.getInstance();
	private static final BelugaConfiguration configuration = BelugaConfiguration.getInstance();

	public static byte[] getResourceAsByteArray(String resource) throws InternalException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream bais;
		try
		{
			bais = HTMLUtil.class.getResourceAsStream(resource);
			int c;
			while ((c = bais.read()) != -1)
			{
				baos.write(c);
			}
			bais.close();
			baos.close();
		} catch (IOException e)
		{
			throw new InternalException(e);
		}
		return baos.toByteArray();
	}
	
	public static String getResourceAsBase64String(String resource) throws InternalException
	{
		return Base64.encodeBase64String(getResourceAsByteArray(resource));
	}
	
	public static String getURLContentAsBase64String(String url) throws CommunicationException
	{
		return Base64.encodeBase64String(HTTPUtil.request(url));
	}

	private static String readFile(String name)
	{
		String html = null;
		try
		{
			StringWriter writer = new StringWriter();
			IOUtils.copy(HTMLUtil.class.getResourceAsStream(name), writer, "UTF-8");
			html = writer.toString();
		} catch (IOException e)
		{
			log.error(e.toString());
		}
		if (html == null)
			html = "error";

		return html;
	}

	private static String replace(String html, Map<String, String> tokens)
	{
		for (String token : tokens.keySet())
			html = html.replace(token, tokens.get(token));

		return html;
	}

	public static String getWelcome()
	{
		Map<String, String> tokens = new HashMap<String, String>();
		String loader = null;
		try
		{
			loader = getResourceAsBase64String("/img/ajax-loader.gif");
		} catch (InternalException e)
		{
			log.error("Cannot load loading animation");
			loader = "";
		}
		tokens.put("$IMG_LOAD$", loader);
		return replace(readFile("/welcome.html"), tokens);
	}

	public static String getSong()
	{
		Map<String, String> tokens = new HashMap<String, String>();
		tokens.put("$CSS$", readFile("/song.css"));
		tokens.put("$USERNAME$", configuration.getUserName());
		tokens.put("$STATION_NAME$", state.getStation().getStationName());
		tokens.put("$ALBUM_NAME$", state.getSong().getAlbumName());
		tokens.put("$ARTIST_NAME$", state.getSong().getArtistName());
		String coverUrl = state.getSong().getAlbumArtUrl();
		String cover = null;
		if (coverUrl != null && !coverUrl.isEmpty())
		{
			try
			{
				cover = getURLContentAsBase64String(coverUrl);
			} catch (CommunicationException e)
			{
				log.error("Cannot retrieve cover: " + coverUrl);
			}
		}
		if(cover == null)
		{
			try
			{
				cover = getResourceAsBase64String("/img/beluga.200x200.png");
			} catch (InternalException e)
			{
				log.error("Cannot load default cover");
				cover = "";
			}
		}
		String loader = null;
		try
		{
			loader = getResourceAsBase64String("/img/ajax-loader-2.gif");
		} catch (InternalException e)
		{
			log.error("Cannot load loading animation");
			loader = "";
		}
		tokens.put("$LOADER$", loader);
		tokens.put("$ALBUM_COVER$", cover);
		tokens.put("$SONG_NAME$", state.getSong().getSongName());
		tokens.put("$STATION_LIST$", generateStationListHTML());
		String feedbackClass = "";
		if (state.getSong().getSongRating() > 0)
			feedbackClass = "liked";
		tokens.put("$LIKE_CLASS$", feedbackClass);

		return replace(readFile("/song.html"), tokens);
	}

	private static String generateStationListHTML()
	{
		StringBuffer html = new StringBuffer();
		for (Station station : state.getStationList())
		{
			html.append("<option value='");
			html.append(station.getStationId());
			html.append("' ");
			if (station.getStationId().equals(state.getStation().getStationId()))
				html.append("selected");
			html.append(">");
			html.append(station.getStationName());
			html.append("</option>");
		}
		return html.toString();
	}

	public static String getConfiguration()
	{
		Map<String, String> tokens = new HashMap<String, String>();
		tokens.put("$USERNAME$", configuration.getUserName());
		tokens.put("$PASSWORD$", configuration.getPassword());
		tokens.put("$PROXY_HOST$", configuration.getProxyServer());
		String proxyHost = "";
		if (configuration.getProxyServerPort() != null)
			proxyHost = String.valueOf(configuration.getProxyServerPort());
		tokens.put("$PROXY_PORT$", proxyHost);

		return replace(readFile("/configuration.html"), tokens);
	}
}
