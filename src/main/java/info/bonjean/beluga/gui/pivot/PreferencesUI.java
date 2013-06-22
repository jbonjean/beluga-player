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
import info.bonjean.beluga.configuration.CustomAction;
import info.bonjean.beluga.configuration.DNSProxy;
import info.bonjean.beluga.connection.BelugaHTTPClient;
import info.bonjean.beluga.log.Log;

import java.net.URL;
import java.util.ArrayList;

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
	private TablePane customActionsTablePane;
	@BXML
	private PushButton submitButton;

	private ListButton[] actionTypes;
	private TextInput[] actionNames;
	private TextInput[] actionValues;

	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();

	public static final int CUSTOM_ACTIONS_COUNT = 4;

	private final class ActionTypeData
	{
		private CustomAction.Type type;
		private String name;

		public ActionTypeData(CustomAction.Type type, String name)
		{
			this.type = type;
			this.name = name;
		}

		public CustomAction.Type getType()
		{
			return type;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

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

				java.util.List<CustomAction> customActions = new ArrayList<CustomAction>();
				for (int i = 0; i < CUSTOM_ACTIONS_COUNT; i++)
				{
					CustomAction.Type type = ((ActionTypeData) actionTypes[i].getSelectedItem()).getType();
					String name = actionNames[i].getText();
					String action = actionValues[i].getText();

					CustomAction customAction = new CustomAction(type, name, action);
					if (customAction.isValid())
						customActions.add(customAction);
				}
				configuration.setCustomActions(customActions);

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
						MainWindow.getInstance().updateCustomActionsMenuSection();
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
		initializeCustomActions(resources);
	}

	private void initializeCustomActions(Resources resources)
	{
		actionTypes = new ListButton[CUSTOM_ACTIONS_COUNT];
		actionNames = new TextInput[CUSTOM_ACTIONS_COUNT];
		actionValues = new TextInput[CUSTOM_ACTIONS_COUNT];

		java.util.List<CustomAction> customActions = configuration.getCustomActions();
		for (int i = 0; i < CUSTOM_ACTIONS_COUNT; i++)
		{
			CustomAction customAction = customActions.size() > i ? configuration.getCustomActions().get(i) : null;

			TablePane.Row row = new TablePane.Row();
			customActionsTablePane.getRows().add(row);

			ListButton actionType = new ListButton();
			@SuppressWarnings("unchecked")
			List<ActionTypeData> actionTypeListData = (List<ActionTypeData>) actionType.getListData();
			for (CustomAction.Type type : CustomAction.Type.values())
			{
				ActionTypeData item = new ActionTypeData(type, (String) resources.get(type.getKey()));
				actionTypeListData.add(item);
				if (customAction != null && type == customAction.getType())
					actionType.setSelectedItem(item);
			}
			if (customAction == null)
				actionType.setSelectedIndex(0);
			row.add(actionType);
			actionTypes[i] = actionType;

			TextInput actionName = new TextInput();
			actionName.setPrompt((String) resources.get("name"));
			if (customAction != null)
				actionName.setText(customAction.getName());
			row.add(actionName);
			actionNames[i] = actionName;

			TextInput actionValue = new TextInput();
			actionValue.setPrompt((String) resources.get("action"));
			if (customAction != null)
				actionValue.setText(customAction.getAction());
			row.add(actionValue);
			actionValues[i] = actionValue;
		}
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
