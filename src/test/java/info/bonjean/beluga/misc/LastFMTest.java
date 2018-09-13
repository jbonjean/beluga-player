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
package info.bonjean.beluga.misc;

import info.bonjean.beluga.client.LastFMSession;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleResult;

/**
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class LastFMTest
{
	private static BelugaConfiguration configuration = BelugaConfiguration.getInstance();

	public static void main(String[] args)
	{
		String key = LastFMSession.API_KEY;
		String secret = LastFMSession.API_SECRET;

		configuration.load();

		String user = configuration.getLastFMUsername();
		String password = configuration.getLastFMPassword();

		Session session = Authenticator.getMobileSession(user, password, key, secret);

		// Update "now playing" status:
		ScrobbleResult nowPlayingResult = Track.updateNowPlaying("Pixies", "Allison", session);
		System.out.println("ok: " + (nowPlayingResult.isSuccessful() && !nowPlayingResult.isIgnored()));

		// Scrobble track:
		int now = (int) (System.currentTimeMillis() / 1000);
		ScrobbleResult TrackScrobbleResult = Track.scrobble("Pixies", "Allison", now, session);
		System.out.println("ok: " + (TrackScrobbleResult.isSuccessful() && !TrackScrobbleResult.isIgnored()));
	}

}
