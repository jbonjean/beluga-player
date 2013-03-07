/* Copyright (C) 2012, 2013 Julien Bonjean <julien@bonjean.info>
 * 
 * This file is part of Beluga.
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

import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.exception.PandoraException;
import info.bonjean.beluga.log.Log;

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextInput;
import org.slf4j.Logger;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class AccountCreationUI extends TablePane implements Bindable
{
	@Log
	private static Logger log;
	
	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private final PandoraClient pandoraClient = PandoraClient.getInstance();

	@BXML
	TextInput emailAddressInput;
	@BXML
	TextInput passwordInput;
	@BXML
	TextInput birthYearInput;
	@BXML
	TextInput zipCodeInput;
	@BXML
	ButtonGroup genderGroup;
	@BXML
	Checkbox emailOptInInput;
	@BXML
	Checkbox termsOfUseInput;
	@BXML
	PushButton submitButton;
	@BXML
	Label errorArea;

	public AccountCreationUI()
	{
		Action.getNamedActions().put("submit", new AsyncAction(MainWindow.getInstance())
		{
			private void displayError(final String message)
			{
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						errorArea.setText(message);
					}
				}, true);
			}

			@Override
			public void asyncPerform(final Component source)
			{
				if (!termsOfUseInput.isSelected())
				{
					displayError("You must accept the Terms of Use");
					return;
				}
				try
				{
					pandoraClient.partnerLogin();
					pandoraClient.createUser(emailAddressInput.getText(), passwordInput.getText(), birthYearInput.getText(), zipCodeInput.getText(),
							(String) genderGroup.getSelection().getUserData().get("value"), String.valueOf(emailOptInInput.isSelected()));
					log.info("accountCreated");
					configuration.setUserName(emailAddressInput.getText());
					configuration.setPassword(passwordInput.getText());
					configuration.store();
				}
				catch (BelugaException e)
				{
					if (e instanceof PandoraException)
					{
						PandoraException pe = (PandoraException) e;
						displayError(pe.getError().getMessageKey());
						return;
					}
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		emailAddressInput.setText(configuration.getUserName());
		passwordInput.setText(configuration.getPassword());
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

		birthYearInput.setEnabled(enabled);
		zipCodeInput.setEnabled(enabled);
		emailOptInInput.setEnabled(enabled);
		termsOfUseInput.setEnabled(enabled);
	}
}
