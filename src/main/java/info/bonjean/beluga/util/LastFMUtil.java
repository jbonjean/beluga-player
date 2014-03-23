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
package info.bonjean.beluga.util;

import info.bonjean.beluga.client.LastFMSession;
import info.bonjean.beluga.gui.pivot.ThreadPools;
import info.bonjean.beluga.response.Song;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class LastFMUtil
{
	private static Logger log = LoggerFactory.getLogger(LastFMUtil.class);
	private static ExecutorService scrobbleThreadPool = ThreadPools.scrobblerPool;
	private static Future<?> scrobbleFuture;

	public static void asyncScrobble(final Song song)
	{
		if (scrobbleFuture != null && !scrobbleFuture.isDone())
		{
			log.error("lastFMTimeout");
			scrobbleFuture.cancel(true);
		}

		scrobbleFuture = scrobbleThreadPool.submit(new Runnable()
		{
			@Override
			public void run()
			{
				LastFMSession.getInstance().scrobbleTrack(song);
			}
		});
	}
}
