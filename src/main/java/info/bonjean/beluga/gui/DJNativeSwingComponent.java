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
package info.bonjean.beluga.gui;

import info.bonjean.beluga.Main;
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.log.Logger;
import info.bonjean.beluga.player.VLCPlayer;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.statefull.BelugaState;
import info.bonjean.beluga.util.HTMLUtil;
import info.bonjean.beluga.util.HTTPUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class DJNativeSwingComponent extends JPanel
{
	private static final long serialVersionUID = -664565924437929082L;
	private static final PandoraClient pandoraClient = PandoraClient.getInstance();
	private static final BelugaState state = BelugaState.getInstance();
	private static final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private static final Logger log = new Logger(Main.class);

	private static final JWebBrowser webBrowser = new JWebBrowser();
	private static final VLCPlayer player = VLCPlayer.getInstance();
	private static Song displayedSong;

	public DJNativeSwingComponent()
	{
		super(new BorderLayout());

		webBrowser.setBarsVisible(false);
		webBrowser.setStatusBarVisible(false);
		webBrowser.setDefaultPopupMenuRegistered(false); // disable right click
		
		webBrowser.addWebBrowserListener(new WebBrowserAdapter()
		{
			@Override
			public void commandReceived(WebBrowserCommandEvent e)
			{
				String command = e.getCommand();
				log.info("Received command: " + command);

				try
				{
					if (command.equals("next"))
					{
						nextSong();
						updateSongUI();

					} else if (command.equals("like"))
					{
						boolean positive = true;
						if(state.getSong().getSongRating() > 0)
							positive = false;
						pandoraClient.addFeedback(positive);
						updateSongUI();

					} else if (command.equals("ban"))
					{
						pandoraClient.addFeedback(false);
						nextSong();
						updateSongUI();

					} else if (command.equals("sleep"))
					{
						pandoraClient.sleepSong();
						nextSong();
						updateSongUI();

					} else if (command.equals("pause"))
					{
						player.togglePause();

					} else if (command.equals("exit"))
					{
						System.exit(0);

					} else if (command.equals("reload"))
					{
						updateSongUI();
						
					} else if (command.equals("configuration"))
					{
						log.info("Update configuration");
						Object[] parameters = e.getParameters();
						configuration.setUserName((String) parameters[0]);
						configuration.setPassword((String) parameters[1]);
						configuration.setProxyServer((String) parameters[2]);
						configuration.setProxyServerPort((String) parameters[3]);
						configuration.store();
						
						try
						{
							pandoraClient.login();
							gotoSongUI();
				
						} catch (Exception e1)
						{
							log.error(e1.getMessage());
							gotoConfigurationUI();
						}

					} else if (command.startsWith("stationSelect"))
					{
						Map<String, String> parameters = HTTPUtil.parseUrl(command);
						String stationId = parameters.get("stationId");
						log.info("Select station with id: " + stationId);

						pandoraClient.selectStation(stationId);
						nextSong();
						updateSongUI();
					}
				} catch (Exception e1)
				{
					log.error(e1.toString());
				}
			}
		});

		webBrowser.setJavascriptEnabled(true);
		add(webBrowser, BorderLayout.CENTER);
	}

	private static void updateSongUI()
	{
		webBrowser.setHTMLContent(HTMLUtil.getSong());
		displayedSong = state.getSong();
	}
	
	private static void updateWelcomeUI()
	{
		webBrowser.setHTMLContent(HTMLUtil.getWelcome());
	}
	
	private static void updateConfigurationUI()
	{
		webBrowser.setHTMLContent(HTMLUtil.getConfiguration());
	}

	private static void nextSong()
	{
		try
		{
			String url = pandoraClient.nextSong();
			displayedSong = state.getSong();
			log.info("Playing: " + url);
			player.play(url);

		} catch (Exception e)
		{
			log.error(e.getMessage());
		}
	}
	
	public static void gotoSongUI()
	{
		try
		{
			pandoraClient.updateStationList();
			pandoraClient.selectStation(state.getStationList().get(0));

		} catch (Exception e)
		{
			log.error(e.getMessage());
			System.exit(-1);
		}
		
		nextSong();
		
		updateSongUI();
		
		ActionListener taskPerformer = new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if (!displayedSong.getTrackToken().equals(state.getSong().getTrackToken()))
				{
					log.info("Song changed, update main window");
					updateSongUI();
				}
				if (player.isPlaying())
					webBrowser.executeJavascript("updateTime('" + player.getProgression() + "')");
			}
		};
		new Timer(1000, taskPerformer).start();
	}
	
	public static void gotoConfigurationUI() {
		updateConfigurationUI();
	}

	public static void start()
	{
		UIUtils.setPreferredLookAndFeel();
		NativeInterface.open();

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame("Beluga Player");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().add(new DJNativeSwingComponent(), BorderLayout.CENTER);
				frame.setSize(550, 400);
				frame.setResizable(false);
				frame.setLocationByPlatform(true);
				frame.setVisible(true);
				updateWelcomeUI();
				
				try
				{
					pandoraClient.login();
					gotoSongUI();
		
				} catch (Exception e)
				{
					log.error(e.getMessage());
					gotoConfigurationUI();
				}
			}
		});

		NativeInterface.runEventPump();
	}
}
