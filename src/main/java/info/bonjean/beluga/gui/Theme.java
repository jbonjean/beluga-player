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
package info.bonjean.beluga.gui;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public enum Theme
{
	CLASSIC("classic")/*, BLACK_AND_WHITE("black.and.white")*/;

	private static final Map<String, Theme> lookup = new HashMap<String, Theme>();

	static
	{
		for (Theme s : EnumSet.allOf(Theme.class))
			lookup.put(s.getId(), s);
	}

	public static Theme get(String id)
	{
		return lookup.get(id);
	}

	private final String id;

	private Theme(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}
}
