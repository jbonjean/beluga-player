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

import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.log.StatusBarAppender;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AsyncAction extends Action {
	private static Logger log = LoggerFactory.getLogger(AsyncAction.class);

	private final MainWindow mainWindow;
	private final boolean disableMainWindowUI;
	private final boolean disablePlayerUI;
	private boolean enabled = true;

	public AsyncAction(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		this.disableMainWindowUI = true;
		this.disablePlayerUI = false;
	}

	public AsyncAction(MainWindow mainWindow, boolean disableMainWindowUI, boolean disablePlayerUI) {
		this.mainWindow = mainWindow;
		this.disableMainWindowUI = disableMainWindowUI;
		this.disablePlayerUI = disablePlayerUI;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		// stop propagation to bypass the bug that prevent having button with different enabled state linked to the
		// same action
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public final void perform(final Component source) {
		ThreadPools.actionPool.execute(new Runnable() {
			@Override
			public void run() {
				StatusBarAppender.clearErrorMessage();
				try {
					if (disableMainWindowUI || disablePlayerUI) {
						ApplicationContext.queueCallback(new Runnable() {
							@Override
							public void run() {
								if (disableMainWindowUI) {
									mainWindow.enableUI(false);
								}
								if (disablePlayerUI) {
									mainWindow.getPlayerUI().enableUI(false);
								}
							}
						}, true);
					}

					asyncPerform(source);
				} catch (Throwable e) {
					log.error(e.getMessage(), e);
				} finally {
					if (disableMainWindowUI || disablePlayerUI) {
						ApplicationContext.queueCallback(new Runnable() {
							@Override
							public void run() {
								if (disableMainWindowUI) {
									mainWindow.enableUI(true);
								}
								if (disablePlayerUI) {
									mainWindow.getPlayerUI().enableUI(true);
								}
							}
						}, true);
					}
					afterPerform();
				}
			}
		});
	}

	public void afterPerform() {
	}

	// BelugaException thrown inside an action must only be logged
	public abstract void asyncPerform(Component source) throws BelugaException;
}
