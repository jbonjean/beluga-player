/*
 * Copyright (C) 2012-2021 Julien Bonjean <julien@bonjean.info>
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

import info.bonjean.beluga.bus.InternalBus;
import info.bonjean.beluga.bus.InternalBusSubscriber;
import info.bonjean.beluga.bus.PlaybackEvent;
import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.LastFMSession;
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.client.PandoraPlaylist;
import info.bonjean.beluga.configuration.AudioQuality;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.configuration.ConnectionType;
import info.bonjean.beluga.configuration.Theme;
import info.bonjean.beluga.connection.BelugaHTTPClient;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.gui.notification.Notification;
import info.bonjean.beluga.player.AudioPlayer;
import info.bonjean.beluga.response.*;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.content.ListItem;
import org.apache.pivot.wtk.content.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is the main controller, that acts as a "glue" between the UI and the
 * backend.
 * Any idea to improve this design is welcome.
 *
 * TODO: more cleanup to do...
 */
public class UIController implements InternalBusSubscriber {
	private static Logger log = LoggerFactory.getLogger(UIController.class);
	private static final BelugaState state = BelugaState.getInstance();
	private static final PandoraClient pandoraClient = PandoraClient.getInstance();
	private static final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private static final PandoraPlaylist playlist = PandoraPlaylist.getInstance();

	private final MainWindow mainWindow;
	private final AudioPlayer audioPlayer;

	public UIController(MainWindow mainWindow) {
		InternalBus.subscribe(this);
		this.mainWindow = mainWindow;
		audioPlayer = new AudioPlayer(mainWindow);
	}

