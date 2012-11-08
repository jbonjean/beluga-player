/* Copyright (C) 2012 Julien Bonjean <julien@bonjean.info>
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.gui.UIBrowserListener;
import info.bonjean.beluga.gui.UI;
import info.bonjean.beluga.gui.UIWindowListener;
import info.bonjean.beluga.log.Logger;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class Main
{
	@SuppressWarnings("unused")
	private static final Logger log = new Logger(Main.class);

	public static void main(String[] args) throws IOException
	{
		BelugaConfiguration configuration = BelugaConfiguration.getInstance();
		configuration.load();
		startUI();
	}
	
	public static void startUI()
	{
		UIUtils.setPreferredLookAndFeel();
		NativeInterface.open();

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame("Beluga Player");
				UI ui = new UI();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().add(ui, BorderLayout.CENTER);
				frame.setSize(550, 400);
				frame.setResizable(false);
				frame.setLocationByPlatform(true);
				frame.setVisible(true);
				
				UIBrowserListener browserListener = new UIBrowserListener(ui);
				ui.getWebBrowser().addWebBrowserListener(browserListener);
				UIWindowListener windowListener = new UIWindowListener(ui, browserListener);
				frame.addWindowListener(windowListener);
			}
		});
		NativeInterface.runEventPump();
	}
}
