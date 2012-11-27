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
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.connection.BelugaHTTPClient;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.exception.CryptoException;
import info.bonjean.beluga.exception.PandoraError;
import info.bonjean.beluga.exception.PandoraException;
import info.bonjean.beluga.gui.notification.Notification;
import info.bonjean.beluga.player.VLCPlayer;
import info.bonjean.beluga.request.Method;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.util.HTMLUtil;
import info.bonjean.beluga.util.HTTPUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class UIBrowserListener extends WebBrowserAdapter
{
	private static final Logger log = LoggerFactory.getLogger(UIBrowserListener.class);

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
					if (state.getPage().equals(Page.SONG) && !displayedSong.getTrackToken().equals(state.getSong().getTrackToken()))
					{
						log.debug("Song changed, update main window");
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
		new Notification(HTMLUtil.getNotificationHTML(state.getSong()));
		displayedSong = state.getSong();
		log.debug("Playing: " + url);
		player.play(url);
		if (!timer.isRunning())
			timer.start();
	}

	@Override
	public void commandReceived(WebBrowserCommandEvent webBrowserCommandEvent)
	{
		String command = webBrowserCommandEvent.getCommand();
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
				ui.displayLoader();
				nextSong();
				ui.updateSongUI();

			} else if (command.equals("like"))
			{
				ui.displayLoader();
				boolean positive = true;
				if (state.getSong().getSongRating() > 0)
					positive = false;
				pandoraClient.addFeedback(positive);
				ui.updateSongUI();

			} else if (command.equals("ban"))
			{
				ui.displayLoader();
				pandoraClient.addFeedback(false);
				nextSong();
				ui.updateSongUI();

			} else if (command.equals("sleep"))
			{
				ui.displayLoader();
				pandoraClient.sleepSong();
				nextSong();
				ui.updateSongUI();

			} else if (command.equals("pause"))
			{
				player.togglePause();

			} else if (command.equals("exit"))
			{
				System.exit(0);

			} else if (command.startsWith("goto/"))
			{
				ui.displayLoader();
				
				String page = command.split("/")[1];
				log.info("Goto page " + page);

				if (page.equals(Page.WELCOME.name()))
					ui.updateWelcomeUI();
				else if (page.equals(Page.CONFIGURATION.name()))
					ui.updateConfigurationUI(true);
				else if (page.equals(Page.SONG.name()))
					ui.updateSongUI();
				else if (page.equals(Page.STATION_ADD.name()))
					ui.updateStationAddUI();
				else if (page.equals(Page.USER_CREATE.name()))
					ui.updateUserCreateUI();
				else
					log.error("Unknow page " + page);

			} else if (command.equals("configuration"))
			{
				ui.displayLoader();
				Object[] parameters = webBrowserCommandEvent.getParameters();
				if (parameters.length > 0)
					state.addError((String) parameters[0]);
				ui.updateConfigurationUI(false);

			} else if (command.equals("save-configuration"))
			{
				ui.displayLoader();
				log.info("Update configuration");
				Object[] parameters = webBrowserCommandEvent.getParameters();
				configuration.setUserName((String) parameters[0]);
				configuration.setPassword((String) parameters[1]);
				configuration.setProxyServer((String) parameters[2]);
				configuration.setProxyServerPort((String) parameters[3]);
				configuration.setProxyDNS((String) parameters[4]);
				configuration.store();

				// reset the HTTP client to apply proxy changes
				BelugaHTTPClient.reset();
				log.info("Redirect to login");
				commandReceived(new WebBrowserCommandEvent(ui.getWebBrowser(), "login", new Object[] {}));

			} else if (command.startsWith("select-station"))
			{
				ui.displayLoader();
				Map<String, String> parameters = HTTPUtil.parseUrl(command);
				String stationId = parameters.get("stationId");
				log.info("Select station with id: " + stationId);

				pandoraClient.selectStation(stationId);
				nextSong();
				ui.updateSongUI();
			} else if (command.equals("search"))
			{
				Object[] parameters = webBrowserCommandEvent.getParameters();
				if (parameters.length > 0)
				{
					String resultsHTML = "";
					String query = (String) webBrowserCommandEvent.getParameters()[0];
					log.debug("query: " + query);

					if (query.length() > 0)
					{
						pandoraClient.search(query);
						resultsHTML = HTMLUtil.generateSearchResultsHTML(pandoraClient.search(query));
					}

					ui.getWebBrowser().executeJavascript("document.getElementById('results').innerHTML = \"" + resultsHTML + "\"");
					ui.hideLoader();
				}
			} else if (command.startsWith("add-station/"))
			{
				ui.displayLoader();
				String[] parameters = command.split("/");
				String type = parameters[1];
				String token = parameters[2];
				if(type.equals("search"))
				{
					log.debug("Add station from search results, token: " + token);
					pandoraClient.addStation(token);
				}
				else
				{
					log.debug("Add station from "+ type +", token: " + token);
					pandoraClient.addStation(type, token);
				}
				pandoraClient.updateStationList();
				ui.updateSongUI();
			} else if (command.equals("delete-station"))
			{
				ui.displayLoader();
				pandoraClient.deleteStation();
				pandoraClient.updateStationList();
				pandoraClient.selectStation(state.getStationList().get(0));
				nextSong();
				ui.updateSongUI();
			} else if (command.equals("create-user"))
			{
				ui.displayLoader();
				log.info("Create user");
				Object[] parameters = webBrowserCommandEvent.getParameters();
				pandoraClient.createUser((String) parameters[0], (String) parameters[1], (String) parameters[2], (String) parameters[3], (String) parameters[4], (String) parameters[5]);
				commandReceived(new WebBrowserCommandEvent(ui.getWebBrowser(), "configuration", new Object[]{}));
			} else
			{
				log.info("Unknown command received: " + command);
			}
		} catch (BelugaException e)
		{
			if (retryCount == MAX_RETRIES)
			{
				log.error("Too many errors occured");

			} else if (e instanceof CryptoException)
			{
				log.error("Crypto related problem, cannot recover");

			} else if (e instanceof CommunicationException)
			{
				if (state.getUserId() == null)
				{
					log.error("Communication problem before login, this is probably proxy related", e);
					commandReceived(new WebBrowserCommandEvent(ui.getWebBrowser(), "configuration", new Object[] { "connection.to.pandora.failed.check.proxy" }));
					return;
				}
				log.error("Communication problem, let's retry", e);
				retryCount++;
				commandReceived(webBrowserCommandEvent);
				return;
			} else if (e instanceof PandoraException)
			{
				PandoraException pe = (PandoraException) e;
				if (pe.getError() == PandoraError.UNKNOWN && pe.getMethod() == Method.USER_LOGIN)
				{
					log.error("Invalid credentials, redirect to configuration");
					commandReceived(new WebBrowserCommandEvent(ui.getWebBrowser(), "configuration", new Object[] { "invalid.credentials" }));
					return;
				}
				if (pe.getError() == PandoraError.LICENSING_RESTRICTIONS)
				{
					log.error("Pandora is not available in your country, you should consider using a proxy or custom DNS");
					commandReceived(new WebBrowserCommandEvent(ui.getWebBrowser(), "configuration", new Object[] { "pandora.not.available.check.proxy" }));
					return;
				}
				if (state.getPage().equals(Page.USER_CREATE))
				{
					ui.hideLoader();
					return;
				}
				log.error(e.toString(), e);
				retryCount++;
				commandReceived(webBrowserCommandEvent);
			}

			log.error("A bug occured, please report this: ", e);
			System.exit(-1);
		}
		retryCount = 0;
	}
}
