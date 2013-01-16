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

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class Response<T>
{
	private String stat;
	private String message;
	private long code;
	private T result;

	public String getStat()
	{
		return stat;
	}

	public void setStat(String stat)
	{
		this.stat = stat;
	}

	public T getResult()
	{
		return result;
	}

	public void setResult(T result)
	{
		this.result = result;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public long getCode()
	{
		return code;
	}

	public void setCode(long code)
	{
		this.code = code;
	}
}
