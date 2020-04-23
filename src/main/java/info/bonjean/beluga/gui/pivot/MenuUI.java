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
import info.bonjean.beluga.response.Station;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Mouse.Button;

import java.net.URL;

public class MenuUI extends TablePane implements Bindable {
	@BXML
	protected MenuBar menubar;
	@BXML
	protected TextInput stationsSearch;
	@BXML
	protected MenuBar.Item pandoraMenu;
	@BXML
	protected Menu.Item stationDetailsButton;

	protected SuggestionPopup stationsPopup = new SuggestionPopup();

	private final BelugaState state = BelugaState.getInstance();

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {

		if (System.getProperty("debug") != null) {
			Menu.Item debugEntry = new Menu.Item("Refresh");
			debugEntry.setAction(Action.getNamedActions().get("debug-refresh"));
			menubar.getItems().get(0).getMenu().getSections().get(0).insert(debugEntry, 0);
		}

		stationsSearch.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener() {
			@Override
			public boolean mouseUp(Component component, Button button, int x, int y) {
				return false;
			}

			@Override
			public boolean mouseDown(Component component, Button button, int x, int y) {
				return false;
			}

			@Override
			public boolean mouseClick(Component component, Button button, int x, int y, int count) {
				showPopup();
				return true;
			}
		});

		stationsSearch.getComponentKeyListeners().add(new ComponentKeyListener() {
			@Override
			public boolean keyTyped(Component component, char character) {
				showPopup();
				return false;
			}

			@Override
			public boolean keyReleased(Component component, int keyCode, KeyLocation keyLocation) {
				return false;
			}

			@Override
			public boolean keyPressed(Component component, int keyCode, KeyLocation keyLocation) {
				return false;
			}
		});
	}

	private void showPopup() {
		String text = stationsSearch.getText();
		String[] textParts = text.replaceAll("\\s+", " ").trim().toLowerCase().split(" ");
		org.apache.pivot.collections.ArrayList<Station> suggestions = new org.apache.pivot.collections.ArrayList<Station>();

		for (Station station : state.getStationList()) {
			if (state.getStation() != null && state.getStation().getStationId().equals(station.getStationId()))
				continue;

			boolean contained = true;
			for (String textPart : textParts) {
				if (!station.getStationName().toLowerCase().contains(textPart)) {
					contained = false;
					break;
				}
			}

			if (contained)
				suggestions.add(station);
		}

		stationsPopup.setSuggestionData(suggestions);
		stationsPopup.open(stationsSearch, new SuggestionPopupCloseListener() {
			@Override
			public void suggestionPopupClosed(SuggestionPopup suggestionPopup) {
				stationsSearch.setText("");
				Station station = (Station) suggestionPopup.getSelectedSuggestion();
				if (station == null)
					return;

				suggestionPopup.getUserData().put("station", station);
				Action.getNamedActions().get("select-station").perform(suggestionPopup);
			}
		});
	}
}
