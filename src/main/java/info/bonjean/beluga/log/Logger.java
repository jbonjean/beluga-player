/* Copyright (C) 2012 Julien Bonjean <julien@bonjean.info>
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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
package info.bonjean.beluga.log;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
@SuppressWarnings("rawtypes")
public class Logger
{
	private enum Level
	{
		INFO, WARN, ERROR;
	}

	private Class clazz;

	@SuppressWarnings("unused")
	private Logger()
	{
	}

	public Logger(Class clazz)
	{
		this.clazz = clazz;
	}

	private void log(Level level, String message)
	{
		System.out.println(level.name() + " [" + clazz.getSimpleName() + "] " + message);
	}

	public void info(String message)
	{
		log(Level.INFO, message);
	}

	public void warn(String message)
	{
		log(Level.WARN, message);
	}

	public void error(String message)
	{
		log(Level.ERROR, message);
	}
}
