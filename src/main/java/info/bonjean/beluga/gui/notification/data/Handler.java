/*
 * Copyright (C) 2012, 2013, 2014 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.gui.notification.data;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 * Code from:
 * http://stackoverflow.com/questions/9388264/jeditorpane-with-inline-image
 * 
 */
public class Handler extends URLStreamHandler
{

	@Override
	protected URLConnection openConnection(URL u) throws IOException
	{
		return new DataConnection(u);
	}

	public static void install()
	{
		String pkgName = Handler.class.getPackage().getName();
		String pkg = pkgName.substring(0, pkgName.lastIndexOf('.'));

		String protocolHandlers = System.getProperty("java.protocol.handler.pkgs", "");
		if (!protocolHandlers.contains(pkg))
		{
			if (!protocolHandlers.isEmpty())
			{
				protocolHandlers += "|";
			}
			protocolHandlers += pkg;
			System.setProperty("java.protocol.handler.pkgs", protocolHandlers);
		}
	}
}
