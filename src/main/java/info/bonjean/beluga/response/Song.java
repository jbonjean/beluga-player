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
package info.bonjean.beluga.response;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class Song
{
	private String trackToken;
	private String artistName;
	private String albumName;
	private String amazonAlbumUrl;
	private String songExplorerUrl;
	private String albumArtUrl;
	private String artistDetailUrl;
	private Map<String, Audio> audioUrlMap;
	private String adToken;
	private String songName;
	private int songRating;
	private String stationId;
	private String trackGain;
	private boolean songBookmarked = false;
	private boolean artistBookmarked = false;
	private String albumArtBase64;
	private List<String> focusTraits;

	public String getTrackToken()
	{
		return trackToken;
	}

	public Map<String, Audio> getAudioUrlMap()
	{
		return audioUrlMap;
	}

	public void setAudioUrlMap(Map<String, Audio> audioUrlMap)
	{
		this.audioUrlMap = audioUrlMap;
	}

	public void setTrackToken(String trackToken)
	{
		this.trackToken = trackToken;
	}

	public String getArtistName()
	{
		return artistName;
	}

	public void setArtistName(String artistName)
	{
		this.artistName = artistName;
	}

	public String getAlbumName()
	{
		return albumName;
	}

	public void setAlbumName(String albumName)
	{
		this.albumName = albumName;
	}

	public String getAmazonAlbumUrl()
	{
		return amazonAlbumUrl;
	}

	public void setAmazonAlbumUrl(String amazonAlbumUrl)
	{
		this.amazonAlbumUrl = amazonAlbumUrl;
	}

	public String getSongExplorerUrl()
	{
		return songExplorerUrl;
	}

	public void setSongExplorerUrl(String songExplorerUrl)
	{
		this.songExplorerUrl = songExplorerUrl;
	}

	public String getAlbumArtUrl()
	{
		return albumArtUrl;
	}

	public void setAlbumArtUrl(String albumArtUrl)
	{
		this.albumArtUrl = albumArtUrl;
	}

	public String getArtistDetailUrl()
	{
		return artistDetailUrl;
	}

	public void setArtistDetailUrl(String artistDetailUrl)
	{
		this.artistDetailUrl = artistDetailUrl;
	}

	public String getAdToken()
	{
		return adToken;
	}

	public void setAdToken(String adToken)
	{
		this.adToken = adToken;
	}

	public String getSongName()
	{
		return songName;
	}

	public void setSongName(String songName)
	{
		this.songName = songName;
	}

	public int getSongRating()
	{
		return songRating;
	}

	public void setSongRating(int songRating)
	{
		this.songRating = songRating;
	}

	public String getStationId()
	{
		return stationId;
	}

	public void setStationId(String stationId)
	{
		this.stationId = stationId;
	}

	public String getTrackGain()
	{
		return trackGain;
	}

	public void setTrackGain(String trackGain)
	{
		this.trackGain = trackGain;
	}

	public boolean isSongBookmarked()
	{
		return songBookmarked;
	}

	public void setSongBookmarked(boolean songBookmarked)
	{
		this.songBookmarked = songBookmarked;
	}

	public boolean isArtistBookmarked()
	{
		return artistBookmarked;
	}

	public void setArtistBookmarked(boolean artistBookmarked)
	{
		this.artistBookmarked = artistBookmarked;
	}

	public String getAlbumArtBase64()
	{
		return albumArtBase64;
	}

	public void setAlbumArtBase64(String albumArtBase64)
	{
		this.albumArtBase64 = albumArtBase64;
	}

	public List<String> getFocusTraits()
	{
		return focusTraits;
	}

	public void setFocusTraits(List<String> focusTraits)
	{
		this.focusTraits = focusTraits;
	}
}
