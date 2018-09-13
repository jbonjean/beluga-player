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
package info.bonjean.beluga.client;

import info.bonjean.beluga.gui.pivot.Page;
import info.bonjean.beluga.response.Bookmarks;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.response.Station;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class BelugaState
{
	private static BelugaState instance;

	private List<Station> stationList = new ArrayList<Station>();

	private Station station;
	private List<Song> playlist;
	private Song song;
	private Bookmarks bookmarks;

	private Page page;
	private String version;

	Set<String> errors = new HashSet<String>();

	private BelugaState()
	{
	}

	public static BelugaState getInstance()
	{
		if (instance == null)
			instance = new BelugaState();

		return instance;
	}

	public List<Station> getStationList()
	{
		return stationList;
	}

	public void setStationList(List<Station> stationList)
	{
		this.stationList = stationList;
	}

	public Station getStation()
	{
		return station;
	}

	public void setStation(Station station)
	{
		this.station = station;
	}

	public List<Song> getPlaylist()
	{
		return playlist;
	}

	public void setPlaylist(List<Song> playlist)
	{
		this.playlist = playlist;
	}

	public Song getSong()
	{
		return song;
	}

	public void setSong(Song song)
	{
		this.song = song;
	}

	public Set<String> getErrors()
	{
		return errors;
	}

	public void clearErrors()
	{
		errors.clear();
	}

	public void addError(String key)
	{
		errors.add(key);
	}

	public void reset()
	{
		stationList = new ArrayList<Station>();
		station = null;
		playlist = null;
		song = null;
	}

	public Bookmarks getBookmarks()
	{
		return bookmarks;
	}

	public void setBookmarks(Bookmarks bookmarks)
	{
		this.bookmarks = bookmarks;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public Page getPage()
	{
		return page;
	}

	public void setPage(Page page)
	{
		this.page = page;
	}
}
