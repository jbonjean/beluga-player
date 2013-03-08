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
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.client.PandoraPlaylist;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.exception.InternalException;
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.log.Log;
import info.bonjean.beluga.log.StatusBarAppender;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.response.Station;

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.Menu.Item;
import org.apache.pivot.wtk.Menu.Section;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuButton;
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

	private final BelugaState state = BelugaState.getInstance();
	private final PandoraClient pandoraClient = PandoraClient.getInstance();
	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();

	@BXML
	TablePane.Row contentWrapper;

	@BXML
	PlayerUI playerUI;

	@BXML
	MenuButton stations;

	@BXML
	MenuBar.Item pandoraMenu;

	@BXML
	MenuUI menuUI;

	@BXML
	Label statusBar;

	Component content;
	String page = "loader";
	Resources resources;

	private static MainWindow instance;

	private int disableLockCount = 0;

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

		Action.getNamedActions().put("void", new AsyncAction(getInstance())
		{
			@Override
			public void asyncPerform(Component source)
			{
				log.info("noActionAssociated");
			}
		});

		Action.getNamedActions().put("load", new AsyncAction(getInstance())
		{
			@Override
			public void asyncPerform(final Component source)
			{
				final String newPage = source.getUserData().get("bxml") != null ? (String) source.getUserData().get("bxml") : page;
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						load(newPage);
					}
				}, true);
			}
		});

		Action.getNamedActions().put("pandoraStart", new AsyncAction(getInstance())
		{
			@Override
			public void asyncPerform(Component source)
			{
				try
				{
					pandoraClient.reset();
					state.reset();
					PandoraPlaylist.getInstance().clear();
					stopPlayer();
					pandoraClient.partnerLogin();
					pandoraClient.userLogin();

					selectStation(null);

					// enable pandora items :-)
					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							setEnablePandoraMenu(true);
						}
					}, true);

					// set the page to song, this means the page will be loaded
					// as soon as songChanged is invoked (by the player)
					page = "song";

					// increase count because the player will decrease on first song
					disableLockCount++;

					// TODO: we could wait here that playback starts, this would prevent
					// the short time where UI is inconsistent. Another possibility is to create
					// a dummy song entry to set the state, this way nothing is broken
					// but be careful to synchronize the state, because it can also be updated
					// by the player thread.
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
				stopPlayer();
			}
		});

		Action.getNamedActions().put("stationSelect", new AsyncAction(getInstance())
		{
			@Override
			public void asyncPerform(Component source)
			{
				Station station = (Station) source.getUserData().get("station");
				try
				{
					selectStation(station);
					stopPlayer();
				}
				catch (BelugaException e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		this.resources = resources;

		// give a reference of the status bar to the logger
		StatusBarAppender.setStatusBar(statusBar);
		// also resource for message translation
		StatusBarAppender.setResources(resources);

		// load temporary screen
		load("loader");

		// disable pandora stuff
		setEnablePandoraMenu(false);

		// start Pandora backend
		// Action.getNamedActions().get("pandoraStart").perform(this);
	}

	/**
	 * Be careful with that method, setEnable must be paired to keep consistent lock count TODO: better solutions are welcome
	 */
	@Override
	public synchronized void setEnabled(boolean enabled)
	{
		if (enabled)
			disableLockCount--;
		else
			disableLockCount++;

		// we don't enable the display if somebody has still a lock
		if (enabled && disableLockCount > 0)
			return;

		menuUI.setEnabled(enabled);
		content.setEnabled(enabled);
		playerUI.setEnabled(enabled);
	}

	public void songChanged(Song song)
	{
		// reload song page only if currently displayed
		if (page.equals("song"))
			load("song");
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
		PivotUI.setEnable(stations, enabled);
	}

	private void load(String bxml)
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
			e.printStackTrace();
		}
	}

	private void selectStation(Station newStation) throws BelugaException
	{
		updateStationsList();

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
			if (state.getStationList().isEmpty())
			{
				// TODO: not yet implemented
				log.error("This account has no station");
				throw new InternalException(null);
			}
			newStation = state.getStationList().get(0);
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
		log.debug("Station selected: " + state.getStation().getStationName());
	}

	public void updateStationsList() throws BelugaException
	{
		// update station list
		state.setStationList(pandoraClient.getStationList());

		// rebuild menu entry
		ApplicationContext.queueCallback(new Runnable()
		{
			@Override
			public void run()
			{
				Section section = stations.getMenu().getSections().get(0);
				section.remove(0, section.getLength());
				for (Station station : state.getStationList())
				{
					Menu.Item item = new Menu.Item(station.getStationName());
					item.getUserData().put("station", station);
					item.setAction(Action.getNamedActions().get("stationSelect"));
					section.add(item);
				}
			}
		}, true);
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