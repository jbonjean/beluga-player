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
package info.bonjean.beluga.util;

import info.bonjean.beluga.exception.CryptoException;
import info.bonjean.beluga.response.Song;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PandoraUtil {
	public static String getSyncTime(String encryptedSyncTime) throws CryptoException {
		return CryptoUtil.pandoraDecrypt(encryptedSyncTime).substring(4, 14);
	}

	public static long getSyncTime() {
		return (new Date().getTime() / 1000L) + 10;
	}

	public static void cleanItemList(List<Song> items) {
		List<Song> toRemove = new ArrayList<Song>();

		for (Song item : items)
			if (item.getAdToken() != null)
				toRemove.add(item);

		for (Song item : toRemove)
			items.remove(item);
	}

	public static String formatTime(long timeMillis) {
		long time = timeMillis / 1000;
		String seconds = Integer.toString((int) (time % 60));
		String minutes = Integer.toString((int) ((time % 3600) / 60));
		for (int i = 0; i < 2; i++) {
			if (seconds.length() < 2) {
				seconds = "0" + seconds;
			}
			if (minutes.length() < 2) {
				minutes = "0" + minutes;
			}
		}
		return minutes + ":" + seconds;
	}
}
