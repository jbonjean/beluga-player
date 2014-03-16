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
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.log.StatusBarAppender;

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class MainWindow extends Window implements Bindable
{
	private static Logger log = LoggerFactory.getLogger(MainWindow.class);
	@BXML
	protected TablePane.Row contentWrapper;
	@BXML
	protected PlayerUI playerUI;
	@BXML
	protected MenuBar.Item pandoraMenu;
	@BXML
	protected Menu.Item deleteStationButton;
	@BXML
	protected Menu.Item stationDetailsButton;
	@BXML
	protected MenuUI menuUI;
	@BXML
	protected Label statusBarText;
	@BXML
	protected ImageView statusBarIcon;
	@BXML
	protected ImageView statusBarIconDiconnected;
	@BXML
	protected Prompt confirmStationDelete;
	@BXML
	protected ActivityIndicator loader;
	@BXML
	protected LinkButton backButton;

	private final BelugaState state = BelugaState.getInstance();
	protected Resources resources;
	private static MainWindow instance;
	private UIController uiDispatcher;

	public MainWindow()
	{
		instance = this;
		uiDispatcher = new UIController(this);
		uiDispatcher.registerActions();
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		this.resources = resources;

		// give a reference of the status bar to the logger
		StatusBarAppender.setLabel(statusBarText);
		StatusBarAppender.setIcon(statusBarIcon);
		// also give resource for message translation
		StatusBarAppender.setResources(resources);

		// load temporary screen
		loadPage("welcome");

		// ensure states aare up-to-date
		setEnabled(true);

		uiDispatcher.initialize();
	}

	@Override
	public synchronized void setEnabled(boolean enabled)
	{
		menuUI.setEnabled(enabled);
		state.getPage().getComponent().setEnabled(enabled);
		playerUI.setEnabled(enabled);
		loader.setVisible(!enabled);
		PivotUI.setEnable(backButton, enabled && !state.getPage().getName().equals("welcome")
				&& !state.getPage().getName().equals("song"));
	}

	public synchronized void loadPage(String bxml)
	{
		try
		{
			BXMLSerializer bxmlSerializer = new BXMLSerializer();
			Component content = (Component) bxmlSerializer.readObject(
					MainWindow.class.getResource(PivotUI.BXML_PATH + bxml + ".bxml"), resources);
			contentWrapper.remove(0, contentWrapper.getLength());
			contentWrapper.add(content);
			state.setPage(new Page(bxml, content));
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
		}
	}

	public static MainWindow getInstance()
	{
		return instance;
	}
}
