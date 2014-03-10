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

import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Mouse.ScrollType;
import org.apache.pivot.wtk.Slider;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 * Adds support for mouse wheel.
 * 
 */
public class BelugaSliderSkin extends FixedTerraSliderSkin
{
	@Override
	public boolean mouseWheel(Component componentArgument, ScrollType scrollType, int scrollAmount, int wheelRotation, int x, int y)
	{
		// quick and dirty, we increase/decrease 10% of the value
		Slider slider = (Slider) BelugaSliderSkin.this.getComponent();
		int value = slider.getValue();
		int step = (int) (0.1 * (slider.getEnd() - slider.getStart()));

		if (wheelRotation > 0)
			value = Math.min(slider.getEnd(), value + step);
		else
			value = Math.max(slider.getStart(), value - step);

		slider.setValue(value);
		return true;
	}
}
