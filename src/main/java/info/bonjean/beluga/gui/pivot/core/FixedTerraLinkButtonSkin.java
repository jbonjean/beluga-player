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
package info.bonjean.beluga.gui.pivot.core;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.skin.terra.TerraLinkButtonSkin;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 * Fixes the NPE in ButtonDataRenderer when using an icon and the fillIcon=true
 * property value.
 * 
 */
public class FixedTerraLinkButtonSkin extends TerraLinkButtonSkin
{
	private Insets padding;

	public FixedTerraLinkButtonSkin()
	{
		super();
		padding = new Insets(0, 0, 0, 0);
	}

	public Insets getPadding()
	{
		return padding;
	}

	public void setPadding(Insets padding)
	{
		if (padding == null)
		{
			throw new IllegalArgumentException("padding is null.");
		}

		this.padding = padding;
		invalidateComponent();
	}

	public final void setPadding(Dictionary<String, ?> padding)
	{
		if (padding == null)
		{
			throw new IllegalArgumentException("padding is null.");
		}
		setPadding(new Insets(padding));
	}

	public final void setPadding(int padding)
	{
		setPadding(new Insets(padding));
	}

	public final void setPadding(Number padding)
	{
		if (padding == null)
		{
			throw new IllegalArgumentException("padding is null.");
		}
		setPadding(padding.intValue());
	}

	public final void setPadding(String padding)
	{
		if (padding == null)
		{
			throw new IllegalArgumentException("padding is null.");
		}
		setPadding(Insets.decode(padding));
	}
}
