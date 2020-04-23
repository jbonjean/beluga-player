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

import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.util.ResourcesUtil;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.TablePane;

import java.net.URL;

public class PlayerUI extends TablePane implements Bindable {
	@BXML
	private Label stationName;
	@BXML
	private Label currentTime;
	@BXML
	private Label totalTime;
	@BXML
	private Meter progress;
	@BXML
	private Meter progressCache;
	@BXML
	protected LinkButton nextButton;
	@BXML
	protected LinkButton muteButton;

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		progressCache.getStyles().put("color", ResourcesUtil.getThemeColor(10));
		enableUI(false);
		resetUI();
	}

	public void resetUI() {
		stationName.setText("");
		currentTime.setText("00:00");
		totalTime.setText("00:00");
		progress.setPercentage(0);
		progressCache.setPercentage(0);
	}

	public void enableUI(boolean enabled) {
		PivotUI.enableComponent(muteButton, enabled);
		PivotUI.enableComponent(nextButton, enabled);
		PivotUI.enableComponent(currentTime, enabled);
		PivotUI.enableComponent(totalTime, enabled);
		PivotUI.enableComponent(progress, enabled);
		PivotUI.enableComponent(progressCache, enabled);
	}

	public Label getStationName() {
		return stationName;
	}

	public Label getCurrentTime() {
		return currentTime;
	}

	public Label getTotalTime() {
		return totalTime;
	}

	public Meter getProgress() {
		return progress;
	}

	public Meter getProgressCache() {
		return progressCache;
	}

	public LinkButton getNextButton() {
		return nextButton;
	}

	public LinkButton getMuteButton() {
		return muteButton;
	}
}
