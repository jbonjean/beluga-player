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

import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.StackPane;
import org.apache.pivot.wtk.TablePane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class WelcomeUI extends TablePane implements Bindable
{
	private static Logger log = LoggerFactory.getLogger(WelcomeUI.class);
	@BXML
	private Label belugaVersion;
	@BXML
	private StackPane newVersionPane;
	@BXML
	private PushButton startPandoraButton;

	private final BelugaState state = BelugaState.getInstance();

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		belugaVersion.setText("Beluga Player " + state.getVersion());

		new Thread()
		{
			@Override
			public void run()
			{
				InputStream in = null;
				try
				{
					float currentVersion = Float.parseFloat(state.getVersion());
					in = new URL("http://jbonjean.github.io/beluga-player/VERSION")
							.openStream();
					float newVersion = Float.parseFloat(IOUtils.toString(in));

					if (newVersion > currentVersion)
					{
						ApplicationContext.queueCallback(new Runnable()
						{
							@Override
							public void run()
							{
								newVersionPane.setVisible(true);
							}
						}, true);
					}
					else
						log.debug("No new version available");
				}
				catch (Exception e)
				{
					log.debug(e.getMessage());
				}
				finally
				{
					if (in != null)
						IOUtils.closeQuietly(in);
				}
			}
		}.start();
	}
}
