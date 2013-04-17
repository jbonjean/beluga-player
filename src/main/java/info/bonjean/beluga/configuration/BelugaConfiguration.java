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
package info.bonjean.beluga.configuration;

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.log.Log;
import info.bonjean.beluga.util.CryptoUtil;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class BelugaConfiguration
{
	@Log
	private static Logger log;
	private static final String CONFIGURATION_DIRECTORY = System.getProperty("user.home") + "/.config/beluga";
	private static final String CONFIGURATION_FILE = CONFIGURATION_DIRECTORY + "/configuration.properties";
	private PropertiesConfiguration properties;

	private static BelugaConfiguration instance;

	private BelugaConfiguration()
	{
	}

	// for migration from 0.* to 0.6
	private void propertiesMigrationV0_6()
	{
		// dns proxy format changed
		if (DNSProxy.get(getDNSProxy()) == null)
		{
			log.debug("Migrating DNS proxy settings");
			// if proxy invalid (IP address), we set proxy DNS
			setDNSProxy(DNSProxy.PROXY_DNS.getId());
		}

		// passwords are now obfuscated
		// we use the property key as encryption key, this is not secure at all but this is not the purpose here
		properties.setProperty(Property.PASSWORD.getKey(),
				CryptoUtil.passwordEncrypt(properties.getString(Property.PASSWORD.getKey()), Property.PASSWORD.getKey()));
		properties.setProperty(Property.LAST_FM_PASSWORD.getKey(),
				CryptoUtil.passwordEncrypt(properties.getString(Property.LAST_FM_PASSWORD.getKey()), Property.LAST_FM_PASSWORD.getKey()));
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
			// TODO: check proxy port is valid (number)
			properties.save(CONFIGURATION_FILE);
		}
		catch (Exception e)
		{
			log.error("Cannot write configuration file");
			System.exit(-1);
		}
	}

	public void load()
	{
		// create configuration directory if necessary
		log.debug("Load configuration");
		File directory = new File(CONFIGURATION_DIRECTORY);
		if (!directory.exists())
		{
			log.debug("Configuration directory does not exist, creating it");
			directory.mkdirs();
		}

		// create configuration file if necessary
		File configurationFile = new File(CONFIGURATION_FILE);
		if (!configurationFile.exists())
		{
			log.debug("Configuration file missing, creating it");
			try
			{
				configurationFile.createNewFile();
			}
			catch (IOException e)
			{
				log.error("Error creating configuration file");
				System.exit(-1);
			}
		}

		// load configuration file
		try
		{
			properties = new PropertiesConfiguration(CONFIGURATION_FILE);
		}
		catch (Exception e)
		{
			log.error("Cannot load configuration file");
			System.exit(-1);
		}

		// check everything is there
		for (Property property : Property.values())
		{
			String key = property.getKey();
			if (properties.getString(key) == null)
				properties.setProperty(key, "");
		}

		// do properties migration if necessary
		if (!BelugaState.getInstance().getVersion().equals("(dev)"))
		{
			float configurationVersion = 0.5f;
			float belugaVersion = 0.1f;
			try
			{
				belugaVersion = Float.parseFloat(BelugaState.getInstance().getVersion());
				configurationVersion = Float.parseFloat(getConfigurationVersion());
			}
			catch (NumberFormatException e)
			{
			}

			log.debug("Configuration file version is " + configurationVersion);

			if (belugaVersion > configurationVersion)
			{
				if (configurationVersion < 0.6f)
				{
					log.info("migratingConfiguration");
					log.debug("Configuration file migration to 0.6");
					propertiesMigrationV0_6();
				}
				// update configuration version
				setConfigurationVersion(BelugaState.getInstance().getVersion());
			}
			else if (belugaVersion < configurationVersion)
			{
				// TODO: display an error message, the GUI is not loaded yet...
				log.error("Your configuration file is for a newer version of Beluga");
			}
		}

		// synchronize file, test write
		store();
	}

	public String getUserName()
	{
		return properties.getString(Property.USER.getKey());
	}

	public void setUserName(String userName)
	{
		properties.setProperty(Property.USER.getKey(), userName);
	}

	public String getPassword()
	{
		return CryptoUtil.passwordDecrypt(properties.getString(Property.PASSWORD.getKey()), Property.PASSWORD.getKey());
	}

	public void setPassword(String password)
	{
		properties.setProperty(Property.PASSWORD.getKey(), CryptoUtil.passwordEncrypt(password, Property.PASSWORD.getKey()));
	}

	public String getProxyHost()
	{
		return properties.getString(Property.PROXY_HOST.getKey());
	}

	public void setProxyHost(String proxyServer)
	{
		properties.setProperty(Property.PROXY_HOST.getKey(), proxyServer);
	}

	public String getProxyPortStr()
	{
		return getProxyPort() == null ? "" : String.valueOf(getProxyPort());
	}

	public Integer getProxyPort()
	{
		return properties.getInteger(Property.PROXY_PORT.getKey(), null);
	}

	public void setProxyPort(String proxyServerPort)
	{
		properties.setProperty(Property.PROXY_PORT.getKey(), proxyServerPort);
	}

	public String getDefaultStationId()
	{
		return properties.getString(Property.DEFAULT_STATION.getKey());
	}

	public void setDefaultStationId(String defaultStationId)
	{
		properties.setProperty(Property.DEFAULT_STATION.getKey(), defaultStationId);
	}

	public String getDNSProxy()
	{
		return properties.getString(Property.PROXY_DNS.getKey());
	}

	public void setDNSProxy(String proxyDNS)
	{
		properties.setProperty(Property.PROXY_DNS.getKey(), proxyDNS);
	}

	public String getThemeId()
	{
		return properties.getString(Property.THEME.getKey(), "classic");
	}

	public void setThemeId(String themeId)
	{
		properties.setProperty(Property.THEME.getKey(), themeId);
	}

	public String getLastFMUsername()
	{
		return properties.getString(Property.LAST_FM_USERNAME.getKey());
	}

	public void setLastFMUsername(String emailAddress)
	{
		properties.setProperty(Property.LAST_FM_USERNAME.getKey(), emailAddress);
	}

	public String getLastFMPassword()
	{
		return CryptoUtil.passwordDecrypt(properties.getString(Property.LAST_FM_PASSWORD.getKey()), Property.LAST_FM_PASSWORD.getKey());
	}

	public void setLastFMPassword(String password)
	{
		properties.setProperty(Property.LAST_FM_PASSWORD.getKey(), CryptoUtil.passwordEncrypt(password, Property.LAST_FM_PASSWORD.getKey()));
	}

	public Boolean getLastFMEnabled()
	{
		return properties.getBoolean(Property.LAST_FM_ENABLED.getKey(), false);
	}

	public void setLastFMEnabled(Boolean enabled)
	{
		properties.setProperty(Property.LAST_FM_ENABLED.getKey(), enabled.toString());
	}

	public String getConfigurationVersion()
	{
		return properties.getString(Property.CONFIGURATION_VERSION.getKey());
	}

	public void setConfigurationVersion(String version)
	{
		properties.setProperty(Property.CONFIGURATION_VERSION.getKey(), version);
	}
}
