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
package info.bonjean.beluga.response;

import java.util.List;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class Result
{
	private String partnerId;
	private String partnerAuthToken;
	private String syncTime;
	private String userAuthToken;
	private String userId;
	private List<Station> stations;
	private List<Song> items;

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

	public String getSyncTime()
	{
		return syncTime;
	}

	public void setSyncTime(String syncTime)
	{
		this.syncTime = syncTime;
	}

	public String getUserAuthToken()
	{
		return userAuthToken;
	}

	public void setUserAuthToken(String userAuthToken)
	{
		this.userAuthToken = userAuthToken;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public List<Station> getStations()
	{
		return stations;
	}

	public void setStations(List<Station> stations)
	{
		this.stations = stations;
	}

	public List<Song> getItems()
	{
		return items;
	}

	public void setItems(List<Song> items)
	{
		this.items = items;
	}
}
