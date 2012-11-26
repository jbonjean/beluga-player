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

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.util.HTMLUtil;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class UI extends JPanel
{
	private static final long serialVersionUID = -4119211066130381277L;
	private final JWebBrowser webBrowser = new JWebBrowser(JWebBrowser.useWebkitRuntime());

	public JWebBrowser getWebBrowser()
	{
		return webBrowser;
	}

	public UI()
	{
		super(new BorderLayout());
		webBrowser.setBarsVisible(false);
		webBrowser.setStatusBarVisible(false);
		webBrowser.setDefaultPopupMenuRegistered(false);
		webBrowser.setJavascriptEnabled(true);
		add(webBrowser, BorderLayout.CENTER);
	}

	public void updateSongUI()
	{
		webBrowser.setHTMLContent(HTMLUtil.getSongHTML(BelugaState.getInstance().getStationList(), BelugaState.getInstance().getStation(), BelugaState.getInstance().getSong()));
	}

	public void updateWelcomeUI()
	{
		webBrowser.setHTMLContent(HTMLUtil.getWelcomeHTML());
	}

	public void updateConfigurationUI()
	{
		webBrowser.setHTMLContent(HTMLUtil.getConfigurationHTML());
	}

	public void updateStationAddUI()
	{
		webBrowser.setHTMLContent(HTMLUtil.getStationAddHTML());
	}

	public void displayLoader()
	{
		webBrowser.executeJavascript("displayLoader()");
	}
	
	public void hideLoader()
	{
		webBrowser.executeJavascript("hideLoader()");
	}
}
