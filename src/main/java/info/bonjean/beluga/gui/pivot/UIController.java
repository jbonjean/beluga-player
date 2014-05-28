/*
 * Copyright (C) 2012, 2013, 2014 Julien Bonjean <julien@bonjean.info>
 * 
 * This file is part of Beluga Player.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package info.bonjean.beluga.gui.pivot;

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.LastFMSession;
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.client.PandoraPlaylist;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.configuration.DNSProxy;
import info.bonjean.beluga.connection.BelugaHTTPClient;
import info.bonjean.beluga.event.PlaybackEvent;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.exception.InternalException;
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.gui.notification.Notification;
import info.bonjean.beluga.response.Feedback;
import info.bonjean.beluga.response.Result;
import info.bonjean.beluga.response.SearchArtist;
import info.bonjean.beluga.response.SearchSong;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.response.Station;
import info.bonjean.beluga.util.LastFMUtil;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.Menu.Section;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.content.ListItem;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 * This is the main controller, that acts as a "glue" between the UI and the
 * backend.
 * Any idea to improve this design is welcome.
 * 
 * TODO: more cleanup to do...
 * 
 */
public class UIController implements EventSubscriber<PlaybackEvent>
{
	private static Logger log = LoggerFactory.getLogger(UIController.class);
	private static final BelugaState state = BelugaState.getInstance();
	private static final PandoraClient pandoraClient = PandoraClient.getInstance();
	private static final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private static final PandoraPlaylist playlist = PandoraPlaylist.getInstance();
	private final MainWindow mainWindow;

	private static PlayerUI playerUI;
	private static MenuUI menuUI;

	public UIController(MainWindow mainWindow)
	{
		EventBus.subscribe(PlaybackEvent.class, this);
		this.mainWindow = mainWindow;
	}

	public void initialize()
	{
		playerUI = MainWindow.getInstance().playerUI;
		menuUI = MainWindow.getInstance().menuUI;
	}

