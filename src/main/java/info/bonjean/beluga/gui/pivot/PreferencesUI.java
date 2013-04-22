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
import info.bonjean.beluga.client.LastFMSession;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.configuration.DNSProxy;
import info.bonjean.beluga.connection.BelugaHTTPClient;
import info.bonjean.beluga.log.Log;

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextInput;
import org.slf4j.Logger;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class PreferencesUI extends TablePane implements Bindable
{
	@Log
	private static Logger log;
	@BXML
	private TextInput emailAddressInput;
	@BXML
	private TextInput passwordInput;
	@BXML
	private TextInput lastFMUsernameInput;
	@BXML
	private TextInput lastFMPasswordInput;
	@BXML
	private Checkbox lastFMEnableCheckbox;
	@BXML
	private TextInput httpProxyHostInput;
	@BXML
	private TextInput httpProxyPortInput;
	@BXML
	private ListButton dnsProxyInput;
	@BXML
	private PushButton submitButton;

	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();

	public PreferencesUI()
	{
		Action.getNamedActions().put("submit", new AsyncAction(MainWindow.getInstance())
		{
			@Override
			public void asyncPerform(final Component source)
			{
				configuration.setUserName(emailAddressInput.getText());
				configuration.setPassword(passwordInput.getText());
				configuration.setProxyHost(httpProxyHostInput.getText());
				configuration.setProxyPort(httpProxyPortInput.getText());
				configuration.setDNSProxy(((DNSProxy) dnsProxyInput.getSelectedItem()).getId());
				configuration.setLastFMEnabled(lastFMEnableCheckbox.isSelected());
				configuration.setLastFMUsername(lastFMUsernameInput.getText());
				configuration.setLastFMPassword(lastFMPasswordInput.getText());
				configuration.setConfigurationVersion(BelugaState.getInstance().getVersion());
				configuration.store();

				log.info("preferencesUpdated");

				BelugaHTTPClient.reset();
				LastFMSession.reset();

				// redirect to the main screen: song if playback started, welcome otherwise
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						MainWindow.getInstance().loadPage(BelugaState.getInstance().isPlaybackStarted() ? "song" : "welcome");
					}
				}, false);
			}
		});
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		emailAddressInput.setText(configuration.getUserName());
		passwordInput.setText(configuration.getPassword());
		httpProxyHostInput.setText(configuration.getProxyHost());
		httpProxyPortInput.setText(configuration.getProxyPort());
		@SuppressWarnings("unchecked")
		List<Object> listData = (List<Object>) dnsProxyInput.getListData();
		for (DNSProxy dnsProxy : DNSProxy.values())
			listData.add(dnsProxy);
		dnsProxyInput.setSelectedItem(DNSProxy.get(configuration.getDNSProxy()));
		lastFMUsernameInput.setText(configuration.getLastFMUsername());
		lastFMPasswordInput.setText(configuration.getLastFMPassword());
		lastFMEnableCheckbox.setSelected(configuration.getLastFMEnabled());
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		if (enabled)
			submitButton.setAction("submit");
		else
			submitButton.setAction((Action) null);
		submitButton.setEnabled(enabled);
		emailAddressInput.setEnabled(enabled);
		passwordInput.setEnabled(enabled);
		httpProxyHostInput.setEnabled(enabled);
		httpProxyPortInput.setEnabled(enabled);
		dnsProxyInput.setEnabled(enabled);
		lastFMEnableCheckbox.setEnabled(enabled);
		lastFMUsernameInput.setEnabled(enabled);
		lastFMPasswordInput.setEnabled(enabled);
	}
}
