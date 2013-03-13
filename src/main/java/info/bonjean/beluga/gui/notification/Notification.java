/* Copyright (C) 2012, 2013 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.gui.notification;

import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.log.Log;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.util.HTMLUtil;

import org.slf4j.Logger;

import ch.swingfx.twinkle.NotificationBuilder;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class Notification extends NotificationBuilder
{
	@Log
	private static Logger log;

	public final static int TIMEOUT = 5000;

	private String getMessage(Song song)
	{
		String message = HTMLUtil.getResourceAsString("/notification.html");
		message = message.replace("@SONG_NAME@", song.getSongName());
		message = message.replace("@ARTIST_NAME@", song.getArtistName());
		try
		{
			String coverArt = song.getAlbumArtUrl().isEmpty() ? HTMLUtil.getResourceBase64("/img/beluga.200x200.png") : HTMLUtil
					.getRemoteResourceBase64(song.getAlbumArtUrl());
			message = message.replace("@COVER_ART@", coverArt);
		}
		catch (CommunicationException e)
		{
			log.error(e.getMessage(), e);
		}
		return message;
	}

	public Notification(Song song)
	{
		super();
		withStyle(new NotificationStyle());
		withMessage(getMessage(song));
		withFadeInAnimation(false);
		withFadeOutAnimation(false);
		withDisplayTime(TIMEOUT);
		showNotification();
	}
}
