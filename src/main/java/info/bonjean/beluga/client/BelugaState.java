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
package info.bonjean.beluga.client;

import info.bonjean.beluga.gui.Page;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.response.Station;

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

	private String userId;
	private String userAuthToken;
	private String partnerId;
	private String partnerAuthToken;

	private List<Station> stationList;

	private Station station;
	private List<Song> playlist;
	private Song song;

	private Page page;
	private Page pageBack;
	
	private float volume = 1f;
	// workaround for missing feature in webkit
	// we store the volume value before mute to be able to restore it later
	private float mutedVolume = 0f;

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

	public boolean isPandoraReachable()
	{
		return partnerAuthToken != null && partnerAuthToken.length() > 0;
	}

	public boolean isLoggedIn()
	{
		return userAuthToken != null && userAuthToken.length() > 0;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getUserAuthToken()
	{
		return userAuthToken;
	}

	public void setUserAuthToken(String userAuthToken)
	{
		this.userAuthToken = userAuthToken;
	}

	public String getPartnerId()
	{
		return partnerId;
	}

	public void setPartnerId(String partnerId)
	{
		this.partnerId = partnerId;
	}

	public String getPartnerAuthToken()
	{
		return partnerAuthToken;
	}

	public void setPartnerAuthToken(String partnerAuthToken)
	{
		this.partnerAuthToken = partnerAuthToken;
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

	public Page getPage()
	{
		return page;
	}

	public void setPage(Page page)
	{
		this.page = page;
	}

	public Page getPageBack()
	{
		return pageBack;
	}

	public void setPageBack(Page pageBack)
	{
		this.pageBack = pageBack;
	}

	public float getMutedVolume()
	{
		return mutedVolume;
	}

	public void setMutedVolume(float mutedVolume)
	{
		this.mutedVolume = mutedVolume;
	}

	public float getVolume()
	{
		return volume;
	}

	public void setVolume(float volume)
	{
		this.volume = volume;
	}
}
