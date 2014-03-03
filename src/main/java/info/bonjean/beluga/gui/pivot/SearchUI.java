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
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.response.Result;
import info.bonjean.beluga.response.SearchArtist;
import info.bonjean.beluga.response.SearchSong;
import info.bonjean.beluga.response.Song;

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class SearchUI extends TablePane implements Bindable
{
	private static Logger log = LoggerFactory.getLogger(SearchUI.class);
	@BXML
	private TextInput searchInput;
	@BXML
	private PushButton submitButton;
	@BXML
	private BoxPane artistsPane;
	@BXML
	private BoxPane songsPane;
	@BXML
	private Border artistsTabPane;
	@BXML
	private Border songsTabPane;
	@BXML
	private Label nearMatchesAvailable;

	private final PandoraClient pandoraClient = PandoraClient.getInstance();
	private final BelugaState state = BelugaState.getInstance();
	private Resources resources;

	public SearchUI()
	{
		Action.getNamedActions().put("create", new AsyncAction(MainWindow.getInstance())
		{
			@Override
			public void asyncPerform(final Component source)
			{
				log.info("creatingNewStation");
				String type = (String) source.getUserData().get("type");
				String token = (String) source.getUserData().get("token");
				try
				{
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
					MainWindow.getInstance().updateStationsList();

					// update stations list menu
					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							MainWindow.getInstance().updateStationsListMenu();
						}
					}, true);
				}
				catch (BelugaException e)
				{
					log.error(e.getMessage(), e);
				}
			}
		});

		Action.getNamedActions().put("submit", new AsyncAction(MainWindow.getInstance())
		{
			@Override
			public void asyncPerform(final Component source)
			{
				String query = searchInput.getText();

				if (query.isEmpty())
				{
					log.info("emptyQuery");
					return;
				}

				log.info("searching");
				try
				{
					setEnabled(false);

					final Result results = pandoraClient.search(searchInput.getText());

					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							artistsPane.remove(0, artistsPane.getLength());
							songsPane.remove(0, songsPane.getLength());

							for (SearchArtist artist : results.getArtists())
								artistsPane.add(newResult(artist.getArtistName(), artist.getArtistName(), artist.getMusicToken(), "search"));

							for (SearchSong artist : results.getSongs())
								songsPane.add(newResult(artist.getSongName() + " (" + artist.getArtistName() + ")", artist.getSongName(),
										artist.getMusicToken(), "search"));

							nearMatchesAvailable.setVisible(results.isNearMatchesAvailable());

							setEnabled(true);
						}
					}, true);
				}
				catch (BelugaException e)
				{
					log.error(e.getMessage(), e);
				}
			}

			@Override
			public void afterPerform()
			{
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						setTabTitles();
						setFocus();
					}
				}, true);
			}
		});
	}

	private MenuButton newResult(String label, String station, String token, String type)
	{
		MenuButton link = new MenuButton();
		link.getStyles().put("padding", 0);
		link.setButtonData(label);
		Menu menu = new Menu();
		Menu.Section menuSection = new Menu.Section();
		StringBuffer sb = new StringBuffer((String) resources.get("createStationWith"));
		sb.append(" <");
		sb.append(station);
		sb.append(">");
		Menu.Item menuItem = new Menu.Item(sb.toString());
		menuItem.setAction("create");
		menuItem.getUserData().put("token", token);
		menuItem.getUserData().put("type", type);
		menuSection.add(menuItem);
		menu.getSections().add(menuSection);
		link.setMenu(menu);
		return link;
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		this.resources = resources;
		searchInput.getComponentKeyListeners().add(new ComponentKeyListener()
		{
			@Override
			public boolean keyTyped(Component component, char character)
			{
				return false;
			}

			@Override
			public boolean keyPressed(Component component, int keyCode, KeyLocation keyLocation)
			{
				if (keyCode == Keyboard.KeyCode.ENTER)
					submitButton.press();
				return false;
			}

			@Override
			public boolean keyReleased(Component component, int keyCode, KeyLocation keyLocation)
			{
				return false;
			}
		});

		Song song = state.getSong();
		artistsPane.add(newResult(song.getArtistName(), song.getArtistName(), song.getTrackToken(), "artist"));
		songsPane.add(newResult(song.getSongName() + " (" + song.getArtistName() + ")", song.getSongName(), song.getTrackToken(), "song"));
		setTabTitles();
		setFocus();
	}

	private void setFocus()
	{
		searchInput.requestFocus();
	}

	private String getTabTitle(BoxPane boxPane, String baseNameKey)
	{
		int count = boxPane.getLength();
		StringBuffer sb = new StringBuffer((String) resources.get(baseNameKey));
		sb.append(" (");
		sb.append(count);
		sb.append(" ");
		if (count > 1)
			sb.append((String) resources.get("results"));
		else
			sb.append((String) resources.get("result"));
		sb.append(")");
		return sb.toString();
	}

	private void setTabTitles()
	{
		TabPane.setTabData(artistsTabPane, getTabTitle(artistsPane, "artists"));
		TabPane.setTabData(songsTabPane, getTabTitle(songsPane, "tracks"));
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		if (enabled)
			submitButton.setAction("submit");
		else
			submitButton.setAction((Action) null);
		submitButton.setEnabled(enabled);
		searchInput.setEnabled(enabled);
		artistsPane.setEnabled(enabled);
		songsPane.setEnabled(enabled);
	}
}
