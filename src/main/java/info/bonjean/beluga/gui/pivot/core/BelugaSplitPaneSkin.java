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
package info.bonjean.beluga.gui.pivot.core;

import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.SplitPane;
import org.apache.pivot.wtk.media.Drawing;
import org.apache.pivot.wtk.skin.terra.TerraSplitPaneSkin;

/**
 * Add support for ratio keep of the left side.
 */
public class BelugaSplitPaneSkin extends TerraSplitPaneSkin {
	// fix for SVG (no max size)
	private static final int DRAWING_MAX_HEIGHT = 500;

	@Override
	public void layout() {
		SplitPane splitPane = (SplitPane) getComponent();
		int splitPaneHeight = getHeight();
		int splitPaneWidth = getWidth();
		int leftWidth = splitPaneHeight;
		if (splitPane instanceof SplitPaneExtended) {
			SplitPaneExtended splitPaneExtended = (SplitPaneExtended) splitPane;
			ImageView imageView = splitPaneExtended.getImageView();
			if (imageView != null && imageView.getImage() != null) {
				int imageHeight = splitPaneHeight - splitPaneExtended.getPrimaryRegionReserved()
						- 2 * splitPaneExtended.getPrimaryRegionPadding();
				int imageMaxHeight = imageView.getImage() instanceof Drawing ? DRAWING_MAX_HEIGHT
						: imageView.getImage().getHeight();

				// ensure we don't exceed the image original size
				if (imageMaxHeight > 0)
					imageHeight = Math.min(imageHeight, imageMaxHeight);

				// ensure the image don't take too much width
				imageHeight = Math.min(imageHeight, splitPaneWidth / 2);

				// prevent negative value
				imageHeight = Math.max(imageHeight, 0);

				// ensure image respect the expected size
				imageView.setPreferredHeight(imageHeight);
				imageView.setPreferredWidth(imageHeight);

				// give the width information to parent component
				splitPaneExtended.setPrimaryRegionWidth(imageHeight);

				// calculate effective width, considering the padding
				leftWidth = imageHeight + 2 * splitPaneExtended.getPrimaryRegionPadding();
			}
		}
		splitPane.setSplitRatio((float) leftWidth / getWidth());
		super.layout();
	}
}
