/* Copyright (C) 2012, 2013 Julien Bonjean <julien@bonjean.info>
 * 
 * This file is part of Beluga Player.
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
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.gui.pivot.SongUI;
import info.bonjean.beluga.log.Log;
import info.bonjean.beluga.response.Song;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.media.Picture;
import org.slf4j.Logger;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class HTMLUtil
{
	@Log
	private static Logger log;

	public static Image getDefaultCover()
	{
		try
		{
			return new Picture(ImageIO.read(SongUI.class.getResourceAsStream("/img/beluga.200x200.png")));
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public static String shorten(String str, int length)
	{
		if (str.length() > length)
			return str.substring(0, length - 3) + "...";
		return str;
	}

	public static String replaceTokens(String string) throws UnsupportedEncodingException
	{
		Song song = BelugaState.getInstance().getSong();

		if (song != null)
		{
			string = string.replaceAll("\\$\\{song\\}", URLEncoder.encode(song.getSongName(), "UTF-8"));
			string = string.replaceAll("\\$\\{artist\\}", URLEncoder.encode(song.getArtistName(), "UTF-8"));
			string = string.replaceAll("\\$\\{album\\}", URLEncoder.encode(song.getAlbumName(), "UTF-8"));
			string = string.replaceAll("\\$\\{url\\}", song.getAdditionalAudioUrl());
		}
		return string;
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
		}
		catch (Exception e)
		{
			log.error("Cannot load resource " + resource);
			System.exit(-1);
		}
		return baos.toByteArray();
	}

	public static String getResourceAsString(String resource)
	{
		return new String(getResourceAsByteArray(resource));
	}

	public static String getResourceBase64(String resource)
	{
		return Base64.encodeBase64String(getResourceAsByteArray(resource));
	}

	public static String getRemoteResourceBase64(String url) throws CommunicationException
	{
		return Base64.encodeBase64String(HTTPUtil.request(url));
	}
}
