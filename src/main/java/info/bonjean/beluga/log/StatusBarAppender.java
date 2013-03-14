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

import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.gui.pivot.ThreadPools;
import info.bonjean.beluga.util.HTMLUtil;

import java.util.Date;

import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class StatusBarAppender<E> extends AppenderBase<E>
{
	private static final long messageDuration = 3 * 1000;
	private static Label label = null;
	@SuppressWarnings("unused")
	private static ImageView icon = null;
	private static Resources resources = null;
	private long lastMessage = 0L;
	private long lastMessageCleared = 0L;
	private Level lastMessageLevel = Level.INFO;

	public StatusBarAppender()
	{
		ThreadPools.statusPool.execute(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					try
					{
						Thread.sleep(messageDuration);
					}
					catch (InterruptedException e)
					{
						break;
					}
					if (lastMessage != lastMessageCleared && !lastMessageLevel.isGreaterOrEqual(Level.ERROR)
							&& (lastMessage + messageDuration < new Date().getTime()))
					{
						lastMessageCleared = lastMessage;
						ApplicationContext.queueCallback(new Runnable()
						{
							@Override
							public void run()
							{
								label.setText("");
							}
						}, true);
					}
				}
			}
		});
	}

	@Override
	protected void append(final E eventObject)
	{
		// if not a loggingevent, what is it?
		if (!(eventObject instanceof LoggingEvent))
			return;

		final LoggingEvent event = (LoggingEvent) eventObject;

		if (label == null)
			return;

		String message = null;

		if (resources != null)
		{
			// if we received an exception
			if (event.getThrowableProxy() != null)
			{
				try
				{
					@SuppressWarnings("rawtypes")
					Class clazz = Class.forName(event.getThrowableProxy().getClassName());

					// use PandoraException message as key and translate it
					if (clazz.isInstance(BelugaException.class))
					{
						String key = event.getThrowableProxy().getMessage();
						message = (String) resources.get(key);
					}
				}
				catch (ClassNotFoundException e)
				{
				}
			}
			// no exception, this is a single text key and should be translatable
			else
				message = (String) resources.get(event.getMessage());
		}

		// if no message yet, use the default
		if (message == null)
			message = event.getMessage();

		final StringBuffer sb = new StringBuffer();
		sb.append('[');
		sb.append(event.getLevel());
		sb.append("] ");
		sb.append(HTMLUtil.shorten(message, 80));

		ApplicationContext.queueCallback(new Runnable()
		{
			@Override
			public void run()
			{
				label.setText(sb.toString());
				if (event.getLevel().isGreaterOrEqual(Level.ERROR))
					label.getStyles().put("color", "#ff0000");
				else
					label.getStyles().put("color", "#000000");
				lastMessage = new Date().getTime();
				lastMessageLevel = event.getLevel();
			}
		}, false);
	}

	public static void setLabel(Label label)
	{
		StatusBarAppender.label = label;
	}

	public static void setResources(Resources resources)
	{
		StatusBarAppender.resources = resources;
	}

	public static void setIcon(ImageView icon)
	{
		StatusBarAppender.icon = icon;
	}
}