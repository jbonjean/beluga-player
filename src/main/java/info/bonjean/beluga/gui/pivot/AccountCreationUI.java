/*
 * Copyright (C) 2012-2021 Julien Bonjean <julien@bonjean.info>
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

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextInput;

public class AccountCreationUI extends TablePane implements Bindable {
	@BXML
	protected TextInput emailAddressInput;
	@BXML
	protected TextInput passwordInput;
	@BXML
	protected TextInput birthYearInput;
	@BXML
	protected TextInput zipCodeInput;
	@BXML
	protected ButtonGroup genderGroup;
	@BXML
	protected Checkbox emailOptInInput;
	@BXML
	protected Checkbox termsOfUseInput;
	@BXML
	protected PushButton submitButton;

	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();

	public AccountCreationUI() {
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		emailAddressInput.setText(configuration.getUserName());
		passwordInput.setText(configuration.getPassword());
	}
}
