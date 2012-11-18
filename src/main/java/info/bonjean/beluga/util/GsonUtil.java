/* Copyright (C) 2012 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.util;

import info.bonjean.beluga.log.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class GsonUtil
{
	@SuppressWarnings("unused")
	private static Logger log = new Logger(GsonUtil.class);

	private static Gson gson;

	public static Gson getGsonInstance()
	{

		if (gson == null)
		{
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.disableHtmlEscaping();
			gson = gsonBuilder.create();
		}

		return gson;
	}
}
