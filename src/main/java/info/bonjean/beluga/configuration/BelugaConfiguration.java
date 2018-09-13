/*
 * Copyright (C) 2012-2018 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.configuration;

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.util.CryptoUtil;
import info.bonjean.beluga.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class BelugaConfiguration {
	private static Logger log = LoggerFactory.getLogger(BelugaConfiguration.class);
	public static final String CONFIGURATION_DEFAULT_VERSION = "(dev)";
	private static final String CONFIGURATION_DIRECTORY = System.getProperty("user.home") + "/.config/beluga";
	private static final String CONFIGURATION_FILE = CONFIGURATION_DIRECTORY + "/configuration.properties";
	private Properties properties;

	private static BelugaConfiguration instance;

	private BelugaConfiguration() {
	}

	private void propertiesMigrationV0_6() {
		// passwords are now obfuscated
		// we use the property key as encryption key, this is not secure at all
		// but this is not the purpose here
		set(Property.PASSWORD, CryptoUtil.passwordEncrypt(getString(Property.PASSWORD), Property.PASSWORD.getKey()));
		set(Property.LAST_FM_PASSWORD,
				CryptoUtil.passwordEncrypt(getString(Property.LAST_FM_PASSWORD), Property.LAST_FM_PASSWORD.getKey()));
	}

	private void propertiesMigrationV0_8() {
		// update to the new connection management
		String proxyDNS = (String) properties.get("proxy.dns");
		if (proxyDNS != null && !proxyDNS.isEmpty())
			setConnectionType(ConnectionType.PROXY_DNS);
		else {
			String httpProxyHost = getProxyHost();
			Integer httpProxyPort = getProxyPort();
			if (httpProxyHost != null && !httpProxyHost.isEmpty() && httpProxyPort != null)
				setConnectionType(ConnectionType.HTTP_PROXY);
			else
				setConnectionType(ConnectionType.DIRECT);
		}
	}

	public static BelugaConfiguration getInstance() {
		if (instance == null)
			instance = new BelugaConfiguration();

		return instance;
	}

	public void store() {
		try {
			// TODO: check proxy port is valid (number)
			properties.store(new FileOutputStream(CONFIGURATION_FILE), null);
		} catch (Exception e) {
			log.error("Cannot write configuration file");
			System.exit(-1);
		}
	}

	public void load() {
		// create configuration directory if necessary
		log.debug("Load configuration");
		File directory = new File(CONFIGURATION_DIRECTORY);
		if (!directory.exists()) {
			log.debug("Configuration directory does not exist, creating it");
			directory.mkdirs();
		}

		// create configuration file if necessary
		File configurationFile = new File(CONFIGURATION_FILE);
		if (!configurationFile.exists()) {
			log.debug("Configuration file missing, creating it");
			try {
				configurationFile.createNewFile();
			} catch (IOException e) {
				log.error("Error creating configuration file");
				System.exit(-1);
			}
		}

		// load configuration file
		properties = new Properties();
		try {
			properties.load(new FileInputStream(CONFIGURATION_FILE));
		} catch (Exception e) {
			log.error("Cannot load configuration file");
			System.exit(-1);
		}

		// do properties migration if necessary
		if (!BelugaState.getInstance().getVersion().equals(CONFIGURATION_DEFAULT_VERSION)) {
			log.debug("Configuration file version is " + getConfigurationVersion());

			int versionComparison = StringUtil.compareVersions(BelugaState.getInstance().getVersion(),
					getConfigurationVersion());

			if (versionComparison > 0) {
				if (StringUtil.compareVersions(getConfigurationVersion(), "0.6") < 0) {
					log.info("migratingConfiguration");
					log.debug("Configuration file migration to 0.6");
					propertiesMigrationV0_6();
				}
				if (StringUtil.compareVersions(getConfigurationVersion(), "0.8") < 0) {
					log.info("migratingConfiguration");
					log.debug("Configuration file migration to 0.8");
					propertiesMigrationV0_8();
				}
				// update configuration version
				setConfigurationVersion(BelugaState.getInstance().getVersion());
			} else if (versionComparison < 0) {
				// TODO: display an error message, the GUI is not loaded yet...
				log.error("Your configuration file is for a newer version of Beluga Player");
			}
		}

		// synchronize file, test write
		store();
	}

	private void set(Property property, String value) {
		properties.setProperty(property.getKey(), value);
	}

	private String getString(Property property, String defaultValue) {
		if (properties.get(property.getKey()) == null)
			return defaultValue;

		return (String) properties.get(property.getKey());
	}

	private String getString(Property property) {
		return getString(property, null);
	}

	private Integer getInteger(Property property, Integer defaultValue) {
		String stringValue = (String) properties.get(property.getKey());
		if (stringValue == null || stringValue.isEmpty())
			return defaultValue;

		return new Integer(stringValue);
	}

	private Boolean getBoolean(Property property, Boolean defaultValue) {
		String stringValue = (String) properties.get(property.getKey());
		if (stringValue == null || stringValue.isEmpty())
			return defaultValue;

		return new Boolean(stringValue);
	}

	public String getUserName() {
		return getString(Property.USER, "");
	}

	public void setUserName(String userName) {
		set(Property.USER, userName);
	}

	public String getPassword() {
		return CryptoUtil.passwordDecrypt(getString(Property.PASSWORD, ""), Property.PASSWORD.getKey());
	}

	public void setPassword(String password) {
		set(Property.PASSWORD, CryptoUtil.passwordEncrypt(password, Property.PASSWORD.getKey()));
	}

	public String getProxyHost() {
		return getString(Property.PROXY_HOST, "");
	}

	public void setProxyHost(String proxyServer) {
		set(Property.PROXY_HOST, proxyServer);
	}

	public String getProxyPortStr() {
		return getProxyPort() == null ? "" : String.valueOf(getProxyPort());
	}

	public Integer getProxyPort() {
		return getInteger(Property.PROXY_PORT, null);
	}

	public void setProxyPort(String proxyServerPort) {
		set(Property.PROXY_PORT, proxyServerPort);
	}

	public String getDefaultStationId() {
		return getString(Property.DEFAULT_STATION, "");
	}

	public void setDefaultStationId(String defaultStationId) {
		set(Property.DEFAULT_STATION, defaultStationId);
	}

	public String getLastFMUsername() {
		return getString(Property.LAST_FM_USERNAME, "");
	}

	public void setLastFMUsername(String emailAddress) {
		set(Property.LAST_FM_USERNAME, emailAddress);
	}

	public String getLastFMPassword() {
		return CryptoUtil.passwordDecrypt(getString(Property.LAST_FM_PASSWORD, ""), Property.LAST_FM_PASSWORD.getKey());
	}

	public void setLastFMPassword(String password) {
		set(Property.LAST_FM_PASSWORD, CryptoUtil.passwordEncrypt(password, Property.LAST_FM_PASSWORD.getKey()));
	}

	public Boolean getLastFMEnabled() {
		return getBoolean(Property.LAST_FM_ENABLED, false);
	}

	public void setLastFMEnabled(Boolean enabled) {
		set(Property.LAST_FM_ENABLED, enabled.toString());
	}

	public Boolean getAdsDetectionEnabled() {
		return getBoolean(Property.ADS_DETECTION_ENABLED, false);
	}

	public void setAdsDetectionEnabled(Boolean enabled) {
		set(Property.ADS_DETECTION_ENABLED, enabled.toString());
	}

	public Boolean getAdsSilenceEnabled() {
		return getBoolean(Property.ADS_SILENCE_ENABLED, false);
	}

	public void setAdsSilenceEnabled(Boolean enabled) {
		set(Property.ADS_SILENCE_ENABLED, enabled.toString());
	}

	public String getConfigurationVersion() {
		return getString(Property.CONFIGURATION_VERSION, "0.5f");
	}

	public void setConfigurationVersion(String version) {
		set(Property.CONFIGURATION_VERSION, version);
	}

	public String getNotificationsStyle() {
		return getString(Property.NOTIFICATIONS_STYLE, "light");
	}

	public void setNotificationsStyle(String notificationsStyle) {
		set(Property.NOTIFICATIONS_STYLE, notificationsStyle);
	}

	public String getStationsOrderBy() {
		return getString(Property.STATIONS_ORDER_BY, "date");
	}

	public void setStationsOrderBy(String stationsOrderBy) {
		set(Property.STATIONS_ORDER_BY, stationsOrderBy);
	}

	public Boolean getWindowRestoreEnabled() {
		return getBoolean(Property.WINDOW_RESTORE, false);
	}

	public void setWindowRestoreEnabled(Boolean enabled) {
		set(Property.WINDOW_RESTORE, enabled.toString());
	}

	public ConnectionType getConnectionType() {
		return ConnectionType.get(getString(Property.CONNECTION_TYPE, ConnectionType.DIRECT.getId()));
	}

	public void setConnectionType(ConnectionType connectionType) {
		set(Property.CONNECTION_TYPE, connectionType.getId());
	}
}
