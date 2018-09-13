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
package info.bonjean.beluga.response;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class SongBookmark
{
	private String musicToken;
	private String artistName;
	private String artUrl;
	private String bookmarkToken;
	private Date dateCreated;
	private String sampleUrl;
	private String sampleGain;
	private String albumName;
	private String songName;

	public String getMusicToken()
	{
		return musicToken;
	}

	public void setMusicToken(String musicToken)
	{
		this.musicToken = musicToken;
	}

	public String getArtistName()
	{
		return artistName;
	}

	public void setArtistName(String artistName)
	{
		this.artistName = artistName;
	}

	public String getArtUrl()
	{
		return artUrl;
	}

	public void setArtUrl(String artUrl)
	{
		this.artUrl = artUrl;
	}

	public String getBookmarkToken()
	{
		return bookmarkToken;
	}

	public void setBookmarkToken(String bookmarkToken)
	{
		this.bookmarkToken = bookmarkToken;
	}

	public Date getDateCreated()
	{
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated)
	{
		this.dateCreated = dateCreated;
	}

	public String getSampleUrl()
	{
		return sampleUrl;
	}

	public void setSampleUrl(String sampleUrl)
	{
		this.sampleUrl = sampleUrl;
	}

	public String getSampleGain()
	{
		return sampleGain;
	}

	public void setSampleGain(String sampleGain)
	{
		this.sampleGain = sampleGain;
	}

	public String getAlbumName()
	{
		return albumName;
	}

	public void setAlbumName(String albumName)
	{
		this.albumName = albumName;
	}

	public String getSongName()
	{
		return songName;
	}

	public void setSongName(String songName)
	{
		this.songName = songName;
	}
}
