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
public enum Page
{
	COMMON, WELCOME, CONFIGURATION, SONG, NOTIFICATION, STATION_ADD, USER_CREATE, ABOUT;
	
	public static final String TEMPLATE_PATH = "/vm/pages/";
	public static final String HTML_PATH = "/html/";
	public static final String CSS_PATH = "/css/";
	public static final String JS_PATH = "/js/";
	public static final String IMG_PATH = "/img/";
	
	public String getTemplate()
	{
		return TEMPLATE_PATH + name().toLowerCase() + ".vm";
	}
	
	public String getCss()
	{
		return CSS_PATH + name().toLowerCase() + ".css";
	}
	
	public String getJs()
	{
		return JS_PATH + name().toLowerCase() + ".js";
	}

	public static Page fromString(String page)
	{
		return valueOf(page.toUpperCase().replace("-", "_"));
	}
}