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

import info.bonjean.beluga.exception.InternalException;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class UIWindowListener implements WindowListener
{
	private static final Logger log = LoggerFactory.getLogger(UIWindowListener.class);
	private UI ui;
	private UIBrowserListener browserListener;

	public UIWindowListener(UI ui, UIBrowserListener browserListener)
	{
		this.ui = ui;
		this.browserListener = browserListener;
	}

	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowClosed(WindowEvent e)
	{
	}

	@Override
	public void windowClosing(WindowEvent e)
	{
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
	}

	@Override
	public void windowOpened(WindowEvent e)
	{
		try
		{
			ui.updateUI(Page.WELCOME);
		} catch (InternalException e1)
		{
			log.error("A bug occured, please report this: ", e);
			System.exit(-1);
		}
		browserListener.commandReceived(new WebBrowserCommandEvent(ui.getWebBrowser(), "login", new Object[] {}));
	}
}
