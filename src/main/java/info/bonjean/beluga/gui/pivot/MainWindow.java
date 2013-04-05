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
package info.bonjean.beluga.gui.pivot;

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.LastFMSession;
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.client.PandoraPlaylist;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.log.Log;
import info.bonjean.beluga.log.StatusBarAppender;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.response.Station;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Menu.Item;
import org.apache.pivot.wtk.Menu.Section;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Window;
import org.slf4j.Logger;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class MainWindow extends Window implements Bindable
{
	@Log
	private static Logger log;
	@BXML
	private TablePane.Row contentWrapper;
	@BXML
	private PlayerUI playerUI;
	@BXML
	private MenuBar.Item pandoraMenu;
	@BXML
	private MenuUI menuUI;
	@BXML
	private Label statusBarText;
	@BXML
	private ImageView statusBarIcon;
	@BXML
	private ImageView statusBarIconDiconnected;
	@BXML
	private Prompt confirmStationDelete;
	@BXML
	private ActivityIndicator loader;

	private final BelugaState state = BelugaState.getInstance();
	private final PandoraClient pandoraClient = PandoraClient.getInstance();
	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private Component content;
	private String page = "loader";
	private Resources resources;
	private static MainWindow instance;

	public MainWindow()
	{
		instance = this;

		Action.getNamedActions().put("exit", new Action()
		{
			@Override
			public void perform(Component source)
			{
				System.exit(0);
			}
		});

		Action.getNamedActions().put("openURL", new Action()
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

		Action.getNamedActions().put("deleteStation", new AsyncAction(getInstance())
		{
			@Override
			public void asyncPerform(Component source)
			{
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						confirmStationDelete.open(MainWindow.getInstance(), new SheetCloseListener()
						{
							@Override
							public void sheetClosed(Sheet sheet)
							{
								if (confirmStationDelete.getResult() && confirmStationDelete.getSelectedOptionIndex() == 1)
								{
									try
									{
										log.info("deletingStation");
										pandoraClient.deleteStation(state.getStation());
										log.info("stationDeleted");
										updateStationsList();
										menuUI.updateStationsListMenu();
										selectStation(null);
										stopPlayer();
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

		Action.getNamedActions().put("void", new AsyncAction(getInstance())
		{
			@Override
			public void asyncPerform(Component source)
			{
				log.info("noActionAssociated");
			}
		});

		Action.getNamedActions().put("bookmarkArtist", new AsyncAction(getInstance())
		{
			@Override
			public void asyncPerform(Component source)
			{
				log.info("creatingNewBookmark");
				try
				{
					pandoraClient.addArtistBookmark(state.getSong().getTrackToken());
					log.info("artistBookmarkCreated");
				}
				catch (BelugaException e)
				{
					log.error(e.getMessage(), e);
				}
			}
		});

		Action.getNamedActions().put("bookmarkSong", new AsyncAction(getInstance())
		{
			@Override
			public void asyncPerform(Component source)
			{
				log.info("creatingNewBookmark");
				try
				{
					pandoraClient.addSongBookmark(state.getSong().getTrackToken());
					log.info("songBookmarkCreated");
				}
				catch (BelugaException e)
				{
					log.error(e.getMessage(), e);
				}
			}
		});

		Action.getNamedActions().put("load", new AsyncAction(getInstance())
		{
			@Override
			public void asyncPerform(final Component source)
			{
				final String newPage = source.getUserData().get("bxml") != null ? (String) source.getUserData().get("bxml") : page;
				try
				{
					// TODO: temporary workaround to load data outside of UI thread
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
							loadPage(newPage);
						}
					}, true);
				}
				catch (BelugaException e)
				{
					log.error(e.getMessage(), e);
				}
			}
		});

		Action.getNamedActions().put("startPandora", new AsyncAction(getInstance())
		{
			@Override
			public void asyncPerform(Component source)
			{
				try
				{
					log.info("connectionToPandora");

					pandoraClient.reset();
					state.reset();
					PandoraPlaylist.getInstance().clear();
					stopPlayer();
					pandoraClient.partnerLogin();
					pandoraClient.userLogin();

					updateStationsList();
					selectStation(null);

					// enable/update Pandora menus
					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							menuUI.updateStationsListMenu();
							setEnablePandoraMenu(true);
							statusBarIconDiconnected.setVisible(false);
						}
					}, true);
				}
				catch (BelugaException e)
				{
					log.error(e.getMessage(), e);
				}
			}
		});

		Action.getNamedActions().put("nextSong", new AsyncAction(getInstance())
		{
			@Override
			public void asyncPerform(Component source)
			{
				log.info("skippingSong");
				stopPlayer();
			}
		});

		Action.getNamedActions().put("stationSelect", new AsyncAction(getInstance())
		{
			@Override
			public void asyncPerform(Component source)
			{
				log.info("changingStation");
				Station station = (Station) source.getUserData().get("station");
				try
				{
					selectStation(station);
					stopPlayer();
				}
				catch (BelugaException e)
				{
					log.error(e.getMessage(), e);
				}
			}
		});
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		this.resources = resources;

		// give a reference of the status bar to the logger
		StatusBarAppender.setLabel(statusBarText);
		StatusBarAppender.setIcon(statusBarIcon);
		// also resource for message translation
		StatusBarAppender.setResources(resources);

		// load temporary screen
		loadPage("welcome");

		// disable pandora stuff
		setEnablePandoraMenu(false);

		// start Pandora backend
		// Action.getNamedActions().get("pandoraStart").perform(this);
	}

	@Override
	public synchronized void setEnabled(boolean enabled)
	{
		menuUI.setEnabled(enabled);
		content.setEnabled(enabled);
		playerUI.setEnabled(enabled);
		loader.setVisible(!enabled);
	}

	public void playbackStarted(Song song)
	{
		// reload song page only if currently displayed
		if (page.equals("song") || page.equals("welcome"))
			loadPage("song");
		
		state.setPlaybackStarted(true);
	}

	public void playbackFinished(final Song song, final long position, final long duration)
	{
		log.debug("Played " + position + " of " + duration);

		// do last.fm call in a separate thread as it could be slow
		new Thread()
		{
			@Override
			public void run()
			{
				LastFMSession.getInstance().scrobbleTrack(song, position, duration);
			}
		}.start();
	}

	public void disconnect()
	{
		state.reset();
		pandoraClient.reset();
		PandoraPlaylist.getInstance().setEnabled(false);

		// invalidate playlist
		PandoraPlaylist.getInstance().clear();

		ApplicationContext.queueCallback(new Runnable()
		{
			@Override
			public void run()
			{
				statusBarIconDiconnected.setVisible(true);
				setEnablePandoraMenu(false);
				loadPage("welcome");
			}
		}, true);
	}

	public void stopPlayer()
	{
		playerUI.stopPlayer();
	}

	private void setEnablePandoraMenu(boolean enabled)
	{
		for (Section section : pandoraMenu.getMenu().getSections())
		{
			for (int i = 0; i < section.getLength(); i++)
			{
				Item item = section.get(i);
				if (item.getName() == null || !item.getName().equals("pandoraConnectButton"))
					PivotUI.setEnable(item, enabled);
			}
		}
		menuUI.setStationsEnabled(enabled);
	}

	public synchronized void loadPage(String bxml)
	{
		try
		{
			BXMLSerializer bxmlSerializer = new BXMLSerializer();
			content = (Component) bxmlSerializer.readObject(MainWindow.class.getResource(PivotUI.BXML_PATH + bxml + ".bxml"), resources);
			contentWrapper.remove(0, contentWrapper.getLength());
			contentWrapper.add(content);
			page = bxml;
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
		}
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

		// at this point, if no station has been selected, there has been a problem, select first one
		if (newStation == null)
		{
			if (!state.getStationList().isEmpty())
				newStation = state.getStationList().get(0);
			else
			{
				log.info("noStation");
				return;
			}
		}

		// check if station changed
		if (state.getStation() == null || !newStation.getStationId().equals(state.getStation().getStationId()))
		{
			// invalidate playlist
			PandoraPlaylist.getInstance().clear();

			// update the configuration
			configuration.setDefaultStationId(newStation.getStationId());
			configuration.store();
		}

		state.setStation(newStation);

		// enable playlist
		PandoraPlaylist.getInstance().setEnabled(true);

		log.debug("Station selected: " + state.getStation().getStationName());

		// initially feed the playlist
		PandoraPlaylist.getInstance().feedQueue();
	}

	public void updateStationsList() throws BelugaException
	{
		log.info("retrievingStations");
		state.setStationList(pandoraClient.getStationList());
	}

	public void updateStationsListMenu()
	{
		menuUI.updateStationsListMenu();
	}

	public static MainWindow getInstance()
	{
		return instance;
	}

	public String getPage()
	{
		return page;
	}

}