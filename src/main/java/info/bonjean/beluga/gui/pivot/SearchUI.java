/*
 * Copyright (C) 2012-2016 Julien Bonjean <julien@bonjean.info>
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
import info.bonjean.beluga.response.Song;

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
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

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class SearchUI extends TablePane implements Bindable
{
	@BXML
	protected TextInput searchInput;
	@BXML
	protected PushButton submitButton;
	@BXML
	protected BoxPane artistsPane;
	@BXML
	protected BoxPane songsPane;
	@BXML
	protected Border artistsTabPane;
	@BXML
	protected Border songsTabPane;
	@BXML
	protected Label nearMatchesAvailable;
	private Resources resources;

	public SearchUI()
	{
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

		// add current playing song as search result
		Song song = BelugaState.getInstance().getSong();
		artistsPane.add(newResult(song.getArtistName(), song.getArtistName(), song.getTrackToken(),
				"artist"));
		songsPane.add(newResult(song.getSongName() + " (" + song.getArtistName() + ")",
				song.getSongName(), song.getTrackToken(), "song"));
		setTabTitles();
		setFocus();
	}

	protected String getTabTitle(BoxPane boxPane, String baseNameKey)
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

	protected MenuButton newResult(String label, String station, String token, String type)
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

	protected void setTabTitles()
	{
		TabPane.setTabData(artistsTabPane, getTabTitle(artistsPane, "artists"));
		TabPane.setTabData(songsTabPane, getTabTitle(songsPane, "tracks"));
	}

	protected void setFocus()
	{
		searchInput.requestFocus();
	}
}
