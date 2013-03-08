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
package info.bonjean.beluga.gui.pivot;

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.PandoraPlaylist;
import info.bonjean.beluga.log.Log;
import info.bonjean.beluga.response.Song;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.player.BelugaMP3Player;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.TablePane;
import org.slf4j.Logger;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class PlayerUI extends TablePane implements Bindable
{
	@Log
	private static Logger log;

	private final BelugaState state = BelugaState.getInstance();

	@BXML
	MainWindow mainWindow;
	@BXML
	Label stationName;
	@BXML
	Label currentTime;
	@BXML
	Label totalTime;
	@BXML
	Meter progress;
	@BXML
	Meter progressCache;
	@BXML
	LinkButton nextButton;

	BelugaMP3Player mp3Player;
	float duration;

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		stationName.setText("Pandora");
		currentTime.setText("00:00");
		totalTime.setText("00:00");
		progress.setPercentage(0);
		progressCache.setPercentage(0);

		// start the UI sync thread
		UIPools.playerUISyncPool.execute(new SyncUI());

		// start the playback thread
		UIPools.playbackPool.execute(new Playback());
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		nextButton.getAction().setEnabled(enabled);
		nextButton.setEnabled(enabled);

		super.setEnabled(enabled);
	}

	private String formatTime(long ms)
	{
		return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(ms),
				TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
	}

	public void stopPlayer()
	{
		if (mp3Player != null)
		{
			mp3Player.close();
			mp3Player = null;
		}
	}

	private class Playback implements Runnable
	{
		@Override
		public void run()
		{
			int successiveFailures = 0;
			while (true)
			{
				try
				{
					final Song song = PandoraPlaylist.getInstance().getNext();

					if (song == null)
					{
						Thread.sleep(500);
						continue;
					}

					log.debug("New song: " + song.getAdditionalAudioUrl());

					// initialize the player
					try
					{
						mp3Player = new BelugaMP3Player(song.getAdditionalAudioUrl());
						successiveFailures = 0;
					}
					catch (Exception e)
					{
						successiveFailures++;

						if (successiveFailures >= 3)
						{
							log.error("tooManyPlayerSuccessiveFailures");
							Thread.sleep(5 * 60 * 1000);
						}
						else
						{
							log.error(e.getMessage(), e);
							Thread.sleep(2000);
						}
						continue;
					}

					duration = mp3Player.getDuration();

					// update UI
					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							// update song duration
							totalTime.setText(formatTime((long) duration));

							// update station name
							stationName.setText(state.getStation().getStationName());

							// notify main window
							mainWindow.songChanged(song);
						}
					}, true);

					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							// (first enabled paired with the one from MainWindow.initialize)
							mainWindow.setEnabled(true);
						}
					}, true);

					try
					{
						// start playback
						mp3Player.play();
					}
					catch (BitstreamException e)
					{
						if (mp3Player != null && e.getErrorCode() == 258 && mp3Player.getDuration() == 42762.45f)
						{
							log.error("pandoraSkipProtection");
							// prevent playlist to be filled again
							state.setStation(null);
							// clear playlist
							PandoraPlaylist.getInstance().clear();

							continue;
						}
					}

					log.debug("Playback finished");

					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							if (mp3Player != null)
							{
								// make things clean in the UI
								progress.setPercentage(1);
								currentTime.setText(formatTime((long) duration));
							}

							mainWindow.setEnabled(false);
						}
					}, true);
				}
				catch (Exception e)
				{
					log.error(e.getMessage(), e);
				}
				finally
				{
					// always stop the player when done
					if (mp3Player != null)
						mp3Player.close();
					mp3Player = null;
				}
			}
		}
	}

	private class SyncUI implements Runnable
	{
		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				if (mp3Player == null)
					continue;

				final int position = mp3Player.getPosition();
				final float progressValue = position / duration;
				final float cacheProgressValue = mp3Player.getCachePosition() / duration;

				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						currentTime.setText(formatTime(position));
						progress.setPercentage(progressValue);
						progressCache.setPercentage(cacheProgressValue);
					}
				}, true);
			}
		}
	}
}
