/*
 * Copyright (C) 2012-2021 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga;

import info.bonjean.beluga.bus.InternalBus;
import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.gui.PivotUI;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * - Use the option -Ddebug=1 to run in debug mode.
 * - Use the option -Dlog.level=LEVEL to control log level.
 */
public class Main {
	public static void main(String[] args) {
		String version = Main.class.getPackage().getImplementationVersion();
		if (version == null)
			version = BelugaConfiguration.CONFIGURATION_DEFAULT_VERSION;

		System.out.println("Beluga Player " + version);

		if (args.length == 1 && args[0].equals("--version"))
			System.exit(0);

		if (args.length == 1 && args[0].equals("--debug")) {
			System.setProperty("debug", "1");
			final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			final Configuration config = ctx.getConfiguration();
			LoggerConfig rootLoggerConfig = config.getLoggers().get("");
			rootLoggerConfig.removeAppender("Console");
			rootLoggerConfig.addAppender(config.getAppender("Console"), Level.DEBUG, null);
			ctx.updateLoggers();
		}

		BelugaState.getInstance().setVersion(version);
		BelugaConfiguration.getInstance().load();

		// enable anti-aliased text:
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");

		// start the events bus
		InternalBus.start();

		PivotUI.startDesktopUI();
	}
}
