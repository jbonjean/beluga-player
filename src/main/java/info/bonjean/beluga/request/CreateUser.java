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
package info.bonjean.beluga.request;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class CreateUser extends JsonData
{
	private String accountType = "registered";
	private String countryCode = "US";
	private String registeredType = "user";
	private String username;
	private String password;
	private int birthYear;
	private String zipCode;
	private String gender;
	private boolean emailOptIn;
	
	private long syncTime;
	private String partnerAuthToken;
	
	public String getAccountType()
	{
		return accountType;
	}
	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}
	public String getCountryCode()
	{
		return countryCode;
	}
	public void setCountryCode(String countryCode)
	{
		this.countryCode = countryCode;
	}
	public String getRegisteredType()
	{
		return registeredType;
	}
	public void setRegisteredType(String registeredType)
	{
		this.registeredType = registeredType;
	}
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
	public int getBirthYear()
	{
		return birthYear;
	}
	public void setBirthYear(int birthYear)
	{
		this.birthYear = birthYear;
	}
	public String getZipCode()
	{
		return zipCode;
	}
	public void setZipCode(String zipCode)
	{
		this.zipCode = zipCode;
	}
	public String getGender()
	{
		return gender;
	}
	public void setGender(String gender)
	{
		this.gender = gender;
	}
	public boolean isEmailOptIn()
	{
		return emailOptIn;
	}
	public void setEmailOptIn(boolean emailOptIn)
	{
		this.emailOptIn = emailOptIn;
	}
	public long getSyncTime()
	{
		return syncTime;
	}
	public void setSyncTime(long syncTime)
	{
		this.syncTime = syncTime;
	}
	public String getPartnerAuthToken()
	{
		return partnerAuthToken;
	}
	public void setPartnerAuthToken(String partnerAuthToken)
	{
		this.partnerAuthToken = partnerAuthToken;
	} 
}
