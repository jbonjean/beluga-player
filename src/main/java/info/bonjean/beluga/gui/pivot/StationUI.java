/*
 * Copyright (C) 2012-2020 Julien Bonjean <julien@bonjean.info>
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
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.gui.pivot.core.SplitPaneExtended;
import info.bonjean.beluga.response.Feedback;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.SplitPane;
import org.apache.pivot.wtk.TextInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StationUI extends SplitPane implements Bindable, SplitPaneExtended {
	private static Logger log = LoggerFactory.getLogger(StationUI.class);
	@BXML
	protected ImageView stationCover;
	@BXML
	protected TextInput stationNameInput;
	@BXML
	protected Label stationCreationDate;
	@BXML
	protected Label stationGenres;
	@BXML
	protected BoxPane lovedSongsPane;
	@BXML
	protected BoxPane bannedSongsPane;

	private final BelugaState state = BelugaState.getInstance();
	private Resources resources;

	public StationUI() {
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		this.resources = resources;
		stationNameInput.setText(state.getStation().getStationName());

		if (state.getStation().getArtUrl().isEmpty())
			stationCover.setImage(BelugaConfiguration.getInstance().getTheme().getBelugaPlayerImagePath());
		else {
			try {
				stationCover.setImage(new URL(state.getStation().getArtUrl()));
			} catch (Exception e) {
				stationCover.setImage(BelugaConfiguration.getInstance().getTheme().getBelugaPlayerImagePath());
			}
		}

		stationCreationDate.setText(
				new SimpleDateFormat("yyyy-MM-dd").format(new Date(state.getStation().getDateCreated().getTime())));
		StringBuffer sb = new StringBuffer();
		for (String genre : state.getStation().getGenre()) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(genre);
		}
		if (sb.length() > 0) {
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

	private MenuButton newFeedback(Feedback feedback) {
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
		Menu.Item menuItem = new Menu.Item(feedback.isPositive() ? resources.get("unlike") : resources.get("unban"));
		menuItem.setAction("delete-feedback");
		menuItem.getUserData().put("feedback", feedback);
		menuItem.getUserData().put("item", link);
		menuSection.add(menuItem);
		menu.getSections().add(menuSection);
		link.setMenu(menu);
		return link;
	}

	/**
	 *
	 * If a feedback has been deleted, we check if it is the song currently
	 * playing
	 *
	 */
	protected void updateSongFeedback(String feedbackId) {
		if (state.getSong() == null)
			return;

		for (Feedback feedback : state.getStation().getFeedback().getThumbsUp()) {
			if (feedback.getFeedbackId().equals(feedbackId)) {
				// this is not bulletproof but should be good enough for 99% of
				// cases.
				if (state.getSong().getArtistName().equals(feedback.getArtistName())
						&& state.getSong().getSongName().equals(feedback.getSongName())) {
					state.getSong().setSongRating(0);
					log.debug("Current song feedback updated");
				}
				return;
			}
		}
	}

	@Override
	public ImageView getImageView() {
		return stationCover;
	}

	@Override
	public int getPrimaryRegionReserved() {
		return 0;
	}

	@Override
	public int getPrimaryRegionPadding() {
		return 10;
	}

	@Override
	public void setPrimaryRegionWidth(int width) {
	}
}
