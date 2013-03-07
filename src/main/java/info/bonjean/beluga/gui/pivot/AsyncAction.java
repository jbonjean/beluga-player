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

import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public abstract class AsyncAction extends Action
{
	private MainWindow mainWindow;
	private boolean disableUI;

	public AsyncAction(MainWindow mainWindow)
	{
		this.mainWindow = mainWindow;
		this.disableUI = true;
	}

	public AsyncAction(MainWindow mainWindow, boolean disableUI)
	{
		this.mainWindow = mainWindow;
		this.disableUI = disableUI;
	}

	@Override
	public final void perform(final Component source)
	{
		UIPools.actionPool.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (disableUI)
					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							mainWindow.setEnabled(false);
						}
					}, true);

				asyncPerform(source);

				if (disableUI)
					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							mainWindow.setEnabled(true);
						}
					}, true);
			}
		});
	}

	public abstract void asyncPerform(Component source);
}
