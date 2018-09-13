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

import java.io.FileReader;
import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextArea;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class LogsUI extends TablePane implements Bindable
{
	@BXML
	TextArea displayArea;

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		try
		{
			displayArea.setText(new FileReader(System.getProperty("user.home")
					+ "/.log/beluga-player.log"));
		}
		catch (Exception e)
		{
			// do nothing, we just have nothing to display
		}
		if (displayArea.getText().isEmpty())
			displayArea.setText((String) resources.get("logFileEmpty"));
	}
}
