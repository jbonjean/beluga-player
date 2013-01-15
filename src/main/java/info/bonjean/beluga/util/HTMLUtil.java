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

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.exception.InternalException;
import info.bonjean.beluga.gui.Page;
import info.bonjean.beluga.gui.RenderingEngine;
import info.bonjean.beluga.gui.Theme;
import info.bonjean.beluga.response.Date;
import info.bonjean.beluga.response.Result;
import info.bonjean.beluga.response.SearchArtist;
import info.bonjean.beluga.response.SearchItem;
import info.bonjean.beluga.response.SearchSong;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.VelocityContext;
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

	public static String shorten(String str, int length)
	{
		if (str.length() > length)
			return str.substring(0, length - 3) + "...";
		return str;
	}

	private static byte[] getResourceAsByteArray(String resource)
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
		} catch (Exception e)
		{
			log.error("Cannot load resource " + resource);
			System.exit(-1);
		}
		return baos.toByteArray();
	}

	public static String getResourceBase64(String resource)
	{
		return Base64.encodeBase64String(getResourceAsByteArray(resource));
	}
	
	public static String getThemeResourceBase64(String resource)
	{
		return Base64.encodeBase64String(getResourceAsByteArray(getThemeResourcePath(resource)));
	}
	
	public static String getThemeResourcePath(String path)
	{
		// first, try with current theme
		String themePath = "/themes/" + BelugaConfiguration.getInstance().getThemeId() + "/" + path;
		String newPath = themePath;
		URL resource = HTMLUtil.class.getResource(themePath);
		
		if(resource == null)
		{
			log.info("Resource " + path + " not found for theme " + BelugaConfiguration.getInstance().getThemeId());
			newPath = "/themes/" + Theme.CLASSIC.getId() + "/" + path;
			// fallback to classic theme
			resource = HTMLUtil.class.getResource(newPath);
		}
		
		if(resource == null)
		{
			log.info("Resource " + path + " not found for default theme");
			newPath = path;
			// second fallback to built-in resources
			resource = HTMLUtil.class.getResource(path);
		}
		
		if(resource == null)
		{
			log.error("Resource " + path + " not found");
			// it's over, return the theme path to have a clean error message
			return themePath;
		}
		
		return newPath;
	}

	public static String getRemoteResourceBase64(String url) throws CommunicationException
	{
		return Base64.encodeBase64String(HTTPUtil.request(url));
	}

	public static String getPageHTML(Page page) throws InternalException
	{
		return getPageHTML(page, null);
	}

	public static String getFormattedPandoraDate(Date date)
	{
		return new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(date.getTime()));
	}

	public static String getPageHTML(Page page, Page pageBack) throws InternalException
	{
		VelocityContext context = new VelocityContext();
		context.put("stations", BelugaState.getInstance().getStationList());
		context.put("station", BelugaState.getInstance().getStation());
		context.put("song", BelugaState.getInstance().getSong());
		context.put("bookmarks", BelugaState.getInstance().getBookmarks());
		context.put("configuration", BelugaConfiguration.getInstance());
		context.put("isLoggedIn", PandoraClient.getInstance().isLoggedIn());
		context.put("isPandoraReachable", PandoraClient.getInstance().isPandoraReachable());
		context.put("HTMLUtil", HTMLUtil.class);
		context.put("text", I18NUtil.class);
		context.put("volume", BelugaState.getInstance().getVolume());
		context.put("mutedVolume", BelugaState.getInstance().getMutedVolume());
		context.put("pageBack", pageBack != null ? pageBack.name().toLowerCase() : null);
		context.put("page", page.name().toLowerCase());
		context.put("themes", Theme.values());
		context.put("debug", System.getProperty("debug") != null);
		context.put("version", BelugaState.getInstance().getVersion());
		return RenderingEngine.getInstance().render(context, page.getTemplate());
	}

	public static String getSearchResultsHTML(Result results) throws InternalException
	{
		List<SearchArtist> artists = results.getArtists();
		List<SearchSong> songs = results.getSongs();

		SearchItem bestMatch = null;
		if (!artists.isEmpty())
			bestMatch = artists.get(0);
		if (!songs.isEmpty() && (bestMatch == null || songs.get(0).getScore() > bestMatch.getScore()))
			bestMatch = songs.get(0);
		if (bestMatch instanceof SearchArtist)
			artists.remove(bestMatch);
		else
			songs.remove(bestMatch);

		VelocityContext context = new VelocityContext();
		context.put("artists", artists);
		context.put("songs", songs);
		context.put("bestMatch", bestMatch);
		context.put("nearchMatchesAvailable", results.isNearMatchesAvailable());
		context.put("text", I18NUtil.class);
		return RenderingEngine.getInstance().render(context, "/vm/search_results.vm");
	}
}
