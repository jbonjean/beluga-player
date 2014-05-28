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
public class QuickMixUI extends TablePane implements Bindable
{
	@BXML
	protected BoxPane stationsPane;
	@BXML
	protected PushButton selectAllButton;
	@BXML
	protected PushButton deselectAllButton;
	@BXML
	protected PushButton submitButton;

	public QuickMixUI()
	{
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		// find the quickmix station, should be the first one but better be safe
		Station quickmixStation = null;
		for (Station station : BelugaState.getInstance().getStationList())
			if (station.isQuickMix())
				quickmixStation = station;

		// create the checkboxes
		for (Station station : BelugaState.getInstance().getStationList())
		{
			if (station.isQuickMix())
				continue;

			Checkbox checkbox = new Checkbox(station.getStationName());
			checkbox.getUserData().put("stationId", station.getStationId());

			// if the station is member of the quickmix, select it
			if (quickmixStation.getQuickMixStationIds().contains(station.getStationId()))
				checkbox.setSelected(true);

			// validator, disable submit button if no station selected
			checkbox.getButtonPressListeners().add(new ButtonPressListener()
			{
				@Override
				public void buttonPressed(Button button)
				{
					for (int i = 0; i < stationsPane.getLength(); i++)
					{
						Checkbox checkbox = (Checkbox) stationsPane.get(i);
						if (checkbox.isSelected())
						{
							PivotUI.enableComponent(submitButton, true);
							return;
						}
					}
					PivotUI.enableComponent(submitButton, false);
				}
			});

			stationsPane.add(checkbox);
		}

		selectAllButton.getButtonPressListeners().add(new ButtonPressListener()
		{
			@Override
			public void buttonPressed(Button button)
			{
				for (int i = 0; i < stationsPane.getLength(); i++)
				{
					Checkbox checkbox = (Checkbox) stationsPane.get(i);
					checkbox.setSelected(true);
				}
				PivotUI.enableComponent(submitButton, true);
			}
		});

		deselectAllButton.getButtonPressListeners().add(new ButtonPressListener()
		{
			@Override
			public void buttonPressed(Button button)
			{
				for (int i = 0; i < stationsPane.getLength(); i++)
				{
					Checkbox checkbox = (Checkbox) stationsPane.get(i);
					checkbox.setSelected(false);
				}
				PivotUI.enableComponent(submitButton, false);
			}
		});
	}
}