	protected void registerActions() {
		Action.getNamedActions().put("load", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				final String newPage = source.getUserData().get("bxml") != null
						? (String) source.getUserData().get("bxml")
						: mainWindow.getCurrentPageName();

				// load data outside of UI thread
				if (newPage.equals("station")) {
					log.info("retrievingStationDetails");
					// retrieve station full information
					state.setStation(pandoraClient.getStation(state.getStation()));
				} else if (newPage.equals("bookmarks")) {
					log.info("retrievingBookmarks");
					state.setBookmarks(pandoraClient.getBookmarks());
				}

				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						mainWindow.loadPage(newPage);
					}
				}, true);
			}
		});
		Action.getNamedActions().put("debug-refresh", new AsyncAction(mainWindow, false, false) {
			@Override
			public void asyncPerform(Component source) throws BelugaException {
				try {
					// Give some time for the menu popup to close and avoid race condition.
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						mainWindow.reload();
						audioPlayer.uiResync();
					}
				}, true);
			}

		});
		Action.getNamedActions().put("back", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						mainWindow.loadPage(isPlaybackStarted() ? "song" : "welcome");
					}
				}, false);
			}
		});
		Action.getNamedActions().put("exit", new Action() {
			@Override
			public void perform(Component source) {
				System.exit(0);
			}
		});

		Action.getNamedActions().put("open-url", new Action() {
			@Override
			public void perform(Component source) {
				String url = (String) source.getUserData().get("url");
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});

		Action.getNamedActions().put("void", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(Component source) throws BelugaException {
				log.info("noActionAssociated");
			}
		});

		Action.getNamedActions().put("bookmark-artist", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(Component source) throws BelugaException {
				log.info("creatingNewBookmark");
				pandoraClient.addArtistBookmark(state.getSong().getTrackToken());
				log.info("artistBookmarkCreated");
			}
		});

		Action.getNamedActions().put("bookmark-song", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(Component source) throws BelugaException {
				log.info("creatingNewBookmark");
				pandoraClient.addSongBookmark(state.getSong().getTrackToken());
				log.info("songBookmarkCreated");
			}
		});

		Action.getNamedActions().put("start-pandora", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(Component source) throws BelugaException {
				log.info("connectionToPandora");

				// ensure the initial state is clear
				clearResources();
				audioPlayer.stop();

				pandoraClient.partnerLogin();
				pandoraClient.userLogin();

				updateStationsList();
				if (state.getStationList().size() == 0) {
					// we need at least one station, this way there is no
					// need
					// to handle the no station case
					// default station: Wolfgang Amadeus Mozart
					pandoraClient.addStation("C88");
					log.info("defaultStationCreated");
					updateStationsList();
				}

				selectStation(null);

				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						mainWindow.loadPage("connected");
					}
				}, true);

				// notify the player we are ready
				InternalBus.publish(new PlaybackEvent(PlaybackEvent.Type.PANDORA_CONNECTED, null));

				// start the player
				audioPlayer.start();
			}
		});

		Action.getNamedActions().put("stop-pandora", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(Component source) throws BelugaException {
				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						clearResources();
						audioPlayer.stop();
						mainWindow.loadPage("welcome");
						log.info("disconnectedFromPandora");
					}
				}, true);
			}
		});

		Action.getNamedActions().put("next-song", new AsyncAction(mainWindow, true, true) {
			@Override
			public void asyncPerform(Component source) throws BelugaException {
				log.info("skippingSong");
				audioPlayer.skip();
			}
		});

		Action.getNamedActions().put("select-station", new AsyncAction(mainWindow, true, true) {
			@Override
			public void asyncPerform(Component source) throws BelugaException {
				log.info("changingStation");
				Station station = (Station) source.getUserData().get("station");
				selectStation(station);
				audioPlayer.skip();
			}
		});
		Action.getNamedActions().put("like", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				log.info("sendingFeedback");
				pandoraClient.addFeedback(state.getSong(), true);
				log.info("feedbackSent");
			}
		});
		Action.getNamedActions().put("ban", new AsyncAction(mainWindow, true, true) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				log.info("sendingFeedback");
				pandoraClient.addFeedback(state.getSong(), false);
				log.info("feedbackSent");
				audioPlayer.skip();
			}
		});
		Action.getNamedActions().put("sleep", new AsyncAction(mainWindow, true, true) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				log.info("sendingFeedback");
				pandoraClient.sleepSong(state.getSong());
				log.info("feedbackSent");
				audioPlayer.skip();
			}
		});
		Action.getNamedActions().put("create", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				log.info("creatingNewStation");
				String type = (String) source.getUserData().get("type");
				String token = (String) source.getUserData().get("token");
				if (type.equals("search")) {
					log.debug("Add station from search results, token: " + token);
					pandoraClient.addStation(token);
				} else {
					log.debug("Add station from " + type + ", token: " + token);
					pandoraClient.addStation(type, token);
				}
				log.info("newStationCreated");

				// update stations list
				updateStationsList();
			}
		});

		Action.getNamedActions().put("search", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				SearchUI searchUI = mainWindow.getCurrentPageComponent(StationCreateUI.class).getSearchUI();
				String query = searchUI.searchInput.getText();

				if (query.isEmpty()) {
					log.info("emptyQuery");
					return;
				}

				log.info("searching");

				final Result results = pandoraClient.search(query);

				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						searchUI.artistsPane.remove(0, searchUI.artistsPane.getLength());
						searchUI.songsPane.remove(0, searchUI.songsPane.getLength());

						for (SearchArtist artist : results.getArtists())
							searchUI.artistsPane.add(searchUI.newResult(artist.getArtistName(), artist.getArtistName(),
									artist.getMusicToken(), "search"));

						for (SearchSong artist : results.getSongs())
							searchUI.songsPane
									.add(searchUI.newResult(artist.getSongName() + " (" + artist.getArtistName() + ")",
											artist.getSongName(), artist.getMusicToken(), "search"));

						searchUI.nearMatchesAvailable.setVisible(results.isNearMatchesAvailable());
					}
				}, true);
			}

			@Override
			public void afterPerform() {
				try {
					SearchUI searchUI = mainWindow.getCurrentPageComponent(StationCreateUI.class).getSearchUI();
					ApplicationContext.queueCallback(new Runnable() {
						@Override
						public void run() {
							searchUI.setTabTitles();
							searchUI.setFocus();
						}
					}, true);
				} catch (BelugaException e) {
					log.error(e.getMessage(), e);
				}
			}
		});

		Action.getNamedActions().put("delete-stations", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				List<String> stationIds = new ArrayList<String>();
				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						StationsUI quickMixUI;
						try {
							quickMixUI = mainWindow.getCurrentPageComponent(StationsUI.class);
						} catch (BelugaException e) {
							log.error(e.getMessage(), e);
							return;
						}
						for (int i = 0; i < quickMixUI.stationsPane.getLength(); i++) {
							Checkbox station = (Checkbox) quickMixUI.stationsPane.get(i);
							if (station.isSelected())
								stationIds.add((String) station.getUserData().get("stationId"));
						}
					}
				}, true);

				// prepare a lookup map for stations
				Map<String, Station> stationsLookup = state.getStationList().stream()
						.collect(Collectors.toMap(Station::getStationId, Function.identity()));
				log.info("deletingStations");
				for (String stationId : stationIds) {
					try {
						Station station = stationsLookup.get(stationId);
						if (station == null) {
							log.error("Skipping station {} that disapeared", stationId);
							continue;
						}
						log.debug("Deleting station {}", stationId);
						pandoraClient.deleteStation(station);
					} catch (BelugaException e) {
						log.error(e.getMessage(), e);
					}
				}
				log.info("stationDeleted");

				// refresh stations.
				updateStationsList();

				// if the current station is contained in the list, switch to QuickMix.
				if (stationIds.contains(state.getStation().getStationId())) {
					selectStation(null);
					audioPlayer.skip();
				}

				// redirect to the main screen
				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						mainWindow.loadPage("song");
					}
				}, false);
			}
		});

		Action.getNamedActions().put("update-quickmix", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				List<String> quickMixStationIds = new ArrayList<String>();
				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						QuickMixUI quickMixUI;
						try {
							quickMixUI = mainWindow.getCurrentPageComponent(QuickMixUI.class);
						} catch (BelugaException e) {
							log.error(e.getMessage(), e);
							return;
						}
						for (int i = 0; i < quickMixUI.stationsPane.getLength(); i++) {
							Checkbox station = (Checkbox) quickMixUI.stationsPane.get(i);
							if (station.isSelected())
								quickMixStationIds.add((String) station.getUserData().get("stationId"));
						}
					}
				}, true);

				pandoraClient.setQuickMix(quickMixStationIds);

				// update stations to get the new quickmix station configuration
				updateStationsList();

				if (state.getStation().isQuickMix())
					playlist.clear();

				// redirect to the main screen
				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						mainWindow.loadPage("song");
					}
				}, false);
			}
		});

		Action.getNamedActions().put("create-station-from-genre", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				GenresUI genresUI = mainWindow.getCurrentPageComponent(StationCreateUI.class).getGenresUI();

				Station station = null;
				Object node = genresUI.genresTree.getSelectedNode();
				if (node != null && node.getClass().equals(TreeNode.class)) {
					TreeNode treeNode = ((TreeNode) node);
					if (treeNode.getUserData() instanceof Station)
						station = (Station) treeNode.getUserData();
				}

				if (station != null) {
					log.info("creatingNewStation");
					pandoraClient.addStation(station.getStationToken());
					log.info("newStationCreated");
					updateStationsList();
				}

				// redirect to the main screen
				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						mainWindow.loadPage("song");
					}
				}, false);
			}
		});

		Action.getNamedActions().put("create-account", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				AccountCreationUI accountCreationUI = mainWindow.getCurrentPageComponent(AccountCreationUI.class);
				if (!accountCreationUI.termsOfUseInput.isSelected()) {
					log.error("youMustAgreeToTheTermsOfUse");
					return;
				}

				log.info("creatingNewAccount");
				pandoraClient.partnerLogin();
				pandoraClient.createUser(accountCreationUI.emailAddressInput.getText(),
						accountCreationUI.passwordInput.getText(), accountCreationUI.birthYearInput.getText(),
						accountCreationUI.zipCodeInput.getText(),
						(String) accountCreationUI.genderGroup.getSelection().getUserData().get("value"),
						String.valueOf(accountCreationUI.emailOptInInput.isSelected()));
				log.info("accountCreated");
				configuration.setUserName(accountCreationUI.emailAddressInput.getText());
				configuration.setPassword(accountCreationUI.passwordInput.getText());
				configuration.setConfigurationVersion(state.getVersion());
				configuration.store();
			}
		});
		Action.getNamedActions().put("delete-bookmark", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				final BookmarksUI bookmarksUI = mainWindow.getCurrentPageComponent(BookmarksUI.class);

				log.info("deletingBookmark");
				final String bookmarkToken = (String) source.getUserData().get("bookmarkToken");
				final MenuButton item = (MenuButton) source.getUserData().get("item");
				final String type = (String) source.getUserData().get("type");

				if (type.equals("song"))
					pandoraClient.deleteSongBookmark(bookmarkToken);
				else
					pandoraClient.deleteArtistBookmark(bookmarkToken);

				// update UI
				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						if (type.equals("song"))
							bookmarksUI.songBookmarksPane.remove(item);
						else
							bookmarksUI.artistBookmarksPane.remove(item);
					}
				}, true);

				log.info("bookmarkDeleted");
			}
		});
		Action.getNamedActions().put("delete-feedback", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				final StationUI stationUI = mainWindow.getCurrentPageComponent(StationUI.class);

				log.info("deletingFeedback");
				final Feedback feedback = (Feedback) source.getUserData().get("feedback");
				final MenuButton item = (MenuButton) source.getUserData().get("item");

				pandoraClient.deleteFeedback(feedback.getFeedbackId());

				// update song currently playing if necessary
				if (feedback.isPositive())
					stationUI.updateSongFeedback(feedback.getFeedbackId());

				// update UI
				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						if (feedback.isPositive())
							stationUI.lovedSongsPane.remove(item);
						else
							stationUI.bannedSongsPane.remove(item);
					}
				}, true);

				log.info("feedbackDeleted");
			}
		});
		Action.getNamedActions().put("save-preferences", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				final PreferencesUI preferencesUI = mainWindow.getCurrentPageComponent(PreferencesUI.class);
				boolean themeChanged = configuration.getTheme().equals(preferencesUI.theme.getSelectedItem()) ? false
						: true;

				configuration.setUserName(preferencesUI.emailAddressInput.getText());
				configuration.setPassword(preferencesUI.passwordInput.getText());
				configuration.setConnectionType((ConnectionType) preferencesUI.connectionType.getSelectedItem());
				configuration.setHTTPProxyHost(preferencesUI.httpProxyHostInput.getText());
				configuration.setHTTPProxyPort(preferencesUI.httpProxyPortInput.getText());
				configuration.setSocks5ProxyHost(preferencesUI.socks5ProxyHostInput.getText());
				configuration.setSocks5ProxyPort(preferencesUI.socks5ProxyPortInput.getText());
				configuration.setLastFMEnabled(preferencesUI.lastFMEnableCheckbox.isSelected());
				configuration.setLastFMUsername(preferencesUI.lastFMUsernameInput.getText());
				configuration.setLastFMPassword(preferencesUI.lastFMPasswordInput.getText());
				configuration.setAudioQuality((AudioQuality) preferencesUI.audioQuality.getSelectedItem());
				configuration.setAdsDetectionEnabled(preferencesUI.adsEnableDetectionCheckbox.isSelected());
				configuration.setAdsSilenceEnabled(preferencesUI.adsEnableSilentCheckbox.isSelected());
				configuration.setNotificationsStyle(
						(String) ((ListItem) preferencesUI.notificationsStyle.getSelectedItem()).getUserData());
				configuration.setStationsOrderBy(
						(String) ((ListItem) preferencesUI.stationsOrderBy.getSelectedItem()).getUserData());
				configuration.setWindowRestoreEnabled(preferencesUI.windowRestoreCheckbox.isSelected());
				configuration.setTheme((Theme) preferencesUI.theme.getSelectedItem());

				if (!state.getVersion().equals(BelugaConfiguration.CONFIGURATION_DEFAULT_VERSION))
					configuration.setConfigurationVersion(state.getVersion());

				configuration.store();

				log.info("preferencesUpdated");

				BelugaHTTPClient.PANDORA_API_INSTANCE.reset();
				LastFMSession.reset();

				// redirect to the main screen: song if playback started,
				// welcome otherwise
				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						if (themeChanged) {
							mainWindow.reload();
							audioPlayer.uiResync();
						}
						mainWindow.loadPage(isPlaybackStarted() ? "song" : "welcome");
					}
				}, false);
			}
		});
		Action.getNamedActions().put("rename-station", new AsyncAction(mainWindow) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				final StationUI stationUI = mainWindow.getCurrentPageComponent(StationUI.class);
				log.info("renamingStation");
				pandoraClient.renameStation(state.getStation(), stationUI.stationNameInput.getText());
				updateStationsList();
				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						audioPlayer.uiUpdateStationName();
					}
				}, false);
			}
		});
		Action.getNamedActions().put("mute", new AsyncAction(mainWindow, false, true) {
			@Override
			public void asyncPerform(final Component source) throws BelugaException {
				audioPlayer.toggleMute();
			}
		});
	}

	private void clearResources() {
		state.reset();
		pandoraClient.reset();
		playlist.clear();
		BelugaHTTPClient.PANDORA_API_INSTANCE.reset();
		LastFMSession.reset();
	}

	private void selectStation(Station newStation) throws BelugaException {
		// if no station requested, select configuration one
		if (newStation == null) {
			for (Station station : state.getStationList()) {
				if (station.getStationId().equals(configuration.getDefaultStationId())) {
					newStation = station;
					break;
				}
			}
		}
		// else check if station requested is valid
		else {
			boolean found = false;
			for (Station station : state.getStationList()) {
				if (station.getStationId().equals(newStation.getStationId())) {
					found = true;
					break;
				}
			}
			if (!found) {
				log.warn("requestedStationDoesNotExist");
				newStation = null;
			}
		}

		// at this point, if no station has been selected, there has been a
		// problem, select first one
		if (newStation == null) {
			if (!state.getStationList().isEmpty())
				newStation = state.getStationList().get(0);
			else {
				// should not happen
				log.error("noStation");
				return;
			}
		}

		// check if station changed
		if (state.getStation() == null || !newStation.getStationId().equals(state.getStation().getStationId())) {
			// invalidate playlist
			playlist.clear();

			// update the configuration
			configuration.setDefaultStationId(newStation.getStationId());
			configuration.store();
		}

		state.setStation(newStation);

		log.debug("Station selected: " + state.getStation().getStationName());

		// initially feed the playlist
		playlist.feedQueue();
	}

	private void updateStationsList() throws BelugaException {
		log.info("retrievingStations");
		List<Station> stationList = pandoraClient.getStationList();
		if (configuration.getStationsOrderBy().equals("name")) {
			Collections.sort(stationList, new Comparator<Station>() {
				@Override
				public int compare(Station station1, Station station2) {
					return station1.getStationName().compareTo(station2.getStationName());
				}
			});
		}
		state.setStationList(stationList);
		// also update the currently select station
		if (state.getStation() != null) {
			// TODO: we should probably do something if we don't find it in the new list...
			for (Station station : stationList) {
				if (station.getStationId().equals(state.getStation().getStationId())) {
					state.setStation(station);
					break;
				}
			}
		}
	}

	private boolean isPlaybackStarted() {
		return pandoraClient.isLoggedIn() && state.getSong() != null;
	}

	@Override
	public void receive(PlaybackEvent playbackEvent) {
		log.debug("Received event: " + playbackEvent.getType());
		Song song = playbackEvent.getSong();
		switch (playbackEvent.getType()) {
		case SONG_START:
			// reload song page only if currently displayed
			if (mainWindow.getCurrentPageName().equals("song") || mainWindow.getCurrentPageName().equals("connected")) {
				ApplicationContext.queueCallback(new Runnable() {
					@Override
					public void run() {
						mainWindow.loadPage("song");
					}
				}, false);
			}
			if (!configuration.getNotificationsStyle().equals("disabled"))
				// send a desktop notification
				new Notification(state.getSong());
			break;
		case SONG_FINISH:
			log.debug("Played " + song.getPosition() + " of " + song.getDuration());
			if (configuration.getLastFMEnabled())
				// scrobble with last.fm
				LastFMSession.getInstance().scrobbleTrack(song);
			break;
		case PANDORA_DISCONNECTED:
			clearResources();
			ApplicationContext.queueCallback(new Runnable() {
				@Override
				public void run() {
					mainWindow.loadPage("welcome");
					// ensure the ui is in sync with disconnected state.
					mainWindow.enableUI(true);
				}
			}, true);
			log.info("disconnectedFromPandora");
			break;
		case PANDORA_CONNECTED:
		case SONG_RESUME:
		}
	}
}
