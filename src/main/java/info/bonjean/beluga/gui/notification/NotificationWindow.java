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
package info.bonjean.beluga.gui.notification;

import info.bonjean.beluga.gui.notification.data.Handler;

import java.awt.GraphicsConfiguration;

import javax.swing.Icon;
import javax.swing.JTextPane;
import javax.swing.JWindow;

import ch.swingfx.twinkle.style.INotificationStyle;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class NotificationWindow extends JWindow
{
	private static final long serialVersionUID = 516771806497568097L;

	public NotificationWindow(Icon icon, String title, String message, INotificationStyle style, GraphicsConfiguration graphicsConfiguration)
	{
		super(graphicsConfiguration);
		Handler.install();
		JTextPane textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPane.setText(message);
		add(textPane);
		setBounds(0, 0, 300, 92);
	}
}
