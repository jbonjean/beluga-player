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
package info.bonjean.beluga.request;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class UserLoginRequest extends JsonRequest
{
	private String username;
	private String password;
	private long syncTime;
	private String loginType = "user";
	private String partnerAuthToken;

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getLoginType()
	{
		return loginType;
	}

	public void setLoginType(String loginType)
	{
		this.loginType = loginType;
	}

	public String getPartnerAuthToken()
	{
		return partnerAuthToken;
	}

	public void setPartnerAuthToken(String partnerAuthToken)
	{
		this.partnerAuthToken = partnerAuthToken;
	}

	public long getSyncTime()
	{
		return syncTime;
	}

	public void setSyncTime(long syncTime)
	{
		this.syncTime = syncTime;
	}
}
