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

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.configuration.ConnectionType;

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.EnumList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.content.ListItem;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class PreferencesUI extends TablePane implements Bindable
{
	@BXML
	protected TextInput emailAddressInput;
	@BXML
	protected TextInput passwordInput;
	@BXML
	protected TextInput lastFMUsernameInput;
	@BXML
	protected TextInput lastFMPasswordInput;
	@BXML
	protected Checkbox lastFMEnableCheckbox;
	@BXML
	protected ListButton connectionType;
	@BXML
	protected TextInput httpProxyHostInput;
	@BXML
	protected TextInput httpProxyPortInput;
	@BXML
	protected Checkbox adsEnableDetectionCheckbox;
	@BXML
	protected Checkbox adsEnableSilentCheckbox;
	@BXML
	protected PushButton submitButton;
	@BXML
	protected ListButton notificationsStyle;
	@BXML
	protected ListButton stationsOrderBy;
	@BXML
	protected Checkbox windowRestoreCheckbox;

	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();

	public PreferencesUI()
	{
	}

	private void setListButtonSelected(ListButton button, String value)
	{
		@SuppressWarnings("unchecked")
		List<ListItem> styleListData = (List<ListItem>) button.getListData();
		for (ListItem listItem : styleListData)
			if (listItem.getUserData().equals(value))
				button.setSelectedItem(listItem);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		emailAddressInput.setText(configuration.getUserName());
		passwordInput.setText(configuration.getPassword());
		setListButtonSelected(stationsOrderBy, configuration.getStationsOrderBy());
		connectionType.setListData(new EnumList(ConnectionType.class));
		connectionType.setSelectedItem(configuration.getConnectionType());
		httpProxyHostInput.setText(configuration.getProxyHost());
		httpProxyPortInput.setText(configuration.getProxyPortStr());
		lastFMUsernameInput.setText(configuration.getLastFMUsername());
		lastFMPasswordInput.setText(configuration.getLastFMPassword());
		lastFMEnableCheckbox.setSelected(configuration.getLastFMEnabled());
		adsEnableDetectionCheckbox.setSelected(configuration.getAdsDetectionEnabled());
		adsEnableSilentCheckbox.setSelected(configuration.getAdsSilenceEnabled());
		setListButtonSelected(notificationsStyle, configuration.getNotificationsStyle());
		setListButtonSelected(stationsOrderBy, configuration.getStationsOrderBy());
		windowRestoreCheckbox.setSelected(configuration.getWindowRestoreEnabled());
	}
}
