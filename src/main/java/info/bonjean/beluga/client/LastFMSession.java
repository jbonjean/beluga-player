/*
 * Copyright (C) 2012, 2013, 2014 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.client;

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.response.Song;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static Logger log = LoggerFactory.getLogger(LastFMSession.class);

	public static final String API_KEY = "79bb16780e65af77078e118f59de365a";
	public static final String API_SECRET = "d9fe10dcd95bee70fdd27588c489108d";
	private static final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private static LastFMSession instance;
	private Session session = null;

	private LastFMSession()
	{
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

	private Session getSession()
	{
		if (session == null)
		{
			log.debug("Creating last.fm session");

			session = Authenticator.getMobileSession(configuration.getLastFMUsername(),
					configuration.getLastFMPassword(), API_KEY, API_SECRET);

			if (session == null)
				log.warn("Authentication with last.fm failed");
		}

		return session;
	}

	public void scrobbleTrack(Song song)
	{
		// don't scrobble if less than 90%
		if (song.getDuration() == 0 || song.getPosition() / (float) song.getDuration() < 0.9f)
		{
			log.debug("Played less than 90%, will no be scrobbled with last.fm");
			return;
		}

		// check if we got a valid session
		if (getSession() == null)
			return;

		// send data to last.fm
		int now = (int) (System.currentTimeMillis() / 1000);
		ScrobbleResult TrackScrobbleResult = Track.scrobble(song.getArtistName(),
				song.getSongName(), now, getSession());

		if (TrackScrobbleResult.isSuccessful() && !TrackScrobbleResult.isIgnored())
			log.info("lastfmScrobbleSuccess");
		else
			log.warn("lastfmScrobbleFailure");
	}

	public void loveTrack(Song song)
	{
		if (getSession() == null)
			return;

		Result TrackScrobbleResult = Track.love(song.getArtistName(), song.getSongName(),
				getSession());

		if (TrackScrobbleResult.isSuccessful())
			log.info("lastfmLoveTrackSuccess");
		else
			log.warn("lastfmLoveTrackFailure");
	}

	public void updateNowPlaying(Song song)
	{
		if (getSession() == null)
			return;

		ScrobbleResult nowPlayingResult = Track.updateNowPlaying(song.getArtistName(),
				song.getSongName(), getSession());

		if (nowPlayingResult.isSuccessful() && !nowPlayingResult.isIgnored())
			log.info("lastfmNowPlayingSuccess");
		else
			log.warn("lastfmNowPlayingFailure");
	}
}
