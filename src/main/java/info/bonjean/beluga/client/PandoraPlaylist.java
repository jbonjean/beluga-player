package info.bonjean.beluga.client;

import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.response.Song;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public Song getNext()
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

	private void feedQueue()
	{
		// check if Pandora client is ready
		if (!pandoraClient.isLoggedIn() || state.getStation() == null)
			return;

		// check if the feed is empty, we do not want to reach Pandora limit
		if (!queue.isEmpty())
		{
			log.warn("We should not be there!");
			return;
		}

		try
		{
			List<Song> playlist = pandoraClient.getPlaylist(state.getStation());

			// populate additional data
			for (Song song : playlist)
				song.setFocusTraits(pandoraClient.retrieveFocusTraits(song));

			queue.addAll(playlist);
		}
		catch (BelugaException e)
		{
			e.printStackTrace();
		}
	}

	public void clear()
	{
		log.info("Invalidating playlist");
		queue.clear();
	}
}
