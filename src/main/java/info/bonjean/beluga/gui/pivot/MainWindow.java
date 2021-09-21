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
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.exception.InternalException;
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.log.StatusBarAppender;
import info.bonjean.beluga.util.ResourcesUtil;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.skin.WindowSkin;
import org.apache.pivot.wtk.skin.terra.TerraTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.net.URL;
import java.util.Iterator;

public class MainWindow extends Window implements Bindable {
	private static Logger log = LoggerFactory.getLogger(MainWindow.class);
	@BXML
	private TablePane.Row menuWrapper;
	@BXML
	private TablePane.Row contentWrapper;
	@BXML
	private TablePane.Row playerWrapper;
	@BXML
	private TablePane.Row statusBarWrapper;

	private PlayerUI playerUI;
	private StatusBarUI statusBarUI;
	private MenuUI menuUI;

	private final BelugaState state = BelugaState.getInstance();
	private final PandoraClient pandoraClient = PandoraClient.getInstance();

	private Component currentPageComponent;
	private String currentPageName;

	private Resources resources;
	private UIController uiDispatcher;

	public MainWindow() {
		getStyles().put("backgroundColor", ResourcesUtil.getThemeColor(4));

		uiDispatcher = new UIController(this);
		uiDispatcher.registerActions();
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		this.resources = resources;

		// Update Terra theme colors with the ones from selected theme.
		updateThemeColors();

		loadDynamicComponents();

		// Give a reference of the status bar to the logger.
		StatusBarAppender.setLabel(statusBarUI.statusBarText);

		// Also give resource for message translation.
		StatusBarAppender.setResources(resources);

		enableUI(true);
	}

	public Component getCurrentPageComponent() {
		return currentPageComponent;
	}

	@SuppressWarnings("unchecked")
	public <E extends Component> E getCurrentPageComponent(Class<E> clazz) throws BelugaException {
		if (clazz.isInstance(currentPageComponent))
			return (E) currentPageComponent;
		throw new InternalException("invalidInternalCall");
	}

	public String getCurrentPageName() {
		return currentPageName;
	}

	/*
	 * Keep everything related to enabled/disabled state here, it make it easier to ensure consistency between screens.
	 */
	public void enableUI(boolean enabled) {
		if (menuUI == null || playerUI == null)
			return;

		// Because of the Component class sealing, we cannot override it so we need to manually update enable state of
		// all components.
		recursiveEnableComponent(menuUI, enabled);
		Iterator<Component> contentIterator = contentWrapper.iterator();
		while (contentIterator.hasNext())
			recursiveEnableComponent(contentIterator.next(), enabled);

		// Get Pandora status.
		boolean connected = pandoraClient.isLoggedIn();

		// Enable/disable the loading and disconnected icons.
		statusBarUI.iconDisconnected.setVisible(!connected);
		statusBarUI.loader.setVisible(!enabled);

		// Enable/disable Pandora related features if connected.
		if (enabled && connected) {
			// There is no station details for the quickmix.
			if (state.getStation() != null && state.getStation().isQuickMix())
				PivotUI.enableComponent(menuUI.stationDetailsButton, false);
			else
				PivotUI.enableComponent(menuUI.stationDetailsButton, true);
		}
		menuUI.pandoraMenu.setEnabled(connected);
		menuUI.stationsSearch.setEnabled(true);
	}

	public PlayerUI getPlayerUI() {
		return playerUI;
	}

	private void recursiveEnableComponent(Component component, boolean enabled) {
		// Disable the parent component first to prevent any user interaction.
		if (!enabled)
			PivotUI.enableComponent(component, false);

		// If it's a container, handle the children (except for menu).
		if (component instanceof Container && !(component instanceof Menu)) {
			Iterator<Component> iterator = ((Container) component).iterator();
			while (iterator.hasNext())
				recursiveEnableComponent(iterator.next(), enabled);
		}

		// Enable the parent component.
		if (enabled)
			PivotUI.enableComponent(component, true);
	}

	public synchronized void loadPage(String bxml) {
		try {
			Component content = loadContainerSequence(bxml, contentWrapper);
			// Don't crash if we cannot load the page.
			if (content != null) {
				currentPageComponent = content;
				currentPageName = bxml;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public synchronized void reload() {
		try {
			// First reload i18n messages.
			resources = new Resources("i18n.messages");
		} catch (Exception e) {
			log.debug(e.getMessage());
		}

		// Update theme colors.
		updateThemeColors();

		// Reload visual components.
		loadDynamicComponents();

		enableUI(true);
	}

	private void updateThemeColors() {
		String[] baseColors = BelugaConfiguration.getInstance().getTheme().getBaseColors();
		TerraTheme theme = (TerraTheme) Theme.getTheme();
		for (int i = 0; i < 8; i++) {
			String colorCode = baseColors[i];
			Color baseColor = Color.decode(colorCode);
			theme.setBaseColor(i, baseColor);
		}

		// Manually override window skin.
		WindowSkin skin = (WindowSkin) getSkin();
		skin.setBackgroundColor(baseColors[1]);
	}

	private void loadDynamicComponents() {
		menuUI = (MenuUI) loadContainerSequence("menu", menuWrapper);
		if (menuUI == null) {
			fatal("Failed to load menu");
		}

		playerUI = (PlayerUI) loadContainerSequence("player", playerWrapper);
		if (playerUI == null) {
			fatal("Failed to load player");
		}

		statusBarUI = (StatusBarUI) loadContainerSequence("status_bar", statusBarWrapper);
		if (statusBarUI == null) {
			fatal("Failed to load status bar");
		}

		loadPage(currentPageName != null ? currentPageName : "welcome");
	}

	private Component loadContainerSequence(String bxml, Sequence<Component> target) {
		try {
			BXMLSerializer bxmlSerializer = ResourcesUtil.getBXMLSerializer();

			Component content = (Component) bxmlSerializer
					.readObject(MainWindow.class.getResource(PivotUI.BXML_PATH + bxml + ".bxml"), resources);
			target.remove(0, target.getLength());
			target.add(content);

			return content;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private void fatal(String message) {
		log.error(message);
		DesktopApplicationContext.exit(false);
		System.exit(1);
	}
}
