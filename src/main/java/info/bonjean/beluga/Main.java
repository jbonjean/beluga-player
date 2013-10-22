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
package info.bonjean.beluga;

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.gui.PivotUI;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 *         Use the option -Ddebug=1 to run in debug mode.
 * 
 */
public class Main
{
	public static void main(String[] args)
	{
		String version = Main.class.getPackage().getImplementationVersion();
		if (version == null)
			version = "(dev)";

		System.out.println("Beluga BelugaMP3Player " + version);
		if (args.length == 1 && args[0].equals("-version"))
			System.exit(0);

		BelugaState.getInstance().setVersion(version);
		BelugaConfiguration.getInstance().load();

		PivotUI.startDesktopUI();
	}
}
