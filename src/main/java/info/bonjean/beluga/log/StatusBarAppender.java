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
package info.bonjean.beluga.log;

import info.bonjean.beluga.exception.PandoraException;

import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Label;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class StatusBarAppender<E> extends AppenderBase<E>
{
	private static Label statusBar = null;
	private static Resources resources = null;

	@Override
	protected void append(final E eventObject)
	{
		// if not a loggingevent, what is it?
		if (!(eventObject instanceof LoggingEvent))
			return;

		LoggingEvent event = (LoggingEvent) eventObject;

		if (statusBar == null)
			return;

		StringBuffer sb = new StringBuffer();

		// if we received an exception
		if (event.getThrowableProxy() != null && resources != null)
		{
			// use PandoraException message as key and translate it
			if (event.getThrowableProxy().getClassName().equals(PandoraException.class.getName()))
			{
				String key = event.getThrowableProxy().getMessage();
				sb.append('[');
				sb.append(event.getLevel());
				sb.append("] ");
				sb.append(resources.get(key));
			}
		}

		// if no message yet, use the default
		if (sb.length() == 0)
			sb.append(eventObject.toString());

		final String finalMessage = sb.toString();
		ApplicationContext.queueCallback(new Runnable()
		{
			@Override
			public void run()
			{
				statusBar.setText(finalMessage);
			}
		}, false);
	}

	public static void setStatusBar(Label statusBar)
	{
		StatusBarAppender.statusBar = statusBar;
	}

	public static void setResources(Resources resources)
	{
		StatusBarAppender.resources = resources;
	}
}