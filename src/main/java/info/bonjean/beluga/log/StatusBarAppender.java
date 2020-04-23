/*
 * Copyright (C) 2012-2020 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.log;

import info.bonjean.beluga.gui.pivot.ThreadPools;
import info.bonjean.beluga.util.ResourcesUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Label;

import java.io.Serializable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Plugin(name = "StatusBar", category = "Core", elementType = "appender", printObject = true)
public final class StatusBarAppender extends AbstractAppender {
	private static final long LOG_MESSAGE_DURATION = 3 * 1000;

	private static Label statusBarText;
	private static Resources resources;
	private Level levelDisplayed;
	private ScheduledFuture<?> expirationTaskFuture;
	private static StatusBarAppender instance;

	protected StatusBarAppender(String name, Filter filter, Layout<? extends Serializable> layout,
			boolean ignoreExceptions) {
		super(name, filter, layout, ignoreExceptions);
		instance = this;
	}

	@PluginFactory
	public static StatusBarAppender createAppender(@PluginAttribute("name") final String name,
			@PluginElement("Layout") Layout<? extends Serializable> layout, @PluginElement("Filter") Filter filter,
			@PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final String ignore) {
		if (name == null) {
			LOGGER.error("No name provided for StatusBarAppender");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);

		return new StatusBarAppender(name, filter, layout, ignoreExceptions);
	}

	@Override
	public void append(LogEvent event) {
		// stop if we didn't receive the UI information
		if (statusBarText == null || resources == null)
			return;

		display(event);
	}

	private void clearMessage() {
		// clear the status bar
		ApplicationContext.queueCallback(new Runnable() {
			@Override
			public void run() {
				statusBarText.setText("");
			}
		}, true);

		levelDisplayed = null;
	}

	public static void clearErrorMessage() {
		if (instance == null)
			return;

		if (instance.levelDisplayed == null)
			return;

		if (instance.levelDisplayed.isMoreSpecificThan(Level.ERROR))
			instance.clearMessage();
	}

	private final Runnable expirationTask = new Runnable() {
		@Override
		public void run() {
			// ensure we don't clear an error message
			if (!instance.levelDisplayed.isMoreSpecificThan(Level.ERROR))
				clearMessage();
		}
	};

	private String formatMessage(LogEvent event) {
		// retrieve the original message
		String key = event.getMessage().getFormattedMessage();

		// if no key, something bad happened
		if (key == null)
			key = "unknownMessage";

		// try to translate it
		String message = (String) resources.get(key);

		// if the translation failed, use the original message
		if (message == null)
			message = key;

		// return the shorten the message
		return ResourcesUtil.shorten(message, 80);
	}

	public boolean display(LogEvent event) {
		// if no message currently displayed
		if (levelDisplayed == null) {
			doDisplay(event);
			return true;
		}

		// if this is at least as important as what we currently display
		if (event.getLevel().isMoreSpecificThan(levelDisplayed)) {
			doDisplay(event);
			return true;
		}

		// the message is discarded
		return false;
	}

	public void doDisplay(LogEvent event) {
		final Level level = event.getLevel();

		if (!level.isMoreSpecificThan(Level.ERROR))
			scheduleMessageExpiration();
		else
			unscheduleMessageExpiration();

		levelDisplayed = level;

		ApplicationContext.queueCallback(new Runnable() {
			@Override
			public void run() {
				String message = formatMessage(event);
				if (message != null) {
					statusBarText.setText(message);
					if (level.isMoreSpecificThan(Level.ERROR))
						statusBarText.getStyles().put("color", 21);
					else
						statusBarText.getStyles().put("color", 0);
				}
			}
		}, false);
	}

	public void unscheduleMessageExpiration() {
		if (expirationTaskFuture != null && !expirationTaskFuture.isDone())
			expirationTaskFuture.cancel(true);
	}

	public void scheduleMessageExpiration() {
		unscheduleMessageExpiration();

		// schedule the expiration task
		expirationTaskFuture = ThreadPools.statusBarScheduler.schedule(expirationTask, LOG_MESSAGE_DURATION,
				TimeUnit.MILLISECONDS);
	}

	public static void setLabel(Label statusBarText) {
		StatusBarAppender.statusBarText = statusBarText;
	}

	public static void setResources(Resources resources) {
		StatusBarAppender.resources = resources;
	}
}
