package info.bonjean.beluga.player;

import info.bonjean.beluga.response.Song;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MusicPlayer
{
	private final static Logger log = LoggerFactory.getLogger(MusicPlayer.class);
	private static JLayer player;
	private static Thread thread;
	private static Status status = Status.STOP;
	private static Song song;

	private enum Status
	{
		PLAY, PAUSE, STOP;
	}

	public static boolean isRunning()
	{
		return status != Status.STOP;
	}

	public static void shutdown()
	{
		if (status == Status.STOP)
		{
			log.info("Not running");
			return;
		}

		player.close();
		song = null;
		status = Status.STOP;
		try
		{
			thread.join();
		}
		catch (InterruptedException e)
		{
			log.error(e.getMessage());
		}
	}

	public static void playPause()
	{
		if (status == Status.STOP)
		{
			log.info("Not running");
			return;
		}

		if (status == Status.PLAY)
		{
			log.info("Suspend player");
			status = Status.PAUSE;
			player.pause();
		}
		else
		{
			log.info("Resume playlist");
			status = Status.PLAY;
			player.resume();
		}
	}

	public void play(Song currentSong) throws Exception
	{
		if (status != Status.STOP)
		{
			log.warn("Already running");
			return;
		}

		BufferedInputStream bis = null;
		status = Status.PLAY;
		song = currentSong;

		log.info("Playing " + song.getSongName() + " by '" + song.getArtistName() + "' from '" + song.getAlbumName() + "'");

		try
		{
			URL url = new URL(song.getAdditionalAudioUrl());
			URLConnection uc = url.openConnection();
			bis = new BufferedInputStream(uc.getInputStream());
			player = new JLayer(bis);
			log.info("start playing");
			player.play();
			log.info("end playing");
		}
		catch (Exception e)
		{
			log.error(e.getMessage());
			throw e;
		}
		finally
		{
			player.close();
			bis.close();
			song = null;
			status = Status.STOP;
		}
	}

	public static Song getSong()
	{
		return song;
	}
}
