/* Copyright (C) 2012, 2013 Julien Bonjean <julien@bonjean.info>
 * 
 * This file is part of Beluga Player.
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
package info.bonjean.beluga.misc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * 
 * @author Julien Bonjean
 * 
 *         I don't understand the choice of Apache Pivot to use JSON format for message bundles. I like JSON but I don't see any advantage in this
 *         case. I find it a lot hardier to maintain and less flexible.
 * 
 */
public class ResourceBundleToResources
{
	private static final String RESOURCES_PATH = "/home/jb/dev/beluga/src/main/resources/i18n/";
	private static final String[] RESOURCES = { "messages" };

	public static void main(String[] args)
	{
		for (String resourceName : RESOURCES)
		{
			try
			{
				String inputFile = RESOURCES_PATH + resourceName + ".properties";
				String outputFile = RESOURCES_PATH + resourceName + ".json";

				FileInputStream input = new FileInputStream(inputFile);
				PrintWriter output = new PrintWriter(outputFile);

				Properties properties = new Properties();
				properties.load(input);
				
				output.println("{");
				for (Object key : properties.keySet())
				{
					StringBuffer sb = new StringBuffer();
					sb.append(key);
					sb.append(":\"");
					sb.append(properties.getProperty((String)key));
					sb.append("\",");

					output.println(sb.toString());
				}
				output.println("}");
				output.flush();
				output.close();
				input.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
