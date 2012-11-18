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

import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.exception.CryptoException;
import info.bonjean.beluga.exception.PandoraError;
import info.bonjean.beluga.exception.PandoraException;
import info.bonjean.beluga.log.Logger;
import info.bonjean.beluga.player.VLCPlayer;
import info.bonjean.beluga.request.Method;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.statefull.BelugaState;
import info.bonjean.beluga.util.HTTPUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.Timer;

import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class UIBrowserListener extends WebBrowserAdapter
{
	private static final Logger log = new Logger(UIBrowserListener.class);

	private UI ui;
	private Song displayedSong;

	private final PandoraClient pandoraClient = PandoraClient.getInstance();
	private final BelugaState state = BelugaState.getInstance();
	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private final VLCPlayer player = VLCPlayer.getInstance();
	private Timer timer;
	private int retryCount = 0;
	private final int MAX_RETRIES = 3;

	public UIBrowserListener(final UI ui)
	{
		this.ui = ui;
		timer = new Timer(1000, new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if (player.isPlaying())
				{
					if (!displayedSong.getTrackToken().equals(state.getSong().getTrackToken()))
					{
						log.info("Song changed, update main window");
						displayedSong = state.getSong();
						ui.updateSongUI();
					}
					ui.getWebBrowser().executeJavascript("updateTime('" + player.getProgression() + "')");
				}
			}
		});
	}

	private void nextSong() throws BelugaException
	{
		String url = pandoraClient.nextSong();
		displayedSong = state.getSong();
		log.info("Playing: " + url);
		player.play(url);
		if (!timer.isRunning())
			timer.start();
	}

	@Override
	public void commandReceived(WebBrowserCommandEvent webBrowserCommandEvent)
	{
		String command = webBrowserCommandEvent.getCommand();
		log.info("Received command: " + command);

		try
		{
			if (command.equals("login"))
			{
				pandoraClient.login();
				pandoraClient.updateStationList();
				pandoraClient.selectStation(state.getStationList().get(0));
				nextSong();
				ui.updateSongUI();

			} else if (command.equals("next"))
			{
				nextSong();
				ui.updateSongUI();

			} else if (command.equals("like"))
			{
				boolean positive = true;
				if (state.getSong().getSongRating() > 0)
					positive = false;
				pandoraClient.addFeedback(positive);
				ui.updateSongUI();

			} else if (command.equals("ban"))
			{
				pandoraClient.addFeedback(false);
				nextSong();
				ui.updateSongUI();

			} else if (command.equals("sleep"))
			{
				pandoraClient.sleepSong();
				nextSong();
				ui.updateSongUI();

			} else if (command.equals("pause"))
			{
				player.togglePause();

			} else if (command.equals("exit"))
			{
				System.exit(0);

			} else if (command.startsWith("reload/"))
			{
				String page = command.split("/")[1];
				log.info("Reload page " + page);
				
				if(page.equals(Page.WELCOME.name()))
					ui.updateWelcomeUI();
				else if(page.equals(Page.CONFIGURATION.name()))
					ui.updateConfigurationUI();
				else if(page.equals(Page.SONG.name()))
					ui.updateSongUI();
				else
					log.error("Unknow page " + page);

			} else if (command.equals("configuration"))
			{
				ui.updateConfigurationUI();

			} else if (command.equals("save-configuration"))
			{
				log.info("Update configuration");
				Object[] parameters = webBrowserCommandEvent.getParameters();
				configuration.setUserName((String) parameters[0]);
				configuration.setPassword((String) parameters[1]);
				configuration.setProxyServer((String) parameters[2]);
				configuration.setProxyServerPort((String) parameters[3]);
				configuration.store();
				log.info("Redirect to login");
				commandReceived(new WebBrowserCommandEvent(ui.getWebBrowser(), "login", new Object[] {}));

			} else if (command.startsWith("stationSelect"))
			{
				Map<String, String> parameters = HTTPUtil.parseUrl(command);
				String stationId = parameters.get("stationId");
				log.info("Select station with id: " + stationId);

				pandoraClient.selectStation(stationId);
				nextSong();
				ui.updateSongUI();
			}
		} catch (BelugaException e)
		{
			if (retryCount == MAX_RETRIES)
			{
				log.error("Too many errors occured");
				
			} else if (e instanceof CryptoException)
			{
				log.error("Crypto related problem (any help is welcome to fix this bug), let's retry");
				retryCount++;
				commandReceived(webBrowserCommandEvent);
				return;
			} else if (e instanceof CommunicationException)
			{
				log.error("Communication problem, let's retry");
				retryCount++;
				commandReceived(webBrowserCommandEvent);
				return;
			} else if (e instanceof PandoraException)
			{
				PandoraException pe = (PandoraException) e;
				if (pe.getError() == PandoraError.UNKNOWN && pe.getMethod() == Method.USER_LOGIN)
				{
					log.error("Invalid credentials, redirect to configuration");
					commandReceived(new WebBrowserCommandEvent(ui.getWebBrowser(), "configuration", new Object[] {}));
					return;
				}
			}

			e.printStackTrace();
			System.exit(-1);
		}
		retryCount = 0;
	}
}
