/* Copyright (C) 2012, 2013 Julien Bonjean <julien@bonjean.info>
 * 
 * This file is part of Beluga Player.
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

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class Date
{
	private int date;
	private int day;
	private int hours;
	private int minutes;
	private int month;
	private int nanos;
	private int seconds;
	private long time;
	private int timezoneOffset;
	private int year;

	public int getDate()
	{
		return date;
	}

	public void setDate(int date)
	{
		this.date = date;
	}

	public int getDay()
	{
		return day;
	}

	public void setDay(int day)
	{
		this.day = day;
	}

	public int getHours()
	{
		return hours;
	}

	public void setHours(int hours)
	{
		this.hours = hours;
	}

	public int getMinutes()
	{
		return minutes;
	}

	public void setMinutes(int minutes)
	{
		this.minutes = minutes;
	}

	public int getMonth()
	{
		return month;
	}

	public void setMonth(int month)
	{
		this.month = month;
	}

	public int getNanos()
	{
		return nanos;
	}

	public void setNanos(int nanos)
	{
		this.nanos = nanos;
	}

	public int getSeconds()
	{
		return seconds;
	}

	public void setSeconds(int seconds)
	{
		this.seconds = seconds;
	}

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public int getTimezoneOffset()
	{
		return timezoneOffset;
	}

	public void setTimezoneOffset(int timezoneOffset)
	{
		this.timezoneOffset = timezoneOffset;
	}

	public int getYear()
	{
		return year;
	}

	public void setYear(int year)
	{
		this.year = year;
	}
}
