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
import info.bonjean.beluga.response.Station;
import info.bonjean.beluga.statefull.BelugaState;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

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

	private static String readFile(String name)
	{
		String html = null;
		try
		{
			html = FileUtils.readFileToString(FileUtils.toFile(HTMLUtil.class.getResource(name)), "UTF-8");
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
		if (coverUrl == null || coverUrl.isEmpty())
			// TODO: find something better
			coverUrl = "http://c95711.r11.cf3.rackcdn.com/VHV-2150.jpg";
		tokens.put("$ALBUM_COVER_URL$", coverUrl);
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
		if(configuration.getProxyServerPort() != null)
			proxyHost = String.valueOf(configuration.getProxyServerPort());
		tokens.put("$PROXY_PORT$", proxyHost);

		return replace(readFile("/configuration.html"), tokens);
	}
}
