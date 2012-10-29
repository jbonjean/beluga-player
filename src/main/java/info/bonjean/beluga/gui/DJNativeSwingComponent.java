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
		
//		webBrowser.getNativeComponent().addMouseListener(new MouseListener() {
//		
//		public void mouseReleased(MouseEvent e) {
//			log.info("mouseReleased");
//		}
//		
//		public void mousePressed(MouseEvent e) {
//			log.info("mousePressed");
//		}
//		
//		public void mouseExited(MouseEvent e) {
//			log.info("mouseExited");
//		}
//		
//		public void mouseEntered(MouseEvent e) {
//			log.info("mouseEntered");
//		}
//		
//		public void mouseClicked(MouseEvent e) {
//			log.info("mouseClicked");
//		}
//	});
		
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
						updateUIContent();

					} else if (command.equals("like"))
					{
						boolean positive = true;
						if(state.getSong().getSongRating() > 0)
							positive = false;
						pandoraClient.addFeedback(positive);
						updateUIContent();

					} else if (command.equals("ban"))
					{
						pandoraClient.addFeedback(false);
						nextSong();
						updateUIContent();

					} else if (command.equals("sleep"))
					{
						pandoraClient.sleepSong();
						nextSong();
						updateUIContent();

					} else if (command.equals("pause"))
					{
						player.togglePause();

					} else if (command.equals("exit"))
					{
						System.exit(0);

					} else if (command.equals("reload"))
					{
						updateUIContent();

					} else if (command.startsWith("stationSelect"))
					{
						Map<String, String> parameters = HTTPUtil.parseUrl(command);
						String stationId = parameters.get("stationId");
						log.info("Select station with id: " + stationId);

						pandoraClient.selectStation(stationId);
						nextSong();
						updateUIContent();
					}
				} catch (Exception e1)
				{
					log.error(e1.toString());
				}
			}
		});

		webBrowser.setJavascriptEnabled(true);
		updateUIContent();

		add(webBrowser, BorderLayout.CENTER);
	}

	private static void updateUIContent()
	{
		webBrowser.setHTMLContent(HTMLUtil.getSong());
		displayedSong = state.getSong();
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

	public static void start()
	{
		try
		{
			PandoraClient pandoraClient = PandoraClient.getInstance();
			pandoraClient.login();
			pandoraClient.updateStationList();
			pandoraClient.selectStation(state.getStationList().get(0));

			nextSong();

		} catch (Exception e)
		{
			log.error(e.getMessage());
			System.exit(-1);
		}

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
			}
		});

		ActionListener taskPerformer = new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if (!displayedSong.getTrackToken().equals(state.getSong().getTrackToken()))
				{

					log.info("Song changed, update main window");
					updateUIContent();
				}
				if (player.isPlaying())
					webBrowser.executeJavascript("updateTime('" + player.getProgression() + "')");
			}
		};
		new Timer(1000, taskPerformer).start();

		NativeInterface.runEventPump();
	}
}
