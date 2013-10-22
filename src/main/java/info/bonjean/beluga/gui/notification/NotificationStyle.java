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
package info.bonjean.beluga.gui.notification;

import java.awt.GraphicsConfiguration;

import javax.swing.Icon;
import javax.swing.JWindow;

import ch.swingfx.twinkle.style.AbstractNotificationStyle;
import ch.swingfx.twinkle.style.INotificationStyle;
import ch.swingfx.twinkle.window.ICreateNotificationWindow;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class NotificationStyle extends AbstractNotificationStyle
{
	public NotificationStyle()
	{
		super();
		withNotificationWindowCreator(new ICreateNotificationWindow()
		{
			@Override
			public JWindow createNotificationWindow(Icon icon, String title, String message, INotificationStyle style,
					GraphicsConfiguration graphicsConfiguration)
			{
				return new NotificationWindow(icon, title, message, style, graphicsConfiguration);
			}
		});
	}
}
