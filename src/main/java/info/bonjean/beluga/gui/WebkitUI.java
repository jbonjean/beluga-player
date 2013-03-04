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
package info.bonjean.beluga.gui;

import static info.bonjean.beluga.util.I18NUtil._;
import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.connection.BelugaHTTPClient;
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.exception.CryptoException;
import info.bonjean.beluga.exception.InternalException;
import info.bonjean.beluga.exception.PandoraError;
import info.bonjean.beluga.exception.PandoraException;
import info.bonjean.beluga.gui.notification.Notification;
import info.bonjean.beluga.gui.webkit.Command;
import info.bonjean.beluga.gui.webkit.Page;
import info.bonjean.beluga.response.Feedback;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.response.Station;
import info.bonjean.beluga.util.HTMLUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.swing.JFrame;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.client.methods.HttpGet;
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
public class WebkitUI
{
	private static final Logger log = LoggerFactory.getLogger(WebkitUI.class);
	private static JWebBrowser webBrowser;
	private static JWebBrowser playerWebBrowser;
	private final BelugaState state = BelugaState.getInstance();
	private final PandoraClient pandoraClient = PandoraClient.getInstance();
	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private int retryCount = 0;
	private final int MAX_RETRIES = 3;

	public JWebBrowser getWebBrowser()
	{
		return webBrowser;
	}

	public WebkitUI(JFrame frame)
	{
		webBrowser = new JWebBrowser(JWebBrowser.useWebkitRuntime());
		webBrowser.setVisible(true);
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

		playerWebBrowser = new JWebBrowser(JWebBrowser.useWebkitRuntime());
		playerWebBrowser.setVisible(true);
		playerWebBrowser.setBarsVisible(false);
		playerWebBrowser.setStatusBarVisible(false);
		playerWebBrowser.setDefaultPopupMenuRegistered(false);
		playerWebBrowser.setJavascriptEnabled(true);
		playerWebBrowser.addWebBrowserListener(new WebBrowserAdapter()
		{
			@Override
			public void commandReceived(WebBrowserCommandEvent webBrowserCommandEvent)
			{
				dispatch(webBrowserCommandEvent.getCommand(), webBrowserCommandEvent.getParameters());
			}
		});

		webBrowser.setPreferredSize(new Dimension(600, 325));
		playerWebBrowser.setPreferredSize(new Dimension(600, 60));

		frame.add(webBrowser, BorderLayout.CENTER);
		frame.add(playerWebBrowser, BorderLayout.SOUTH);

		frame.pack();
	}

	public void displayLoader()
	{
		webBrowser.executeJavascript("$.noty.closeAll()");
		webBrowser.executeJavascript("displayLoader()");
	}

	public void hideLoader()
	{
		webBrowser.executeJavascript("$.noty.closeAll()");
		webBrowser.executeJavascript("hideLoader()");
	}

	public void updateAudioUI() throws InternalException
	{
		playerWebBrowser.setHTMLContent(HTMLUtil.getPageHTML(Page.AUDIO));
	}

