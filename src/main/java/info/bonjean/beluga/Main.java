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
package info.bonjean.beluga;

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.gui.WebkitUI;
import info.bonjean.beluga.gui.webkit.Page;
import info.bonjean.beluga.gui.webkit.UIWindowListener;
import info.bonjean.beluga.player.JLayer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Mixer;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.Player;
import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 *         Use the option -Ddebug=1 to run in debug mode.
 * 
 */
public class Main
{
	public static void main(String[] args)
	{
		String version = Main.class.getPackage().getImplementationVersion();
		if (version == null)
			version = "(dev)";

		System.out.println("Beluga Player " + version);
		if (args.length == 1 && args[0].equals("-version"))
		{
			System.exit(0);
		}

		BelugaConfiguration.getInstance().load();
		BelugaState.getInstance().setVersion(version);

		// set the proxy parameters for webkit before it is instanciated
		if (BelugaConfiguration.getInstance().getDNSProxyWebkit())
		{
			System.setProperty("network.proxy_host", BelugaConfiguration.getInstance().getDNSProxy());
			System.setProperty("network.proxy_port", "80");
		}

		// startWebkitUI();
		startPivotUI();
	}

	public static void startPivotUI()
	{
		PivotUI.startDesktopUI();
	}

	public static void startWebkitUI()
	{
		UIUtils.setPreferredLookAndFeel();
		NativeInterface.open();

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				JFrame frame = new JFrame("Beluga Player");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				Image image = Toolkit.getDefaultToolkit().getImage(Main.class.getResource(Page.IMG_PATH + "beluga.40x40.png"));
				frame.setIconImage(image);
				frame.setResizable(false);
				frame.setLocationByPlatform(true);
				frame.setVisible(true);

				Container contentPane = frame.getContentPane();
				contentPane.setLayout(new BorderLayout());

				final WebkitUI ui = new WebkitUI(frame);
				UIWindowListener windowListener = new UIWindowListener(ui);
				frame.addWindowListener(windowListener);
				ui.getWebBrowser().getNativeComponent().addMouseListener(windowListener);
			}
		});
		NativeInterface.runEventPump();
	}
}
