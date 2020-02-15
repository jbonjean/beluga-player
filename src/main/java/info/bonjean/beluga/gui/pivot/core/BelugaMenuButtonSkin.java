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
package info.bonjean.beluga.gui.pivot.core;

import java.awt.Graphics2D;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.skin.terra.TerraMenuButtonSkin;

/**
 * Simplifies the menu-button skin to behave the same way as other menu
 * buttons.
 * Also fixes the focusable value to false (should be configurable?).
 */
public class BelugaMenuButtonSkin extends TerraMenuButtonSkin {
	@Override
	public boolean isFocusable() {
		return false;
	}

	@Override
	public void paint(Graphics2D graphics) {
		MenuButton menuButton = (MenuButton) getComponent();

		int width = getWidth();
		int height = getHeight();

		Bounds contentBounds = new Bounds(getPadding().left + 1, getPadding().top + 1,
				Math.max(width - (getPadding().left + getPadding().right + getSpacing() + 2), 0),
				Math.max(height - (getPadding().top + getPadding().bottom + 2), 0));
		Button.DataRenderer dataRenderer = menuButton.getDataRenderer();
		dataRenderer.render(menuButton.getButtonData(), menuButton, highlighted);
		dataRenderer.setSize(contentBounds.width, contentBounds.height);

		Graphics2D contentGraphics = (Graphics2D) graphics.create();
		contentGraphics.translate(contentBounds.x, contentBounds.y);
		contentGraphics.clipRect(0, 0, contentBounds.width, contentBounds.height);
		dataRenderer.paint(contentGraphics);
		contentGraphics.dispose();
	}
}
