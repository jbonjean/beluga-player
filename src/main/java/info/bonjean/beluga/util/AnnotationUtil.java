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
package info.bonjean.beluga.util;

import info.bonjean.beluga.log.Log;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;
import com.impetus.annovention.listener.FieldAnnotationDiscoveryListener;

public class AnnotationUtil
{
	private static Logger log = LoggerFactory.getLogger(AnnotationUtil.class);

	public static void parseAnnotations()
	{
		Discoverer discoverer = new ClasspathDiscoverer();

		// Add class annotation listener (optional)
		discoverer.addAnnotationListener(new FieldAnnotationDiscoveryListener()
		{
			@Override
			public String[] supportedAnnotations()
			{
				return new String[] { Log.class.getName() };
			}

			@Override
			public void discovered(String clazz, String field, String annotation)
			{
				log.debug("Discovered Field(" + clazz + "." + field + ") with Annotation(" + annotation + ")");
				try
				{
					Class c = Class.forName(clazz);
					Field f = c.getDeclaredField(field);
					f.setAccessible(true);
					f.set(null, LoggerFactory.getLogger(c));
				}
				catch (Exception e)
				{
					log.error(e.getMessage(), e);
				}
			}
		});

		discoverer.discover(false, true, false, true, true);
	}
}