	public void updateUI(Page page) throws InternalException
	{
		Page pageBack = null;
		if (page.equals(Page.CONFIGURATION))
		{
			if (pandoraClient.isLoggedIn())
				pageBack = Page.SONG;
		}
		else if (!page.equals(Page.SONG))
		{
			if (state.getPage() == page)
				pageBack = state.getPageBack();
			else
				pageBack = state.getPage();
		}
		if (state.getStationList().isEmpty() && pageBack == Page.SONG)
			pageBack = null;
		if (pageBack == Page.WELCOME)
			pageBack = null;

		state.setPageBack(pageBack);
		state.setPage(page);
		webBrowser.setHTMLContent(HTMLUtil.getPageHTML(page, pageBack));
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
		}
		catch (Exception e)
		{
			handleException(e, command, parameters);
		}
	}

	private void doDispatch(String fullCommand, Object[] postParameters) throws Exception
	{
		String[] fullCommandSplit = fullCommand.split("/");

		Command command = Command.fromString(fullCommandSplit[0]);
		String[] parameters = new String[fullCommandSplit.length - 1];

		// populate parameters passed through URL
		for (int i = 1; i < fullCommandSplit.length; i++)
			parameters[i - 1] = fullCommandSplit[i];

		// this is a big exclusion list, not very pretty but it ensure that
		// default behaviour is safety check
		if (pandoraClient.isLoggedIn() && !command.equals(Command.LOGIN) && !command.equals(Command.BOOKMARK)
				&& !command.equals(Command.DELETE_FEEDBACK) && !command.equals(Command.AUDIO_ERROR) && !command.equals(Command.SEARCH)
				&& !command.equals(Command.STORE_VOLUME) && !command.equals(Command.ADD_STATION) && !command.equals(Command.EXIT)
				&& !command.equals(Command.SELECT_STATION)
				&& !(command.equals(Command.GOTO) && !(parameters[0].equals("song") || parameters[0].equals("station-details"))))
		{
			displayLoader();

			String currentStationId = state.getStation() == null ? null : state.getStation().getStationId();
			String selectedStationId = currentStationId == null ? configuration.getDefaultStationId() : currentStationId;

			// update station list
			state.setStationList(pandoraClient.getStationList());

			// if no station, go to station creation page
			if (state.getStationList().isEmpty())
			{
				updateUI(Page.STATION_ADD);
				return;
			}

			// select station
			state.setStation(null);
			if (selectedStationId == null)
				state.setStation(state.getStationList().get(0));
			else
			{
				for (Station station : state.getStationList())
				{
					if (station.getStationId().equals(selectedStationId))
					{
						state.setStation(station);
						break;
					}
				}
				if (state.getStation() == null)
					state.setStation(state.getStationList().get(0));
			}

			// retrieve station full information
			reportInfo("retrieving.station.data");
			state.setStation(pandoraClient.getStation(state.getStation()));

			// station changed
			if (!state.getStation().getStationId().equals(currentStationId))
			{
				// if station changed, reset playlist
				state.setPlaylist(null);

				// update the configuration
				configuration.setDefaultStationId(state.getStation().getStationId());
				configuration.store();

				// and prevent delete, the station does not exist anymore!
				if (command.equals(Command.DELETE_STATION))
					command = Command.NEXT;

			}
			if (state.getPlaylist() == null || state.getPlaylist().isEmpty())
			{
				// retrieve playlist from Pandora
				reportInfo("retrieving.playlist");
				state.setPlaylist(pandoraClient.getPlaylist(state.getStation()));

				// update extra information
				reportInfo("retrieving.song.extra.information");
				for (Song song : state.getPlaylist())
					song.setFocusTraits(pandoraClient.retrieveFocusTraits(song));

				// retrieve covers
				reportInfo("retrieving.album.covers");
				for (Song song : state.getPlaylist())
					song.setAlbumArtBase64(pandoraClient.retrieveCover(song.getAlbumArtUrl()));
			}

			// check song
			if (state.getSong() == null || command.equals(Command.NEXT))
			{
				state.setSong(state.getPlaylist().get(0));
				state.getPlaylist().remove(state.getSong());
				log.info(state.getSong().getAudioUrlMap().get("lowQuality").getAudioUrl());
				new Notification(HTMLUtil.getPageHTML(Page.NOTIFICATION));

				new Thread()
				{
					@Override
					public void run()
					{
						try
						{
							InputStream in = BelugaHTTPClient.getInstance().httpRequest(
									new HttpGet(state.getSong().getAudioUrlMap().get("lowQuality").getAudioUrl()));
							//String tDir = System.getProperty("java.io.tmpdir");
							FileOutputStream out = new FileOutputStream("/tmp/beluga.aac");
							int read = 0;
							long total = 0;
							byte[] bytes = new byte[1024];
							while ((read = in.read(bytes)) != -1)
							{
								out.write(bytes, 0, read);
								total += read;
							}
							in.close();
							out.flush();
							out.close();
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}.start();
				Thread.currentThread().sleep(5000);

				updateAudioUI();
			}
		}
		else if (command.equals(Command.GOTO) && parameters[0].equals("bookmarks"))
		{
			displayLoader();
			reportInfo("retrieving.bookmarks");
			state.setBookmarks(pandoraClient.getBookmarks());
		}

		switch (command)
		{
			case LOGIN:
				updateUI(Page.WELCOME);
				pandoraClient.partnerLogin();
				pandoraClient.userLogin();
				state.reset();
				updateUI(Page.WELCOME);
				dispatch("goto/song");
				return;

			case NEXT:
				break;

			case LIKE:
				if (state.getSong().getSongRating() < 1)
					pandoraClient.addFeedback(state.getSong(), true);
				dispatch("goto/song");
				reportSuccess("feedback.sent");
				return;

			case DELETE_FEEDBACK:
				pandoraClient.deleteFeedback(parameters[0]);
				updateSongFeedback(parameters[0]);
				dispatch("goto/station-details");
				reportSuccess("feedback.deleted");
				return;

			case DELETE_BOOKMARK:
				displayLoader();
				String deleteBookmarkType = parameters[0];
				String deleteBookmarkToken = parameters[1];
				if (deleteBookmarkType.equals("artist"))
					pandoraClient.deleteArtistBookmark(deleteBookmarkToken);
				else
					pandoraClient.deleteSongBookmark(deleteBookmarkToken);
				hideLoader();
				reportSuccess("bookmark.deleted");
				return;

			case BAN:
				displayLoader();
				pandoraClient.addFeedback(state.getSong(), false);
				state.setSong(null);
				dispatch("next");
				return;

			case SLEEP:
				displayLoader();
				pandoraClient.sleepSong(state.getSong());
				state.setSong(null);
				dispatch("next");
				return;

			case BOOKMARK:
				displayLoader();
				String bookmarkType = parameters[0];
				if (bookmarkType.equals("artist"))
					pandoraClient.addArtistBookmark(state.getSong().getTrackToken());
				else
					pandoraClient.addSongBookmark(state.getSong().getTrackToken());
				hideLoader();
				reportSuccess("bookmark.created");
				return;

			case EXIT:
				System.exit(0);
				return;

			case GOTO:
				Page page = Page.fromString(parameters[0]);
				if (page == null)
				{
					log.error("Unknow page " + page);
					break;
				}
				if (page.equals(Page.AUDIO))
				{
					updateAudioUI();
				}
				else
					updateUI(page);
				break;

			case SAVE_CONFIGURATION:
				displayLoader();
				log.debug("Update configuration");
				configuration.setUserName((String) postParameters[0]);
				configuration.setPassword((String) postParameters[1]);
				configuration.setProxyHost((String) postParameters[2]);
				configuration.setProxyPort((String) postParameters[3]);
				configuration.setDNSProxy((String) postParameters[4]);
				configuration.setDNSProxyWebkit((Boolean) postParameters[5]);
				configuration.setThemeId((String) postParameters[6]);
				configuration.store();

				// reset the HTTP client to apply proxy changes
				BelugaHTTPClient.reset();
				log.debug("Redirect to login");
				dispatch("login");
				return;

			case SELECT_STATION:
				displayLoader();
				log.debug("Select station with id: " + parameters[0]);
				configuration.setDefaultStationId(parameters[0]);
				state.setStation(null);
				dispatch("next");
				return;

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
				break;

			case ADD_STATION:
				String type = parameters[0];
				String token = parameters[1];
				if (type.equals("search"))
				{
					log.debug("Add station from search results, token: " + token);
					pandoraClient.addStation(token);
				}
				else
				{
					log.debug("Add station from " + type + ", token: " + token);
					pandoraClient.addStation(type, token);
				}
				dispatch("goto/song");
				reportSuccess("station.created");
				return;

			case DELETE_STATION:
				pandoraClient.deleteStation(state.getStation());
				dispatch("next");
				reportSuccess("station.deleted");
				return;

			case CREATE_USER:
				displayLoader();
				log.info("Create user");
				pandoraClient.createUser((String) postParameters[0], (String) postParameters[1], (String) postParameters[2],
						(String) postParameters[3], (String) postParameters[4], (String) postParameters[5]);
				dispatch("configuration");
				return;

			case STORE_VOLUME:
				log.debug("Store volume=" + parameters[0] + ", muted=" + parameters[1]);
				state.setVolume(Float.parseFloat(parameters[0]));
				state.setMutedVolume(Float.parseFloat(parameters[1]));
				return;

			case AUDIO_ERROR:
				reportFatalError("audio.player.error", null);
				return;

			default:
				log.info("Unknown command received: " + fullCommand);
		}

		// we are done, hide loader
		hideLoader();

		// always update song page
		if (state.getPage().equals(Page.SONG))
			updateUI(Page.SONG);

		// display errors
		for (String errorKey : state.getErrors())
			showError(errorKey);
		state.clearErrors();

		// reset loop protection
		retryCount = 0;
	}

	/**
	 * 
	 * If a feedback has been deleted, we check if it is the song currently playing
	 * 
	 */
	private void updateSongFeedback(String feedbackId)
	{
		if (state.getSong() == null)
			return;

		for (Feedback feedback : state.getStation().getFeedback().getThumbsUp())
		{
			if (feedback.getFeedbackId().equals(feedbackId))
			{
				// this is not bulletproof but should be good enough for 99% of cases.
				if (state.getSong().getArtistName().equals(feedback.getArtistName()) && state.getSong().getSongName().equals(feedback.getSongName()))
				{
					state.getSong().setSongRating(0);
					log.info("Current song feedback updated");
				}
				return;
			}
		}
	}

	public static void showError(String messageKey)
	{
		webBrowser.executeJavascript("showError('" + StringEscapeUtils.escapeJavaScript(_(messageKey)) + "')");
	}

	public static void reportSuccess(String infoKey)
	{
		webBrowser.executeJavascript("showSuccess('" + StringEscapeUtils.escapeJavaScript(_(infoKey)) + "')");
	}

	public static void reportInfo(String infoKey)
	{
		webBrowser.executeJavascript("showInfo('" + StringEscapeUtils.escapeJavaScript(_(infoKey)) + "')");
	}

	public static void reportError(String messageKey)
	{
		reportError(messageKey, false, true, false, null);
	}

	public static void reportError(String messageKey, boolean now)
	{
		reportError(messageKey, false, true, now, null);
	}

	public static void reportFatalError(String messageKey, Exception e)
	{
		reportError(messageKey, true, true, true, e);
	}

	public static void reportError(String messageKey, boolean fatal, boolean reportUI, boolean now, Exception e)
	{
		log.error(_(messageKey), e);
		if (reportUI)
		{
			if (now)
				showError(messageKey);
			else
				BelugaState.getInstance().addError(messageKey);
		}
		if (fatal)
		{
			// display all message we have in the queue
			for (String errorKey : BelugaState.getInstance().getErrors())
				showError(errorKey);

			// reset everything
			BelugaState.getInstance().reset();
			PandoraClient.getInstance().reset();
			try
			{
				// update the player (this will disable it to prevent more errors to happen)
				playerWebBrowser.setHTMLContent(HTMLUtil.getPageHTML(Page.AUDIO));
			}
			catch (InternalException e1)
			{
				log.error(e1.getMessage());
				System.exit(-1);
			}

			webBrowser.executeJavascript("disableUI()");
			webBrowser.executeJavascript("showFatal()");
		}
	}

	private void handleException(Exception e, String command, Object[] parameters)
	{
		retryCount++;
		try
		{
			if (retryCount >= MAX_RETRIES)
			{
				reportFatalError("too.many.errrors", e);
			}
			else if (e instanceof CryptoException)
			{
				reportFatalError("crypto.related.problem", e);
			}
			else if (e instanceof CommunicationException)
			{
				if (!pandoraClient.isLoggedIn())
				{
					reportError("connection.to.pandora.failed.check.proxy");
					dispatch("goto/configuration");
					return;
				}
				reportError("communication.problem");
				dispatch(command, parameters);
				return;
			}
			else if (e instanceof PandoraException)
			{
				PandoraException pe = (PandoraException) e;
				if (pe.getError() == PandoraError.INVALID_CREDENTIALS || pe.getError() == PandoraError.LICENSING_RESTRICTIONS)
				{
					reportError(pe.getError().getMessageKey());
					dispatch("goto/configuration");
					return;
				}
				if (pe.getError() == PandoraError.INVALID_AUTH_TOKEN)
				{
					reportError(pe.getError().getMessageKey());
					pandoraClient.partnerLogin();
					pandoraClient.userLogin();
					dispatch("next");
					return;
				}
				if (pe.getError() == PandoraError.INVALID_STATION)
				{
					reportError(pe.getError().getMessageKey());
					dispatch("next");
					return;
				}
				if (state.getPage().equals(Page.USER_CREATE))
				{
					hideLoader();
					reportError(pe.getError().getMessageKey(), true);
					return;
				}
				hideLoader();
				reportFatalError(pe.getError().getMessageKey(), pe);
				return;
			}
		}
		catch (Exception e1)
		{
		}
		reportFatalError("a.bug.occured", e);
	}

	public void lostFocus()
	{
		webBrowser.executeJavascript("hideMenu()");
	}
}
