/*
 * Copyright (C) 2012-2018 Julien Bonjean <julien@bonjean.info>
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
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.response.Station;
import java.net.URL;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;

/**
 *
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class StationsUI extends TablePane implements Bindable {
	@BXML
	protected BoxPane stationsPane;
	@BXML
	protected PushButton selectAllButton;
	@BXML
	protected PushButton deselectAllButton;
	@BXML
	protected PushButton submitButton;

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		// create the checkboxes
		for (Station station : BelugaState.getInstance().getStationList()) {
			if (station.isQuickMix())
				continue;

			Checkbox checkbox = new Checkbox(station.getStationName());
			checkbox.getUserData().put("stationId", station.getStationId());

			checkbox.getButtonPressListeners().add(new ButtonPressListener() {
				@Override
				public void buttonPressed(Button button) {
					PivotUI.enableComponent(submitButton, canSubmit());
				}
			});

			stationsPane.add(checkbox);
		}

		selectAllButton.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button button) {
				for (int i = 0; i < stationsPane.getLength(); i++) {
					Checkbox checkbox = (Checkbox) stationsPane.get(i);
					checkbox.setSelected(true);
				}
				PivotUI.enableComponent(submitButton, false);
			}
		});

		deselectAllButton.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button button) {
				for (int i = 0; i < stationsPane.getLength(); i++) {
					Checkbox checkbox = (Checkbox) stationsPane.get(i);
					checkbox.setSelected(false);
				}
				PivotUI.enableComponent(submitButton, false);
			}
		});

		PivotUI.enableComponent(submitButton, false);
	}

	private boolean canSubmit() {
		// we need both a selected and unselect entries to enable the submit
		// (unselected because we want to ensure there is always at least one station)
		boolean hasSelectedEntry = false;
		boolean hasUnSelectedEntry = false;
		for (int i = 0; i < stationsPane.getLength(); i++) {
			Checkbox checkbox = (Checkbox) stationsPane.get(i);
			if (checkbox.isSelected())
				hasSelectedEntry = true;
			else
				hasUnSelectedEntry = true;
			if (hasSelectedEntry && hasUnSelectedEntry) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		PivotUI.enableComponent(submitButton, canSubmit());
	}
}
