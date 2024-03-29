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

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.gui.pivot.core.SplitPaneExtended;
import info.bonjean.beluga.util.ResourcesUtil;

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.SplitPane;

public class SongUI extends SplitPane implements Bindable, SplitPaneExtended {
	@BXML
	protected Label songTitle;
	@BXML
	protected Label albumTitle;
	@BXML
	protected Label artistName;
	@BXML
	protected Label songTraits;
	@BXML
	protected ImageView albumCover;
	@BXML
	protected PushButton likeButton;
	@BXML
	protected PushButton banButton;
	@BXML
	protected PushButton sleepButton;

	private final BelugaState state = BelugaState.getInstance();

	public SongUI() {
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		songTitle.setText(state.getSong().getSongName());
		albumTitle.setText(ResourcesUtil.shorten(state.getSong().getAlbumName(), 80));
		artistName.setText("by " + state.getSong().getArtistName());

		StringBuffer focusTraits = new StringBuffer();
		for (String focusTrait : state.getSong().getFocusTraits()) {
			if (focusTraits.length() > 0)
				focusTraits.append(", ");

			focusTraits.append(focusTrait);
		}
		if (focusTraits.length() > 0) {
			// capitalize first letter
			focusTraits.setCharAt(0, Character.toUpperCase(focusTraits.charAt(0)));

			// finish with a period
			focusTraits.append(".");
		}

		songTraits.setText(focusTraits.toString());

		try {
			albumCover.setImage(new URL(state.getSong().getAlbumArtUrl()));
		} catch (Exception e) {
			albumCover.setImage(BelugaConfiguration.getInstance().getTheme().getBelugaPlayerImagePath());
		}

		PivotUI.enableComponent(likeButton, state.getSong().getSongRating() < 1);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		PivotUI.enableComponent(likeButton, state.getSong().getSongRating() > 0 ? false : enabled);
	}

	@Override
	public int getPrimaryRegionReserved() {
		// space reserved for the album title
		return 35;
	}

	@Override
	public ImageView getImageView() {
		return albumCover;
	}

	@Override
	public void setPrimaryRegionWidth(int width) {
		// set width to trigger text wrap
		albumTitle.setWidthLimits(0, width);
	}

	@Override
	public int getPrimaryRegionPadding() {
		return 10;
	}
}
