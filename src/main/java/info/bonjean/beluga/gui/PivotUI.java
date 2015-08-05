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
package info.bonjean.beluga.gui;

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.gui.pivot.MainWindow;
import info.bonjean.beluga.gui.pivot.core.BelugaMenuButtonSkin;
import info.bonjean.beluga.gui.pivot.core.BelugaSplitPaneSkin;
import info.bonjean.beluga.gui.pivot.core.BelugaSuggestionPopupSkin;
import info.bonjean.beluga.gui.pivot.core.BelugaTextInputSkin;
import info.bonjean.beluga.gui.pivot.core.FixedTerraLinkButtonSkin;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.SplitPane;
import org.apache.pivot.wtk.SuggestionPopup;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;

/**
 *
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class PivotUI implements Application
{
	private Window window = null;
	public static final String BXML_PATH = "/bxml/";

	public static void startDesktopUI()
	{
		try
		{
			Preferences preferences = Preferences
					.userNodeForPackage(DesktopApplicationContext.class);
			preferences = preferences.node(PivotUI.class.getName());
			if (!BelugaConfiguration.getInstance().getWindowRestoreEnabled())
			{
				preferences.clear();
				preferences.putInt("width", 600);
				preferences.putInt("height", 415);
			}
			preferences.putBoolean("resizable", false);
			preferences.flush();
		}
		catch (BackingStoreException e)
		{
		}

		Theme.getTheme().set(LinkButton.class, FixedTerraLinkButtonSkin.class);
		Theme.getTheme().set(MenuButton.class, BelugaMenuButtonSkin.class);
		Theme.getTheme().set(SplitPane.class, BelugaSplitPaneSkin.class);
		Theme.getTheme().set(SuggestionPopup.class, BelugaSuggestionPopupSkin.class);
		Theme.getTheme().set(TextInput.class, BelugaTextInputSkin.class);
		DesktopApplicationContext.main(PivotUI.class, new String[] {});
	}

	public static void enableComponent(Component component, boolean enabled)
	{
		if (component instanceof Button)
		{
			Button button = (Button) component;
			// sync action to prevent the inconsistent exception
			if (button.getAction() != null)
				button.getAction().setEnabled(enabled);

			button.setEnabled(enabled);

			// re-enable the action, it could be used by other buttons!
			if (button.getAction() != null)
				button.getAction().setEnabled(true);
		}
		else
			component.setEnabled(enabled);
	}

	@Override
	public void startup(Display display, Map<String, String> properties) throws Exception
	{
		BXMLSerializer bxmlSerializer = new BXMLSerializer();
		window = (Window) bxmlSerializer.readObject(MainWindow.class.getResource(BXML_PATH
				+ "main.bxml"), new Resources("i18n.messages"));
		window.open(display);
	}

	@Override
	public boolean shutdown(boolean optional)
	{
		if (window != null)
		{
			window.close();
		}

		return false;
	}

	@Override
	public void suspend()
	{
	}

	@Override
	public void resume()
	{
	}
}
