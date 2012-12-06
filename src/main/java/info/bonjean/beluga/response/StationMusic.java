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
package info.bonjean.beluga.response;

import java.util.List;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class StationMusic
{
	private List<Song> songs;
	private List<Song> artists;

	public List<Song> getSongs()
	{
		return songs;
	}

	public void setSongs(List<Song> songs)
	{
		this.songs = songs;
	}

	public List<Song> getArtists()
	{
		return artists;
	}

	public void setArtists(List<Song> artists)
	{
		this.artists = artists;
	}
}
