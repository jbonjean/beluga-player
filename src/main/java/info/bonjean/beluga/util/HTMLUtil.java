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
package info.bonjean.beluga.util;

import static info.bonjean.beluga.util.I18NUtil._;
import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.gui.Page;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.response.Station;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class HTMLUtil
{
	private final static Logger log = LoggerFactory.getLogger(HTMLUtil.class);

	private static final BelugaConfiguration configuration = BelugaConfiguration.getInstance();

	public static byte[] getResourceAsByteArray(String resource)
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
			log.error("Cannot load resource " + resource);
			System.exit(-1);
		}
		return baos.toByteArray();
	}

	public static String getResourceAsBase64String(String resource)
	{
		return Base64.encodeBase64String(getResourceAsByteArray(resource));
	}

	private static String getResourceAsString(String name)
	{
		String content = null;
		try
		{
			StringWriter writer = new StringWriter();
			IOUtils.copy(HTMLUtil.class.getResourceAsStream(name), writer, "UTF-8");
			content = writer.toString();
		} catch (Exception e)
		{
			log.error("Cannot load resource " + name);
			System.exit(-1);
		}
		return content;
	}

	public static String getURLContentAsBase64String(String url) throws CommunicationException
	{
		return Base64.encodeBase64String(HTTPUtil.request(url));
	}

	private static String replace(String html, Map<String, String> tokens)
	{
		for (String token : tokens.keySet())
			html = html.replace(token, tokens.get(token));

		return html;
	}

	private static String loadPage(Page page, Map<String, String> tokens)
	{
		// load page raw HTML
		String html = replace(getResourceAsString(page.getHTML()), tokens);

		// clear tokens for main html (wrapper)
		tokens.clear();

		// add css token
		StringBuffer sb = new StringBuffer();
		sb.append(getResourceAsString(Page.COMMON.getCss()));
		sb.append(getResourceAsString(page.getCss()));
		sb.append(getResourceAsString(Page.CSS_PATH + "tooltips.css"));
		if (System.getProperty("debug") != null)
			sb.append(getResourceAsString(Page.CSS_PATH + "debug.css"));
		tokens.put("$CSS$", sb.toString());

		// add js token
		sb = new StringBuffer();
		sb.append(getResourceAsString(Page.COMMON.getJs()));
		sb.append(getResourceAsString(page.getJs()));
		tokens.put("$JS$", sb.toString());

		// add images
		tokens.put("$LOADER$", getResourceAsBase64String(Page.IMG_PATH + "ajax-loader-2.gif"));

		// errors
		sb = new StringBuffer();
		for (String error : BelugaState.getInstance().getErrors())
			sb.append("<p>" + _(error) + "</p>");
		tokens.put("$ERRORS$", sb.toString());
		BelugaState.getInstance().clearErrors();

		tokens.put("$PAGE$", page.name());

		// add html token (include page to display)
		tokens.put("$CONTENT$", html);

		return replace(getResourceAsString(Page.COMMON.getHTML()), tokens);
	}

	private static String shorten(String str, int length)
	{
		if (str.length() > length)
			return str.substring(0, length - 3) + "...";
		return str;
	}

	public static String getWelcomeHTML()
	{
		Map<String, String> tokens = new HashMap<String, String>();
		tokens.put("$LOADER$", getResourceAsBase64String(Page.IMG_PATH + "ajax-loader.gif"));
		tokens.put("$BACKGROUND$", getResourceAsBase64String(Page.IMG_PATH + "beluga.600x400.png"));
		return loadPage(Page.WELCOME, tokens);
	}

	public static String getSongHTML(List<Station> stations, Station station, Song song)
	{
		Map<String, String> tokens = new HashMap<String, String>();
		tokens.put("$USERNAME$", configuration.getUserName());
		tokens.put("$STATION_NAME$", station.getStationName());
		tokens.put("$ALBUM_NAME$", song.getAlbumName());
		tokens.put("$ARTIST_NAME$", song.getArtistName());
		tokens.put("$ALBUM_COVER$", song.getAlbumArtBase64());
		tokens.put("$SONG_NAME$", song.getSongName());
		tokens.put("$STATION_LIST$", generateStationListHTML(stations, station));
		String feedbackClass = "";
		if (song.getSongRating() > 0)
			feedbackClass = "liked";
		tokens.put("$LIKE_CLASS$", feedbackClass);
		StringBuffer focusTraits = new StringBuffer();
		try
		{
			Builder parser = new Builder();
			Document doc = parser.build(song.getSongExplorerUrl());
			Nodes nodes = doc.query("/songExplorer/focusTrait/text()");
			for (int i = 0; i < nodes.size(); i++)
			{
				if (i > 0)
					focusTraits.append(", ");
				focusTraits.append(nodes.get(i).getValue());
			}
		} catch (Exception ex)
		{
			log.error("Cannot retrieve focus traits");
		}
		tokens.put("$FOCUS_TRAITS$", focusTraits.toString());

		return loadPage(Page.SONG, tokens);
	}

	public static String getNotificationHTML(Song song)
	{
		Map<String, String> tokens = new HashMap<String, String>();
		tokens.put("$ALBUM_COVER$", song.getAlbumArtBase64());
		tokens.put("$ARTIST_NAME$", shorten(song.getArtistName(), 30));
		tokens.put("$SONG_NAME$", shorten(song.getSongName(), 30));
		return replace(getResourceAsString(Page.NOTIFICATION.getHTML()), tokens);
	}

	public static String getConfigurationHTML()
	{
		Map<String, String> tokens = new HashMap<String, String>();
		tokens.put("$USERNAME$", configuration.getUserName());
		tokens.put("$PASSWORD$", configuration.getPassword());
		tokens.put("$PROXY_HOST$", configuration.getProxyServer());
		String proxyHost = "";
		if (configuration.getProxyServerPort() != null)
			proxyHost = String.valueOf(configuration.getProxyServerPort());
		tokens.put("$PROXY_PORT$", proxyHost);
		tokens.put("$PROXY_DNS$", configuration.getProxyDNS());
		tokens.put("$BACKGROUND$", getResourceAsBase64String(Page.IMG_PATH + "beluga.600x400.png"));
		tokens.put("$ICON_INFO$", getResourceAsBase64String(Page.ICONS_PATH + "info-20.png"));

		return loadPage(Page.CONFIGURATION, tokens);
	}

	private static String generateStationListHTML(List<Station> stations, Station selectedStation)
	{
		StringBuffer html = new StringBuffer();
		for (Station station : stations)
		{
			html.append("<option value='");
			html.append(station.getStationId());
			html.append("' ");
			if (station.getStationId().equals(selectedStation.getStationId()))
				html.append("selected");
			html.append(">");
			html.append(station.getStationName());
			html.append("</option>");
		}
		return html.toString();
	}

	public static String retrieveAlbumArt(Song song)
	{
		String coverUrl = song.getAlbumArtUrl();
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
		if (cover == null)
		{
			cover = getResourceAsBase64String(Page.IMG_PATH + "beluga.200x200.png");
		}
		return cover;
	}
}
