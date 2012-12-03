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
package info.bonjean.beluga;

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.gui.Page;
import info.bonjean.beluga.gui.UI;
import info.bonjean.beluga.gui.UIWindowListener;

import java.awt.Image;
import java.awt.Toolkit;

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
	public static void main(String[] args)
	{
		if (args.length == 1 && args[0].equals("-version"))
		{
			System.out.println("Beluga Player 0.3");
			System.exit(0);
		}

		BelugaConfiguration.getInstance().load();
		startUI();
	}

	public static void startUI()
	{
		UIUtils.setPreferredLookAndFeel();
		NativeInterface.open();

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				JFrame frame = new JFrame("Beluga Player");
				frame.setLayout(null);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(600, 410);
				frame.setResizable(false);
				frame.setLocationByPlatform(true);
				frame.setVisible(true);
				Image image = Toolkit.getDefaultToolkit().getImage(Main.class.getResource(Page.IMG_PATH + "beluga.40x40.png"));
				frame.setIconImage(image);

				final UI ui = new UI(frame);
				UIWindowListener windowListener = new UIWindowListener(ui);
				frame.addWindowListener(windowListener);
			}
		});
		NativeInterface.runEventPump();
	}
}
