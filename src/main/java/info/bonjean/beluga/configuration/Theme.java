/*
 * Copyright (C) 2012-2020 Julien Bonjean <julien@bonjean.info>
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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Theme {
	// TODO: handle i18n
	DARK(
			"dark",
			"Dark",
			new String[] { //
					"#bbbbbb", // 0-2 light gray (font color)
					"#333333", // 3-5 dark grey (background)
					"#666666", // 6-8 (borders)
					"#333333", // 9-11 (buttons)
					"#aaaaaa", // 12-14 (hyperlinks, menu highlight background)
					"#eeeeee", // 15-17 (checkboxes, playback progress overlay)
					"#555555", // 18-20 yellow (hover)
					"#ff6359" // 21-23 red (error message)
			},
			"/img/beluga-player-dark.svg",
			"/img/house-dark.svg",
			"/img/disconnected-dark.svg",
			"/img/forward-dark.svg",
			"/img/volume-dark.svg",
			"/img/mute-dark.svg")

	,
	DEFAULT(
			"default",
			"Default",
			new String[] { //
					"#000000", //
					"#ffffff", //
					"#999999", //
					"#dddcd5", //
					"#336699", //
					"#336699", //
					"#ffe480", //
					"#eb0000" //
			},
			"/img/beluga-player.svg",
			"/img/house.svg",
			"/img/disconnected.svg",
			"/img/forward.svg",
			"/img/volume.svg",
			"/img/mute.svg");

	private final String id;
	private final String name;
	private final String[] baseColors;
	private final String belugaPlayerImagePath;
	private final String homeImagePath;
	private final String disconnectedImagePath;
	private final String forwardImagePath;
	private final String volumeImagePath;
	private final String muteImagePath;

	private static final Map<String, Theme> lookup = new HashMap<String, Theme>();

	static {
		for (Theme s : EnumSet.allOf(Theme.class))
			lookup.put(s.getId(), s);
	}

	public static Theme get(String id) {
		return lookup.getOrDefault(id, DEFAULT);
	}

	private Theme(String id, String name, String[] baseColors, String belugaPlayerImagePath, String homeImagePath,
			String disconnectedImagePath, String forwardImagePath, String volumeImagePath, String muteImagePath) {
		this.id = id;
		this.name = name;
		this.baseColors = baseColors;
		this.belugaPlayerImagePath = belugaPlayerImagePath;
		this.homeImagePath = homeImagePath;
		this.disconnectedImagePath = disconnectedImagePath;
		this.forwardImagePath = forwardImagePath;
		this.volumeImagePath = volumeImagePath;
		this.muteImagePath = muteImagePath;
	}

	public String getId() {
		return id;
	}

	public String toString() {
		return name;
	}

	public String[] getBaseColors() {
		return baseColors;
	}

	public String getBelugaPlayerImagePath() {
		return belugaPlayerImagePath;
	}

	public String getHomeImagePath() {
		return homeImagePath;
	}

	public String getDisconnectedImagePath() {
		return disconnectedImagePath;
	}

	public String getForwardImagePath() {
		return forwardImagePath;
	}

	public String getVolumeImagePath() {
		return volumeImagePath;
	}

	public String getMuteImagePath() {
		return muteImagePath;
	}
}
