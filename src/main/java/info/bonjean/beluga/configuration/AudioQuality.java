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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public enum AudioQuality {
	LOW("low", "AAC Low"), MEDIUM("medium", "AAC Medium"), HIGH("high", "AAC High"), MP3("mp3", "MP3");
	private final String id;
	private final String name;
	private static final Map<String, AudioQuality> lookup = new HashMap<String, AudioQuality>();

	static {
		for (AudioQuality s : EnumSet.allOf(AudioQuality.class))
			lookup.put(s.getId(), s);
	}

	public static AudioQuality get(String id) {
		return lookup.get(id);
	}

	private AudioQuality(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String toString() {
		return name;
	}
}
