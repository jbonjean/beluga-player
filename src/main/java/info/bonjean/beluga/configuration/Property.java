/*
 * Copyright (C) 2012-2021 Julien Bonjean <julien@bonjean.info>
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

public enum Property {
	CONFIGURATION_VERSION("configuration.version"),
	USER("user"),
	PASSWORD("password"),
	CONNECTION_TYPE("connection.type"),
	HTTP_PROXY_HOST("http_proxy.host"),
	HTTP_PROXY_PORT("http_proxy.port"),
	SOCKS5_PROXY_HOST("socks5_proxy.host"),
	SOCKS5_PROXY_PORT("socks5_proxy.port"),
	DEFAULT_STATION("default.station"),
	LAST_FM_USERNAME("last.fm.username"),
	LAST_FM_PASSWORD("last.fm.password"),
	LAST_FM_ENABLED("last.fm.enabled"),
	ADS_DETECTION_ENABLED("ads.detection.enabled"),
	ADS_SILENCE_ENABLED("ads.silence.enabled"),
	NOTIFICATIONS_STYLE("notifications.style"),
	STATIONS_ORDER_BY("stations.order.by"),
	WINDOW_RESTORE("window.restore"),
	AUDIO_QUALITY("audio.quality"),
	THEME("theme");

	private final String key;

	private Property(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
