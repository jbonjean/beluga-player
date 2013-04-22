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
import info.bonjean.beluga.response.Feedback;
import info.bonjean.beluga.util.HTMLUtil;

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
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.TablePane;
import org.slf4j.Logger;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class StationUI extends TablePane implements Bindable
{
	@Log
	private static Logger log;
	@BXML
	private ImageView stationCover;
	@BXML
	private Label stationName;
	@BXML
	private Label stationCreationDate;
	@BXML
	private Label stationGenres;
	@BXML
	private BoxPane lovedSongsPane;
	@BXML
	private BoxPane bannedSongsPane;

	private final BelugaState state = BelugaState.getInstance();
	private final PandoraClient pandoraClient = PandoraClient.getInstance();
	private Resources resources;

	public StationUI()
	{
		Action.getNamedActions().put("deleteFeedback", new AsyncAction(MainWindow.getInstance())
		{
			@Override
			public void asyncPerform(final Component source)
			{
				log.info("deletingFeedback");
				final Feedback feedback = (Feedback) source.getUserData().get("feedback");
				final MenuButton item = (MenuButton) source.getUserData().get("item");

				try
				{
					pandoraClient.deleteFeedback(feedback.getFeedbackId());

					// update song currently playing if necessary
					if (feedback.isPositive())
						updateSongFeedback(feedback.getFeedbackId());

					// update UI
					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							if (feedback.isPositive())
								lovedSongsPane.remove(item);
							else
								bannedSongsPane.remove(item);
						}
					}, true);

					log.info("feedbackDeleted");
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
		stationName.setText(state.getStation().getStationName());

		if (state.getStation().getArtUrl().isEmpty())
			stationCover.setImage(HTMLUtil.getDefaultCover());
		else
		{
			try
			{
				stationCover.setImage(new URL(state.getStation().getArtUrl()));
			}
			catch (Exception e)
			{
				stationCover.setImage(HTMLUtil.getDefaultCover());
			}
		}

		stationCreationDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date(state.getStation().getDateCreated().getTime())));
		StringBuffer sb = new StringBuffer();
		for (String genre : state.getStation().getGenre())
		{
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(genre);
		}
		if (sb.length() > 0)
		{
			// capitalize first letter
			sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
			// finish with a period
			sb.append(".");
		}
		stationGenres.setText(sb.toString());

		lovedSongsPane.removeAll();
		for (Feedback feedback : state.getStation().getFeedback().getThumbsUp())
			lovedSongsPane.add(newFeedback(feedback));

		bannedSongsPane.removeAll();
		for (Feedback feedback : state.getStation().getFeedback().getThumbsDown())
			bannedSongsPane.add(newFeedback(feedback));
	}

	private MenuButton newFeedback(Feedback feedback)
	{
		MenuButton link = new MenuButton();
		link.getStyles().put("padding", 0);
		StringBuffer sb = new StringBuffer(feedback.getSongName());
		sb.append(" ");
		sb.append((String) resources.get("by"));
		sb.append(" ");
		sb.append(feedback.getArtistName());
		link.setButtonData(sb.toString());
		Menu menu = new Menu();
		Menu.Section menuSection = new Menu.Section();
		Menu.Item menuItem = new Menu.Item(resources.get(feedback.isPositive() ? "unlike" : "unban"));
		menuItem.setAction("deleteFeedback");
		menuItem.getUserData().put("feedback", feedback);
		menuItem.getUserData().put("item", link);
		menuSection.add(menuItem);
		menu.getSections().add(menuSection);
		link.setMenu(menu);
		return link;
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
					log.debug("Current song feedback updated");
				}
				return;
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled)
	{
	}
}
