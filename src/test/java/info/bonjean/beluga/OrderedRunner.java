/*
 * Copyright (C) 2012-2016 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class OrderedRunner extends BlockJUnit4ClassRunner
{
	public OrderedRunner(Class klass) throws InitializationError
	{
		super(klass);
	}

	@Override
	protected List computeTestMethods()
	{
		List list = super.computeTestMethods();
		List copy = new ArrayList(list);
		Collections.sort(copy, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				return ((FrameworkMethod) o1).getName().compareTo(((FrameworkMethod) o2).getName());
			}
		});
		return copy;
	}
}
