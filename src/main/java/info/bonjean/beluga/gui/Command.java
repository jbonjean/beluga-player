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

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public enum Command
{
	LOGIN, NEXT, LIKE, BAN, SLEEP, PAUSE, EXIT, GOTO, CONFIGURATION, SAVE_CONFIGURATION, SELECT_STATION, SEARCH, ADD_STATION, DELETE_STATION, CREATE_USER, STORE_VOLUME;

	public static Command fromString(String name)
	{
		return valueOf(name.toUpperCase().replace("-", "_"));
	}
}
