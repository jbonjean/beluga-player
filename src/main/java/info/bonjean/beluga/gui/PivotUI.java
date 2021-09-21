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
package info.bonjean.beluga.gui;

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.gui.pivot.MainWindow;
import info.bonjean.beluga.gui.pivot.core.*;
import info.bonjean.beluga.util.ResourcesUtil;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PivotUI implements Application {
	public static final String BXML_PATH = "/bxml/";
	private Window window = null;

	public static void startDesktopUI() {
		try {
			Preferences preferences = Preferences.userNodeForPackage(DesktopApplicationContext.class);
			preferences = preferences.node(PivotUI.class.getName());
			if (!BelugaConfiguration.getInstance().getWindowRestoreEnabled()) {
				preferences.clear();
				preferences.putInt("width", 600);
				preferences.putInt("height", 415);
			}
			preferences.putBoolean("resizable", false);
			preferences.flush();
		} catch (BackingStoreException e) {
		}

		Theme.getTheme().set(LinkButton.class, FixedTerraLinkButtonSkin.class);
		Theme.getTheme().set(MenuButton.class, BelugaMenuButtonSkin.class);
		Theme.getTheme().set(SplitPane.class, BelugaSplitPaneSkin.class);
		Theme.getTheme().set(SuggestionPopup.class, BelugaSuggestionPopupSkin.class);
		Theme.getTheme().set(TextInput.class, BelugaTextInputSkin.class);
		Theme.getTheme().set(FillPane.class, BelugaFillPaneSkin.class);
		DesktopApplicationContext.main(PivotUI.class, new String[] {});
	}

	public static void enableComponent(Component component, boolean enabled) {
		if (component instanceof Button) {
			Button button = (Button) component;
			// Sync action state to prevent inconsistent exceptions.
			if (button.getAction() != null)
				button.getAction().setEnabled(enabled);

			button.setEnabled(enabled);

			// Re-enable the action, it could be used by other buttons.
			if (button.getAction() != null)
				button.getAction().setEnabled(true);
		} else
			component.setEnabled(enabled);
	}

	@Override
	public void startup(Display display, Map<String, String> properties) throws Exception {
		window = (Window) ResourcesUtil.getBXMLSerializer()
				.readObject(MainWindow.class.getResource(BXML_PATH + "main.bxml"), new Resources("i18n.messages"));
		window.open(display);
	}

	@Override
	public boolean shutdown(boolean optional) {
		if (window != null) {
			window.close();
		}
		return false;
	}

	@Override
	public void suspend() {
	}

	@Override
	public void resume() {
	}
}
