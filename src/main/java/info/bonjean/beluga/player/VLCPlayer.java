/* Copyright (C) 2012 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.player;

import info.bonjean.beluga.Main;
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.gui.Page;
import info.bonjean.beluga.gui.notification.Notification;
import info.bonjean.beluga.util.HTMLUtil;
import info.bonjean.beluga.util.PandoraUtil;

import java.io.OutputStream;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class VLCPlayer
{
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	private static VLCPlayer instance;
	private MediaPlayer mediaPlayer;

	private VLCPlayer()
	{
		// temporarily disable System.err to avoid this annoying VLC message 
		PrintStream err = System.out;
		System.setErr(new PrintStream(new OutputStream() {
		    public void write(int b) {
		    }
		}));
		
		// load VLC library
		MediaPlayerFactory factory = new MediaPlayerFactory();
		
		// re-enable error output
		System.setErr(err);
		
		mediaPlayer = factory.newHeadlessMediaPlayer();
		mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter()
		{
			@Override
			public void finished(MediaPlayer mediaPlayer)
			{
				log.info("Playback finished");

				try
				{
					mediaPlayer.playMedia(PandoraClient.getInstance().nextSong());
					new Notification(HTMLUtil.getPageHTML(Page.NOTIFICATION));

				} catch (Exception e)
				{
					log.error(e.toString());
				}
			}

			@Override
			public void error(MediaPlayer mediaPlayer)
			{
				log.error("Failed to play media");
			}
		});
	}

	public void play(String url)
	{
		mediaPlayer.playMedia(url);
	}

	public void togglePause()
	{
		if (mediaPlayer.isPlaying())
			mediaPlayer.pause();
		else
			mediaPlayer.play();
	}

	public boolean isPlaying()
	{
		return mediaPlayer.isPlaying();
	}

	public String getProgression()
	{
		return PandoraUtil.formatTime(mediaPlayer.getTime()) + " / " + PandoraUtil.formatTime(mediaPlayer.getLength());
	}

	public static VLCPlayer getInstance()
	{
		if (instance == null)
			instance = new VLCPlayer();

		return instance;
	}
}
