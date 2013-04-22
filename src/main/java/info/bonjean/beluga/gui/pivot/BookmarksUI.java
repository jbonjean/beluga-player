/* Copyright (C) 2012, 2013 Julien Bonjean <julien@bonjean.info>
 * 
 * This file is part of Beluga Player.
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
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.log.Log;
import info.bonjean.beluga.response.ArtistBookmark;
import info.bonjean.beluga.response.SongBookmark;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.TablePane;
import org.slf4j.Logger;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class BookmarksUI extends TablePane implements Bindable
{
	@Log
	private static Logger log;
	@BXML
	private BoxPane artistBookmarksPane;
	@BXML
	private BoxPane songBookmarksPane;

	private Resources resources;
	private final BelugaState state = BelugaState.getInstance();
	private final PandoraClient pandoraClient = PandoraClient.getInstance();

	public BookmarksUI()
	{
		Action.getNamedActions().put("deleteBookmark", new AsyncAction(MainWindow.getInstance())
		{
			@Override
			public void asyncPerform(final Component source)
			{
				log.info("deletingBookmark");
				final String bookmarkToken = (String) source.getUserData().get("bookmarkToken");
				final MenuButton item = (MenuButton) source.getUserData().get("item");
				final String type = (String) source.getUserData().get("type");

				try
				{
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
								songBookmarksPane.remove(item);
							else
								artistBookmarksPane.remove(item);
						}
					}, true);

					log.info("bookmarkDeleted");
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
		artistBookmarksPane.removeAll();
		for (ArtistBookmark artistBookmark : state.getBookmarks().getArtists())
			artistBookmarksPane.add(newBookmark(new Date(artistBookmark.getDateCreated().getTime()), artistBookmark.getArtistName(),
					artistBookmark.getBookmarkToken(), "artist"));

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
			songBookmarksPane.add(newBookmark(new Date(songBookmark.getDateCreated().getTime()), sb.toString(), songBookmark.getBookmarkToken(),
					"song"));
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
		menuItem.setAction("deleteBookmark");
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
