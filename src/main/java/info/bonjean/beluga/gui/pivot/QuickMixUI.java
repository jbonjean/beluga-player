/* Copyright (C) 2012, 2013 Julien Bonjean <julien@bonjean.info>
 * 
 * This file is part of Beluga Player.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.bonjean.beluga.gui.pivot;

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.client.PandoraPlaylist;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.response.Station;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class QuickMixUI extends TablePane implements Bindable
{
	private static Logger log = LoggerFactory.getLogger(QuickMixUI.class);
	@BXML
	private BoxPane stationsPane;
	@BXML
	private PushButton selectAllButton;
	@BXML
	private PushButton deselectAllButton;
	@BXML
	private PushButton submitButton;

	public QuickMixUI()
	{
		Action.getNamedActions().put("submit", new AsyncAction(MainWindow.getInstance())
		{
			@Override
			public void asyncPerform(final Component source)
			{
				final List<String> quickMixStationIds = new ArrayList<String>();
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						for (int i = 0; i < stationsPane.getLength(); i++)
						{
							Checkbox station = (Checkbox) stationsPane.get(i);
							if (station.isSelected())
								quickMixStationIds.add((String) station.getUserData().get("stationId"));
						}
					}
				}, true);

				try
				{
					PandoraClient.getInstance().setQuickMix(quickMixStationIds);
					PandoraPlaylist.getInstance().clear();
					MainWindow.getInstance().updateStationsList();
				}
				catch (BelugaException e)
				{
					log.error(e.getMessage(), e);
				}

				// redirect to the main screen
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						MainWindow.getInstance().loadPage("song");
					}
				}, false);
			}
		});
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		// find the quickmix station, should be the first one but better be safe
		Station quickmixStation = null;
		for (Station station : BelugaState.getInstance().getStationList())
		{
			if (station.isQuickMix())
				quickmixStation = station;
		}

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
							PivotUI.setEnable(submitButton, true);
							return;
						}
					}
					PivotUI.setEnable(submitButton, false);
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
				PivotUI.setEnable(submitButton, true);
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
				PivotUI.setEnable(submitButton, false);
			}
		});
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		PivotUI.setEnable(submitButton, enabled);
	}
}