	protected void registerActions()
	{
		Action.getNamedActions().put("load", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(final Component source) throws BelugaException
			{
				final String newPage = source.getUserData().get("bxml") != null ? (String) source
						.getUserData().get("bxml") : state.getPage().getName();

				// load data outside of UI thread
				if (newPage.equals("station"))
				{
					log.info("retrievingStationDetails");
					// retrieve station full information
					state.setStation(pandoraClient.getStation(state.getStation()));
				}
				else if (newPage.equals("bookmarks"))
				{
					log.info("retrievingBookmarks");
					state.setBookmarks(pandoraClient.getBookmarks());
				}

				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						mainWindow.loadPage(newPage);
					}
				}, true);
			}
		});
		Action.getNamedActions().put("debug-refresh", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(Component source) throws BelugaException
			{
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						mainWindow.reloadResources();
						mainWindow.loadPage(state.getPage().getName());
					}
				}, true);
			}

		});
		Action.getNamedActions().put("back", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(final Component source) throws BelugaException
			{
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						MainWindow.getInstance().loadPage(isPlaybackStarted() ? "song" : "welcome");
					}
				}, false);
			}
		});
		Action.getNamedActions().put("exit", new Action()
		{
			@Override
			public void perform(Component source)
			{
				System.exit(0);
			}
		});

		Action.getNamedActions().put("open-url", new Action()
		{
			@Override
			public void perform(Component source)
			{
				String url = (String) source.getUserData().get("url");
				try
				{
					Desktop.getDesktop().browse(new URI(url));
				}
				catch (Exception e)
				{
					log.error(e.getMessage(), e);
				}
			}
		});

		Action.getNamedActions().put("delete-station", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(Component source) throws BelugaException
			{
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						mainWindow.confirmStationDelete.open(MainWindow.getInstance(),
								new SheetCloseListener()
								{
									@Override
									public void sheetClosed(Sheet sheet)
									{
										if (mainWindow.confirmStationDelete.getResult()
												&& mainWindow.confirmStationDelete
														.getSelectedOptionIndex() == 1)
										{
											try
											{
												log.info("deletingStation");
												pandoraClient.deleteStation(state.getStation());
												log.info("stationDeleted");
												updateStationsList();
												selectStation(null);
												playerUI.skip();
											}
											catch (BelugaException e)
											{
												log.error(e.getMessage(), e);
											}
										}
									}
								});
					}
				}, true);
			}
		});

		Action.getNamedActions().put("void", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(Component source) throws BelugaException
			{
				log.info("noActionAssociated");
			}
		});

		Action.getNamedActions().put("bookmark-artist", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(Component source) throws BelugaException
			{
				log.info("creatingNewBookmark");
				pandoraClient.addArtistBookmark(state.getSong().getTrackToken());
				log.info("artistBookmarkCreated");
			}
		});

		Action.getNamedActions().put("bookmark-song", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(Component source) throws BelugaException
			{
				log.info("creatingNewBookmark");
				pandoraClient.addSongBookmark(state.getSong().getTrackToken());
				log.info("songBookmarkCreated");
			}
		});

		Action.getNamedActions().put("start-pandora", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(Component source) throws BelugaException
			{
				log.info("connectionToPandora");

				// ensure the initial state is clear
				clearResources();
				playerUI.close();

				pandoraClient.partnerLogin();
				pandoraClient.userLogin();

				updateStationsList();
				if (state.getStationList().size() == 0)
				{
					// we need at least one station, this way there is no
					// need
					// to handle the no station case
					// default station: Wolfgang Amadeus Mozart
					pandoraClient.addStation("C88");
					log.info("defaultStationCreated");
					updateStationsList();
				}

				selectStation(null);

				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						mainWindow.loadPage("connected");
					}
				}, true);

				// notify the player we are ready
				EventBus.publish(new PlaybackEvent(PlaybackEvent.Type.PANDORA_CONNECTED, null));

				// start the player
				playerUI.open();
			}
		});

		Action.getNamedActions().put("stop-pandora", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(Component source) throws BelugaException
			{
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						clearResources();
						playerUI.close();
						mainWindow.loadPage("welcome");
						log.info("disconnectedFromPandora");
					}
				}, true);
			}
		});

		Action.getNamedActions().put("next-song", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(Component source) throws BelugaException
			{
				log.info("skippingSong");
				playerUI.skip();
			}
		});

		Action.getNamedActions().put("pause-song", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(Component source) throws BelugaException
			{
				if (playerUI.isPaused())
					log.info("unpausingSong");
				else
					log.info("pausingSong");

				playerUI.playPause();
			}
		});

		Action.getNamedActions().put("select-station", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(Component source) throws BelugaException
			{
				log.info("changingStation");
				Station station = (Station) source.getUserData().get("station");
				selectStation(station);
				playerUI.skip();
			}
		});
		Action.getNamedActions().put("like", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(final Component source) throws BelugaException
			{
				log.info("sendingFeedback");
				pandoraClient.addFeedback(state.getSong(), true);
				log.info("feedbackSent");
			}
		});
		Action.getNamedActions().put("ban", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(final Component source) throws BelugaException
			{
				log.info("sendingFeedback");
				pandoraClient.addFeedback(state.getSong(), false);
				log.info("feedbackSent");
				playerUI.skip();
			}
		});
		Action.getNamedActions().put("sleep", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(final Component source) throws BelugaException
			{
				log.info("sendingFeedback");
				pandoraClient.sleepSong(state.getSong());
				log.info("feedbackSent");
				playerUI.skip();
			}
		});
		Action.getNamedActions().put("create", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(final Component source) throws BelugaException
			{
				log.info("creatingNewStation");
				String type = (String) source.getUserData().get("type");
				String token = (String) source.getUserData().get("token");
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
				log.info("newStationCreated");

				// update stations list
				updateStationsList();
			}
		});

		Action.getNamedActions().put("search", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(final Component source) throws BelugaException
			{
				final SearchUI searchUI = getPage(SearchUI.class);
				String query = searchUI.searchInput.getText();

				if (query.isEmpty())
				{
					log.info("emptyQuery");
					return;
				}

				log.info("searching");
				setEnabled(false);

				final Result results = pandoraClient.search(searchUI.searchInput.getText());

				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						searchUI.artistsPane.remove(0, searchUI.artistsPane.getLength());
						searchUI.songsPane.remove(0, searchUI.songsPane.getLength());

						for (SearchArtist artist : results.getArtists())
							searchUI.artistsPane.add(searchUI.newResult(artist.getArtistName(),
									artist.getArtistName(), artist.getMusicToken(), "search"));

						for (SearchSong artist : results.getSongs())
							searchUI.songsPane.add(searchUI.newResult(artist.getSongName() + " ("
									+ artist.getArtistName() + ")", artist.getSongName(),
									artist.getMusicToken(), "search"));

						searchUI.nearMatchesAvailable.setVisible(results.isNearMatchesAvailable());

						setEnabled(true);
					}
				}, true);
			}

			@Override
			public void afterPerform()
			{
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							SearchUI searchUI = getPage(SearchUI.class);
							searchUI.setTabTitles();
							searchUI.setFocus();
						}
						catch (BelugaException e)
						{
							log.error(e.getMessage(), e);
						}
					}
				}, true);
			}
		});
		Action.getNamedActions().put("update-quickmix", new AsyncAction(MainWindow.getInstance())
		{
			@Override
			public void asyncPerform(final Component source) throws BelugaException
			{
				final List<String> quickMixStationIds = new ArrayList<String>();
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						QuickMixUI quickMixUI;
						try
						{
							quickMixUI = getPage(QuickMixUI.class);
						}
						catch (BelugaException e)
						{
							log.error(e.getMessage(), e);
							return;
						}
						for (int i = 0; i < quickMixUI.stationsPane.getLength(); i++)
						{
							Checkbox station = (Checkbox) quickMixUI.stationsPane.get(i);
							if (station.isSelected())
								quickMixStationIds.add((String) station.getUserData().get(
										"stationId"));
						}
					}
				}, true);

				PandoraClient.getInstance().setQuickMix(quickMixStationIds);

				if (state.getStation().isQuickMix())
				{
					PandoraPlaylist.getInstance().clear();
					updateStationsList();
				}

				// redirect to the main screen
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						MainWindow.getInstance().loadPage("song");
					}
				}, false);
			}
		});
		Action.getNamedActions().put("create-account", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(final Component source) throws BelugaException
			{
				AccountCreationUI accountCreationUI = getPage(AccountCreationUI.class);
				if (!accountCreationUI.termsOfUseInput.isSelected())
				{
					log.error("youMustAgreeToTheTermsOfUse");
					return;
				}

				log.info("creatingNewAccount");
				pandoraClient.partnerLogin();
				pandoraClient.createUser(
						accountCreationUI.emailAddressInput.getText(),
						accountCreationUI.passwordInput.getText(),
						accountCreationUI.birthYearInput.getText(),
						accountCreationUI.zipCodeInput.getText(),
						(String) accountCreationUI.genderGroup.getSelection().getUserData()
								.get("value"),
						String.valueOf(accountCreationUI.emailOptInInput.isSelected()));
				log.info("accountCreated");
				configuration.setUserName(accountCreationUI.emailAddressInput.getText());
				configuration.setPassword(accountCreationUI.passwordInput.getText());
				configuration.setConfigurationVersion(BelugaState.getInstance().getVersion());
				configuration.store();
			}
		});
		Action.getNamedActions().put("delete-bookmark", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(final Component source) throws BelugaException
			{
				final BookmarksUI bookmarksUI = getPage(BookmarksUI.class);

				log.info("deletingBookmark");
				final String bookmarkToken = (String) source.getUserData().get("bookmarkToken");
				final MenuButton item = (MenuButton) source.getUserData().get("item");
				final String type = (String) source.getUserData().get("type");

				if (type.equals("song"))
					pandoraClient.deleteSongBookmark(bookmarkToken);
				else
					pandoraClient.deleteArtistBookmark(bookmarkToken);

				// update UI
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						if (type.equals("song"))
							bookmarksUI.songBookmarksPane.remove(item);
						else
							bookmarksUI.artistBookmarksPane.remove(item);
					}
				}, true);

				log.info("bookmarkDeleted");
			}
		});
		Action.getNamedActions().put("delete-feedback", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(final Component source) throws BelugaException
			{
				final StationUI stationUI = getPage(StationUI.class);

				log.info("deletingFeedback");
				final Feedback feedback = (Feedback) source.getUserData().get("feedback");
				final MenuButton item = (MenuButton) source.getUserData().get("item");

				pandoraClient.deleteFeedback(feedback.getFeedbackId());

				// update song currently playing if necessary
				if (feedback.isPositive())
					stationUI.updateSongFeedback(feedback.getFeedbackId());

				// update UI
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						if (feedback.isPositive())
							stationUI.lovedSongsPane.remove(item);
						else
							stationUI.bannedSongsPane.remove(item);
					}
				}, true);

				log.info("feedbackDeleted");
			}
		});
		Action.getNamedActions().put("save-preferences", new AsyncAction(mainWindow)
		{
			@Override
			public void asyncPerform(final Component source) throws BelugaException
			{
				final PreferencesUI preferencesUI = getPage(PreferencesUI.class);

				configuration.setUserName(preferencesUI.emailAddressInput.getText());
				configuration.setPassword(preferencesUI.passwordInput.getText());
				configuration.setProxyHost(preferencesUI.httpProxyHostInput.getText());
				configuration.setProxyPort(preferencesUI.httpProxyPortInput.getText());
				configuration.setDNSProxy(((DNSProxy) preferencesUI.dnsProxyInput.getSelectedItem())
						.getId());
				configuration.setLastFMEnabled(preferencesUI.lastFMEnableCheckbox.isSelected());
				configuration.setLastFMUsername(preferencesUI.lastFMUsernameInput.getText());
				configuration.setLastFMPassword(preferencesUI.lastFMPasswordInput.getText());
				configuration.setAdsDetectionEnabled(preferencesUI.adsEnableDetectionCheckbox
						.isSelected());
				configuration.setAdsSilenceEnabled(preferencesUI.adsEnableSilentCheckbox
						.isSelected());
				configuration
						.setNotificationsStyle((String) ((ListItem) preferencesUI.notificationsStyle
								.getSelectedItem()).getUserData());
				configuration.setStationsOrderBy((String) ((ListItem) preferencesUI.stationsOrderBy
						.getSelectedItem()).getUserData());
				configuration.setWindowRestoreEnabled(preferencesUI.windowRestoreCheckbox
						.isSelected());

				if (!BelugaState.getInstance().getVersion()
						.equals(BelugaConfiguration.CONFIGURATION_DEFAULT_VERSION))
					configuration.setConfigurationVersion(BelugaState.getInstance().getVersion());

				configuration.store();

				log.info("preferencesUpdated");

				BelugaHTTPClient.reset();
				LastFMSession.reset();

				// redirect to the main screen: song if playback started,
				// welcome otherwise
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						MainWindow.getInstance().loadPage(isPlaybackStarted() ? "song" : "welcome");
					}
				}, false);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private <E extends Component> E getPage(Class<E> clazz) throws BelugaException
	{
		Component page = state.getPage().getComponent();
		if (clazz.isInstance(page))
			return (E) page;
		throw new InternalException("invalidActionCall");
	}

	private void clearResources()
	{
		state.reset();
		pandoraClient.reset();
		playlist.clear();
		BelugaHTTPClient.reset();
		LastFMSession.reset();
	}

	private void selectStation(Station newStation) throws BelugaException
	{
		// if no station requested, select configuration one
		if (newStation == null)
		{
			for (Station station : state.getStationList())
			{
				if (station.getStationId().equals(configuration.getDefaultStationId()))
				{
					newStation = station;
					break;
				}
			}
		}
		// else check if station requested is valid
		else
		{
			boolean found = false;
			for (Station station : state.getStationList())
			{
				if (station.getStationId().equals(newStation.getStationId()))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				log.warn("requestedStationDoesNotExist");
				newStation = null;
			}
		}

		// at this point, if no station has been selected, there has been a
		// problem, select first one
		if (newStation == null)
		{
			if (!state.getStationList().isEmpty())
				newStation = state.getStationList().get(0);
			else
			{
				// should not happen
				log.error("noStation");
				return;
			}
		}

		// check if station changed
		if (state.getStation() == null
				|| !newStation.getStationId().equals(state.getStation().getStationId()))
		{
			// invalidate playlist
			PandoraPlaylist.getInstance().clear();

			// update the configuration
			configuration.setDefaultStationId(newStation.getStationId());
			configuration.store();
		}

		state.setStation(newStation);

		log.debug("Station selected: " + state.getStation().getStationName());

		// initially feed the playlist
		PandoraPlaylist.getInstance().feedQueue();
	}

	private void updateStationsList() throws BelugaException
	{
		log.info("retrievingStations");
		List<Station> stationList = pandoraClient.getStationList();
		if (configuration.getStationsOrderBy().equals("name"))
		{
			Collections.sort(stationList, new Comparator<Station>()
			{
				@Override
				public int compare(Station station1, Station station2)
				{
					return station1.getStationName().compareTo(station2.getStationName());
				}
			});
		}
		state.setStationList(stationList);
	}

	private boolean isPlaybackStarted()
	{
		return pandoraClient.isLoggedIn() && state.getSong() != null;
	}

	public void recursiveEnableComponent(Component component, boolean enabled)
	{
		// disable the parent component first to prevent any user interaction
		if (!enabled)
			PivotUI.enableComponent(component, false);

		// if it's a container, handle the children (except for menu)
		if (component instanceof Container && !(component instanceof Menu))
		{
			Iterator<Component> iterator = ((Container) component).iterator();
			while (iterator.hasNext())
				recursiveEnableComponent(iterator.next(), enabled);
		}

		// enable the parent component
		if (enabled)
			PivotUI.enableComponent(component, true);
	}

	/*
	 * Keep everything related to enabled/disabled state here, it make it easier
	 * to ensure consistency between screens
	 */
	public void enableUI(boolean enabled)
	{
		if (menuUI == null || mainWindow == null || playerUI == null)
			return;

		// because of the Component class sealing, we cannot override it
		// so we need to manually update enable state of all components
		recursiveEnableComponent(menuUI, enabled);
		Iterator<Component> contentIterator = mainWindow.contentWrapper.iterator();
		while (contentIterator.hasNext())
			recursiveEnableComponent(contentIterator.next(), enabled);

		// get Pandora status
		boolean connected = pandoraClient.isLoggedIn();

		// enable/disable the loading and disconnected icons
		mainWindow.statusBarIconDiconnected.setVisible(!connected);
		mainWindow.loader.setVisible(!enabled);

		// enable/disable Pandora related features if connected
		if (enabled && connected)
		{
			// disable delete station button if only 1 station (quickmix) or
			// if quickmix is selected
			if (state.getStationList().size() < 3
					|| (state.getStation() != null && state.getStation().isQuickMix()))
				PivotUI.enableComponent(mainWindow.deleteStationButton, false);
			else
				PivotUI.enableComponent(mainWindow.deleteStationButton, true);

			// there is no station details for the quickmix
			if (state.getStation() != null && state.getStation().isQuickMix())
				PivotUI.enableComponent(mainWindow.stationDetailsButton, false);
			else
				PivotUI.enableComponent(mainWindow.stationDetailsButton, true);

			// update stations list
			Section section = mainWindow.stations.getMenu().getSections().get(0);
			section.remove(0, section.getLength());
			for (Station station : state.getStationList())
			{
				Menu.Item item = new Menu.Item(station.getStationName());
				item.getUserData().put("station", station);
				item.setAction(Action.getNamedActions().get("select-station"));
				// disable selected station
				if (state.getStation() != null
						&& state.getStation().getStationId().equals(station.getStationId()))
					PivotUI.enableComponent(item, true);
				section.add(item);
			}
		}
		mainWindow.pandoraMenu.setEnabled(connected);
		mainWindow.stations.setEnabled(connected);

		// update player UI
		recursiveEnableComponent(playerUI, !playerUI.isClosed());
	}

	@Override
	public void onEvent(PlaybackEvent playbackEvent)
	{
		log.debug("Received event: " + playbackEvent.getType());
		Song song = playbackEvent.getSong();
		switch (playbackEvent.getType())
		{
			case SONG_START:
				// reload song page only if currently displayed
				if (state.getPage().getName().equals("song")
						|| state.getPage().getName().equals("connected"))
					mainWindow.loadPage("song");
				// send a desktop notification
				new Notification(state.getSong());
				break;
			case SONG_FINISH:
				log.debug("Played " + song.getPosition() + " of " + song.getDuration());
				if (configuration.getLastFMEnabled())
					// scrobble with last.fm
					LastFMUtil.asyncScrobble(song);
				break;
			case PANDORA_DISCONNECTED:
				clearResources();
				mainWindow.loadPage("welcome");
				log.info("disconnectedFromPandora");
				break;
			case PANDORA_CONNECTED:
			case SONG_PAUSE:
			case SONG_RESUME:
		}

		// update player UI
		recursiveEnableComponent(playerUI, !playerUI.isClosed());
	}
}
