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

import info.bonjean.beluga.Main;
import info.bonjean.beluga.log.Log;

import java.lang.reflect.Field;

import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationUtil
{
	private static Logger log = LoggerFactory.getLogger(AnnotationUtil.class);
	
	public static void parseAnnotations()
	{
		try
		{
			AnnotationDB db = new AnnotationDB();
			db.scanArchives(Main.class.getClassLoader().getResource(""));
			for (String annotatedClassName : db.getAnnotationIndex().get(Log.class.getName()))
			{
				Field field = null;
				@SuppressWarnings("rawtypes")
				Class clazz =  Class.forName(annotatedClassName);
				try
				{
					field = clazz.getDeclaredField("log");
					if (field.getType() != Logger.class)
					{
						log.debug("Field named 'log' found but not of type org.slf4j.Logger");
						field = null;
					}
				}
				catch (Exception e)
				{
					log.debug("Field named 'log' not found for @Log, this slows down annotation processing");
				}

				// go for the hard way
				if(field == null)
				{
					for (Field f : clazz.getDeclaredFields())
					{
						if (f.getType() == Logger.class && f.getAnnotation(Log.class) != null)
							field = f;
					}
				}

				if (field == null)
				{
					log.error("Something bad happened during annotation parsing");
					System.exit(-1);
				}
				
				// do the injection
				field.setAccessible(true);
				field.set(null, LoggerFactory.getLogger(clazz));
			}
		}
		catch (Exception e)
		{
			log.error(e.getMessage(),e);
			System.exit(-1);
		}
	}
}
