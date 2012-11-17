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
package info.bonjean.beluga.request;

import info.bonjean.beluga.exception.InternalException;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class ParameterMap
{
	private List<NameValuePair> params = new LinkedList<NameValuePair>();

	public List<NameValuePair> getNameValuePairList()
	{
		return params;
	}

	public void add(String key, String value)
	{
		params.add(new BasicNameValuePair(key, value));
	}
}
