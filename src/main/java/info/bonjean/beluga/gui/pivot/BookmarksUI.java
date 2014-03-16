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
import info.bonjean.beluga.response.ArtistBookmark;
import info.bonjean.beluga.response.SongBookmark;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.TablePane;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class BookmarksUI extends TablePane implements Bindable
{
	@BXML
	protected BoxPane artistBookmarksPane;
	@BXML
	protected BoxPane songBookmarksPane;

	private Resources resources;
	private final BelugaState state = BelugaState.getInstance();

	public BookmarksUI()
	{
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		this.resources = resources;
		artistBookmarksPane.removeAll();
		for (ArtistBookmark artistBookmark : state.getBookmarks().getArtists())
			artistBookmarksPane.add(newBookmark(
					new Date(artistBookmark.getDateCreated().getTime()),
					artistBookmark.getArtistName(), artistBookmark.getBookmarkToken(), "artist"));

		songBookmarksPane.removeAll();
		for (SongBookmark songBookmark : state.getBookmarks().getSongs())
		{
			StringBuffer sb = new StringBuffer(songBookmark.getSongName());
			sb.append(" ");
			sb.append((String) resources.get("by"));
			sb.append(" ");
			sb.append(songBookmark.getArtistName());
			sb.append(" (");
			sb.append(songBookmark.getAlbumName());
			sb.append(")");
			songBookmarksPane.add(newBookmark(new Date(songBookmark.getDateCreated().getTime()),
					sb.toString(), songBookmark.getBookmarkToken(), "song"));
		}
	}

	private MenuButton newBookmark(Date date, String label, String bookmarkToken, String type)
	{
		MenuButton link = new MenuButton();
		link.getStyles().put("padding", 0);
		StringBuffer sb = new StringBuffer(new SimpleDateFormat("yyyy-MM-dd").format(date));
		sb.append(" ");
		sb.append(label);
		link.setButtonData(sb.toString());
		Menu menu = new Menu();
		Menu.Section menuSection = new Menu.Section();
		Menu.Item menuItem = new Menu.Item(resources.get("deleteBookmark"));
		menuItem.setAction("delete-bookmark");
		menuItem.getUserData().put("bookmarkToken", bookmarkToken);
		menuItem.getUserData().put("item", link);
		menuItem.getUserData().put("type", type);
		menuSection.add(menuItem);
		menu.getSections().add(menuSection);
		link.setMenu(menu);
		return link;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
	}
}
