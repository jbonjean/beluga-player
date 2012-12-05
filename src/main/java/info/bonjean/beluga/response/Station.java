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

import java.util.Date;
import java.util.List;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class Station
{
	private String stationId;
	private boolean isQuickMix;
	private String stationDetailUrl;
	private String stationToken;
	private String stationName;
	
	// getStation attributes
	private boolean allowAddMusic;
	private Date dateCreated;
	private String artUrl;
	//private StationMusic music
	private boolean isShared;
	private boolean allowDelete;
	private List<String> genre;
	private boolean allowRename;
	private String stationSharingUrl;
	//private StationFeedback feedback; 

	public boolean isQuickMix()
	{
		return isQuickMix;
	}

	public void setQuickMix(boolean isQuickMix)
	{
		this.isQuickMix = isQuickMix;
	}

	public String getStationDetailUrl()
	{
		return stationDetailUrl;
	}

	public void setStationDetailUrl(String stationDetailUrl)
	{
		this.stationDetailUrl = stationDetailUrl;
	}

	public String getStationToken()
	{
		return stationToken;
	}

	public void setStationToken(String stationToken)
	{
		this.stationToken = stationToken;
	}

	public String getStationName()
	{
		return stationName;
	}

	public void setStationName(String stationName)
	{
		this.stationName = stationName;
	}

	public String getStationId()
	{
		return stationId;
	}

	public void setStationId(String stationId)
	{
		this.stationId = stationId;
	}
}
