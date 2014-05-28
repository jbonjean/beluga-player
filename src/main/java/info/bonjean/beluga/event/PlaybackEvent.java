/*
 * Copyright (C) 2012, 2013, 2014 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.event;

import info.bonjean.beluga.response.Song;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class PlaybackEvent
{
	public enum Type
	{
		SONG_START, SONG_PAUSE, SONG_RESUME, SONG_FINISH, PANDORA_CONNECTED, PANDORA_DISCONNECTED;
	}

	private Song song;
	private Type type;

	public PlaybackEvent(Type type, Song song)
	{
		this.type = type;
		this.song = song;
	}

	public Song getSong()
	{
		return song;
	}

	public Type getType()
	{
		return type;
	}
}
