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

import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.response.Song;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class PandoraPlaylist
{
	private static final Logger log = LoggerFactory.getLogger(PandoraPlaylist.class);
	private final BelugaState state = BelugaState.getInstance();
	private final PandoraClient pandoraClient = PandoraClient.getInstance();

	private static PandoraPlaylist instance;
	private LinkedList<Song> queue = new LinkedList<Song>();

	private PandoraPlaylist()
	{
	}

	public static PandoraPlaylist getInstance()
	{
		if (instance == null)
			instance = new PandoraPlaylist();
		return instance;
	}

	public Song getNext() throws BelugaException
	{
		// queue empty, feed with pandora data
		if (queue.isEmpty())
			feedQueue();

		// queue still empty, there was a problem, return null
		if (queue.isEmpty())
			return null;

		Song song = queue.removeFirst();

		// update global state
		state.setSong(song);

		return song;
	}

	public synchronized void feedQueue() throws BelugaException
	{
		// check if Pandora client is ready
		if (!pandoraClient.isLoggedIn() || state.getStation() == null)
			return;

		// check if the feed is empty, we do not want to reach Pandora limit
		if (!queue.isEmpty())
			return;

		log.info("retrievingPlaylist");

		List<Song> playlist = pandoraClient.getPlaylist(state.getStation());

		// populate additional data
		for (Song song : playlist)
			song.setFocusTraits(pandoraClient.retrieveFocusTraits(song));

		queue.addAll(playlist);
	}

	public void clear()
	{
		log.debug("Invalidating playlist");
		queue.clear();
	}
}
