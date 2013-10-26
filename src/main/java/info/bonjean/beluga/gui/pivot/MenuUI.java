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
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.response.Station;

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.Menu.Section;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuBar.Item;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.TablePane;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class MenuUI extends TablePane implements Bindable
{
	@BXML
	private MenuBar menubar;
	@BXML
	private MenuButton stations;

	private final BelugaState state = BelugaState.getInstance();
	// keep track of stations entry enabled status
	// as it will be manually enabled/disabled
	boolean stationsEnabled = false;

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		if (System.getProperty("debug") != null)
		{
			Menu.Item debugEntry = new Menu.Item("Refresh");
			debugEntry.setAction(Action.getNamedActions().get("load"));
			menubar.getItems().get(0).getMenu().getSections().get(0).insert(debugEntry, 0);
		}
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		for (Item item : menubar.getItems())
			item.setEnabled(enabled);

		if (enabled)
			stations.setEnabled(stationsEnabled);
		else
			stations.setEnabled(false);
	}

	public void setStationsEnabled(boolean enabled)
	{
		stationsEnabled = enabled;
	}

	public void updateStationsListMenu()
	{
		// rebuild menu entry
		Section section = stations.getMenu().getSections().get(0);
		section.remove(0, section.getLength());
		for (Station station : state.getStationList())
		{
			Menu.Item item = new Menu.Item(station.getStationName());
			item.getUserData().put("station", station);
			item.setAction(Action.getNamedActions().get("stationSelect"));
			if (state.getStation() != null && state.getStation().getStationId().equals(station.getStationId()))
				PivotUI.setEnable(item, false);
			section.add(item);
		}
	}
}
