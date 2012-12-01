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

import static info.bonjean.beluga.util.I18NUtil._;
import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.connection.BelugaHTTPClient;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.exception.CryptoException;
import info.bonjean.beluga.exception.InternalException;
import info.bonjean.beluga.exception.PandoraError;
import info.bonjean.beluga.exception.PandoraException;
import info.bonjean.beluga.gui.notification.Notification;
import info.bonjean.beluga.player.VLCPlayer;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.util.HTMLUtil;
import info.bonjean.beluga.util.HTTPUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class UI extends JPanel
{
	private static final Logger log = LoggerFactory.getLogger(JPanel.class);
	private static final long serialVersionUID = -4119211066130381277L;
	private final JWebBrowser webBrowser = new JWebBrowser(JWebBrowser.useWebkitRuntime());
	private final BelugaState state = BelugaState.getInstance();
	private final PandoraClient pandoraClient = PandoraClient.getInstance();
	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private final VLCPlayer player = VLCPlayer.getInstance();
	private Song displayedSong;
	private Timer timer;
	private int retryCount = 0;
	private final int MAX_RETRIES = 3;

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
		webBrowser.addWebBrowserListener(new WebBrowserAdapter()
		{
			@Override
			public void commandReceived(WebBrowserCommandEvent webBrowserCommandEvent)
			{
				dispatch(webBrowserCommandEvent.getCommand(), webBrowserCommandEvent.getParameters());
			}
		});
		add(webBrowser, BorderLayout.CENTER);

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
						try
						{
							updateUI(Page.SONG);
						} catch (InternalException e)
						{
							log.error("A bug occured, please report this: ", e);
							System.exit(-1);
						}
					}
					webBrowser.executeJavascript("updateTime('" + player.getProgression() + "')");
				}
			}
		});
	}

	public void displayLoader()
	{
		webBrowser.executeJavascript("displayLoader()");
	}

	public void hideLoader()
	{
		webBrowser.executeJavascript("hideLoader()");
	}

	public void updateUI(Page page, Page pageBack) throws InternalException
	{
		state.setPage(page);
		webBrowser.setHTMLContent(HTMLUtil.getPageHTML(page, pageBack));
	}

	public void updateUI(Page page) throws InternalException
	{
		updateUI(page, null);
	}

	private void nextSong() throws BelugaException
	{
		String url = pandoraClient.nextSong();
		new Notification(HTMLUtil.getPageHTML(Page.NOTIFICATION));
		displayedSong = state.getSong();
		log.debug("Playing: " + url);
		player.play(url);
		if (!timer.isRunning())
			timer.start();
	}

	public void dispatch(String command)
	{
		dispatch(command, new Object[] {});
	}

	public void dispatch(String command, Object[] parameters)
	{
		try
		{
			doDispatch(command, parameters);
			retryCount = 0;
		} catch (BelugaException e)
		{
			handleException(e, command, parameters);
		}
	}

	private void doDispatch(String fullCommand, Object[] postParameters) throws BelugaException
	{
		String[] fullCommandSplit = fullCommand.split("/");

		Command command = Command.fromString(fullCommandSplit[0]);
		String[] parameters = new String[fullCommandSplit.length - 1];

		// populate parameters passed through URL
		for (int i = 1; i < fullCommandSplit.length; i++)
			parameters[i - 1] = fullCommandSplit[i];

		switch (command)
		{
		case LOGIN:
			pandoraClient.login();
			pandoraClient.updateStationList();
			if (state.getStationList().isEmpty())
			{
				updateUI(Page.STATION_ADD);
				break;
			}
			nextSong();
			updateUI(Page.SONG);
			break;

		case NEXT:
			displayLoader();
			nextSong();
			updateUI(Page.SONG);
			break;

		case LIKE:
			displayLoader();
			boolean positive = state.getSong().getSongRating() > 0 ? false : true;
			if (positive)
				pandoraClient.addFeedback(true);
			else
				log.error("TBD: deleteFeedback");
			updateUI(Page.SONG);
			break;

		case BAN:
			displayLoader();
			pandoraClient.addFeedback(false);
			nextSong();
			updateUI(Page.SONG);
			break;

		case SLEEP:
			displayLoader();
			pandoraClient.sleepSong();
			nextSong();
			updateUI(Page.SONG);
			break;

		case PAUSE:
			player.togglePause();
			updateUI(Page.SONG);
			break;

		case EXIT:
			System.exit(0);
			break;

		case GOTO:
			displayLoader();
			Page page = Page.fromString(parameters[0]);
			if (page == null)
			{
				log.error("Unknow page " + page);
				break;
			}
			updateUI(page);
			break;

		case CONFIGURATION:
			displayLoader();
			updateUI(Page.CONFIGURATION);
			break;

		case SAVE_CONFIGURATION:
			displayLoader();
			log.debug("Update configuration");
			configuration.setUserName((String) postParameters[0]);
			configuration.setPassword((String) postParameters[1]);
			configuration.setProxyHost((String) postParameters[2]);
			configuration.setProxyPort((String) postParameters[3]);
			configuration.setDNSProxy((String) postParameters[4]);
			configuration.store();

			// reset the HTTP client to apply proxy changes
			BelugaHTTPClient.reset();
			log.debug("Redirect to login");
			dispatch("login");
			break;

		case SELECT_STATION:
			displayLoader();
			Map<String, String> params = HTTPUtil.parseUrl(fullCommand);
			String stationId = params.get("stationId");
			log.debug("Select station with id: " + stationId);
			pandoraClient.selectStation(stationId);
			nextSong();
			updateUI(Page.SONG);
			break;

		case SEARCH:
			if (postParameters.length != 1)
			{
				log.error("Invalid parameters received");
				break;
			}
			String resultsHTML = "";
			String query = (String) postParameters[0];
			log.debug("query: " + query);
			if (query.length() > 0)
			{
				pandoraClient.search(query);
				resultsHTML = StringEscapeUtils.escapeJavaScript(HTMLUtil.getSearchResultsHTML(pandoraClient.search(query)));
			}
			webBrowser.executeJavascript("document.getElementById('results').innerHTML = \"" + resultsHTML + "\"");
			hideLoader();
			break;

		case ADD_STATION:
			displayLoader();
			String type = parameters[0];
			String token = parameters[1];
			if (type.equals("search"))
			{
				log.debug("Add station from search results, token: " + token);
				pandoraClient.addStation(token);
			} else
			{
				log.debug("Add station from " + type + ", token: " + token);
				pandoraClient.addStation(type, token);
			}
			pandoraClient.updateStationList();
			if (state.getSong() == null)
				nextSong();
			updateUI(Page.SONG);
			break;

		case DELETE_STATION:
			displayLoader();
			pandoraClient.deleteStation();
			state.setStation(null);
			pandoraClient.updateStationList();
			nextSong();
			updateUI(Page.SONG);
			break;

		case CREATE_USER:
			displayLoader();
			log.info("Create user");
			pandoraClient.createUser((String) postParameters[0], (String) postParameters[1], (String) postParameters[2], (String) postParameters[3], (String) postParameters[4],
					(String) postParameters[5]);
			dispatch("configuration");
			break;

		default:
			log.info("Unknown command received: " + fullCommand);
		}
	}

	public static void reportError(String messageKey)
	{
		reportError(messageKey, false, true, null);
	}

	public static void reportFatalError(String messageKey, Exception e)
	{
		reportError(messageKey, true, true, e);
	}

	public static void reportError(String messageKey, boolean fatal, boolean reportUI, Exception e)
	{
		log.error(_(messageKey), e);
		if (reportUI)
			BelugaState.getInstance().addError(messageKey);
		if (fatal)
			System.exit(-1);
	}

	private void handleException(BelugaException e, String command, Object[] parameters)
	{
		if (retryCount == MAX_RETRIES)
		{
			reportFatalError("too.many.errrors", e);
		} else if (e instanceof CryptoException)
		{
			reportFatalError("crypto.related.problem", e);
		} else if (e instanceof CommunicationException)
		{
			if (state.getUserId() == null)
			{
				reportError("connection.to.pandora.failed.check.proxy");
				dispatch("configuration");
				return;
			}
			reportError("communication.problem");
			retryCount++;
			dispatch(command, parameters);
			return;
		} else if (e instanceof PandoraException)
		{
			PandoraException pe = (PandoraException) e;
			if (pe.getError() == PandoraError.INVALID_CREDENTIALS)
			{
				reportError("invalid.credentials");
				dispatch("configuration");
				return;
			}
			if (pe.getError() == PandoraError.LICENSING_RESTRICTIONS)
			{
				reportError("pandora.not.available.check.proxy");
				dispatch("configuration");
				return;
			}
			if (pe.getError() == PandoraError.INVALID_AUTH_TOKEN)
			{
				reportError("authentication.token.expired");
				dispatch("login");
				return;
			}
			if (state.getPage().equals(Page.USER_CREATE))
			{
				reportError(pe.getMessage());
				hideLoader();
				return;
			}
			reportError(e.getMessage());
			retryCount++;
			dispatch(command, parameters);
		}
		reportFatalError("a.bug.occured", e);
	}
}
