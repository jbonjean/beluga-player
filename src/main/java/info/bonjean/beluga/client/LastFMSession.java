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
package info.bonjean.beluga.client;

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.log.Log;
import info.bonjean.beluga.response.Song;

import org.slf4j.Logger;

import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Result;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleResult;

/**
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class LastFMSession
{
	@Log
	private static Logger log;

	public static final String API_KEY = "79bb16780e65af77078e118f59de365a";
	public static final String API_SECRET = "d9fe10dcd95bee70fdd27588c489108d";
	private static final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private static LastFMSession instance;
	private Session session = null;

	private LastFMSession()
	{
		if (configuration.getLastFMEnabled())
		{
			session = Authenticator.getMobileSession(configuration.getLastFMUsername(), configuration.getLastFMPassword(), API_KEY, API_SECRET);
			if (session == null)
				log.error("Authentication with last.fm failed");
		}
	}

	public static void reset()
	{
		instance = null;
	}

	public static LastFMSession getInstance()
	{
		if (instance == null)
			instance = new LastFMSession();
		return instance;
	}

	public void scrobbleTrack(Song song, long position, long duration)
	{
		if (session == null)
			return;

		if (position / (float) duration < 0.9f)
		{
			log.debug("Played less than 90%, will no be scrobbled with last.fm");
			return;
		}
		
		//41952 of 42569

		int now = (int) (System.currentTimeMillis() / 1000);
		ScrobbleResult TrackScrobbleResult = Track.scrobble(song.getArtistName(), song.getSongName(), now, session);
		boolean success = TrackScrobbleResult.isSuccessful() && !TrackScrobbleResult.isIgnored();
		if (success)
			log.info("lastfmScrobbleSuccess");
		else
			log.warn("lastfmScrobbleFailure");
	}

	public void loveTrack(Song song)
	{
		if (session == null)
			return;

		Result TrackScrobbleResult = Track.love(song.getArtistName(), song.getSongName(), session);
		boolean success = TrackScrobbleResult.isSuccessful();
		if (success)
			log.info("lastfmLoveTrackSuccess");
		else
			log.warn("lastfmLoveTrackFailure");
	}

	public void updateNowPlaying(Song song)
	{
		if (session == null)
			return;

		ScrobbleResult nowPlayingResult = Track.updateNowPlaying(song.getArtistName(), song.getSongName(), session);
		boolean success = nowPlayingResult.isSuccessful() && !nowPlayingResult.isIgnored();
		if (success)
			log.info("lastfmNowPlayingSuccess");
		else
			log.warn("lastfmNowPlayingFailure");
	}
}