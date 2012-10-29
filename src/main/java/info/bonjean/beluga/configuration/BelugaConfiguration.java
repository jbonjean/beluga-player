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
package info.bonjean.beluga.configuration;

import info.bonjean.beluga.log.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class BelugaConfiguration
{
	private static final Logger log = new Logger(BelugaConfiguration.class);
	private static final String CONFIGURATION_DIRECTORY = System.getProperty("user.home") + "/.config/beluga";
	private static final String CONFIGURATION_FILE = CONFIGURATION_DIRECTORY + "/configuration.properties";
	private Properties properties;

	private static BelugaConfiguration instance;

	private BelugaConfiguration()
	{
	}

	public static BelugaConfiguration getInstance()
	{
		if (instance == null)
			instance = new BelugaConfiguration();

		return instance;
	}

	public void store()
	{
		try
		{
			properties.store(new FileOutputStream(CONFIGURATION_FILE), null);
		} catch (Exception e)
		{
			log.error("Cannot write configuration file");
			System.exit(-1);
		}
	}

	public void load()
	{
		// create configuration directory if necessary
		log.info("Load configuration");
		File directory = new File(CONFIGURATION_DIRECTORY);
		if (!directory.exists())
		{
			log.info("Configuration directory does not exists, creating it");
			directory.mkdirs();
		}

		// create configuration file if necessary
		File configurationFile = new File(CONFIGURATION_FILE);
		if (!configurationFile.exists())
		{
			log.info("Configuration file missing, creating it");
			try
			{
				configurationFile.createNewFile();
			} catch (IOException e)
			{
				log.error("Error creating configuration file");
				System.exit(-1);
			}
		}

		// load configuration file
		properties = new Properties();
		try
		{
			properties.load(new FileInputStream(CONFIGURATION_FILE));
		} catch (Exception e)
		{
			log.error("Cannot load configuration file");
			System.exit(-1);
		}

		// check everything is there
		for (Property property : Property.values())
		{
			String key = property.getKey();
			if (properties.get(key) == null)
				properties.put(key, "");
		}
		// synchronize file, test write
		store();
	}

	public void set(Property property, String value)
	{
		properties.setProperty(property.getKey(), value);
	}

	public String getString(Property property, String defaultValue)
	{
		if (properties.get(property.getKey()) == null)
			return defaultValue;

		return (String) properties.get(property.getKey());
	}

	public String getString(Property property)
	{
		return getString(property, null);
	}

	public Integer getInteger(Property property, Integer defaultValue)
	{
		if (properties.get(property.getKey()) == null)
			return defaultValue;

		return new Integer((String) properties.get(property.getKey()));
	}

	public Integer getInteger(Property property)
	{
		return getInteger(property, null);
	}

	public Long getLong(Property property)
	{
		return getLong(property, null);
	}

	private Long getLong(Property property, Long defaultValue)
	{
		if (properties.get(property.getKey()) == null)
			return defaultValue;

		return new Long((String) properties.get(property.getKey()));
	}

	public String getUserName()
	{
		return getString(Property.USER);
	}

	public void setUserName(String userName)
	{
		set(Property.USER, userName);
	}

	public String getPassword()
	{
		return getString(Property.PASSWORD);
	}

	public void setPassword(String password)
	{
		set(Property.PASSWORD, password);
	}

	public String getProxyServer()
	{
		return getString(Property.PROXY_HOST);
	}

	public void setProxyServer(String proxyServer)
	{
		set(Property.PROXY_HOST, proxyServer);
	}

	public int getProxyServerPort()
	{
		return getInteger(Property.PROXY_PORT);
	}

	public void setProxyServerPort(int proxyServerPort)
	{
		set(Property.PROXY_PORT, String.valueOf(proxyServerPort));
	}

	public String getDefaultStationId()
	{
		return getString(Property.DEFAULT_STATION);
	}

	public void setDefaultStationId(String defaultStationId)
	{
		set(Property.DEFAULT_STATION, defaultStationId);
	}
}
