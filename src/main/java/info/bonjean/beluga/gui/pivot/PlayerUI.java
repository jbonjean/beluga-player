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
package info.bonjean.beluga.gui.pivot;

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.PandoraPlaylist;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.event.PandoraEvent;
import info.bonjean.beluga.event.PlaybackEvent;
import info.bonjean.beluga.player.MP3Player;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.util.ResourcesUtil;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.SliderValueListener;
import org.apache.pivot.wtk.TablePane;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 * TODO: use more events to reduce coupling (play/pause).
 * TODO: redesign the main thread integration with the player.
 * 
 */
public class PlayerUI extends TablePane implements Bindable, EventSubscriber<PandoraEvent>
{
	private static Logger log = LoggerFactory.getLogger(PlayerUI.class);
	@BXML
	private MainWindow mainWindow;
	@BXML
	private Label stationName;
	@BXML
	private Label currentTime;
	@BXML
	private Label totalTime;
	@BXML
	private Meter progress;
	@BXML
	private Meter progressCache;
	@BXML
	protected LinkButton nextButton;
	@BXML
	protected LinkButton pauseButton;
	@BXML
	protected Slider volumeControl;

	private final BelugaState state = BelugaState.getInstance();
	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private final MP3Player mp3Player = new MP3Player();
	private static final int UI_REFRESH_INTERVAL = 200;
	private long duration;
	private Future<?> playerUISyncFuture;
	private Future<?> playbackThreadFuture;
	private boolean active;

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		currentTime.setText("00:00");
		totalTime.setText("00:00");
		progress.setPercentage(0);
		progressCache.setPercentage(0);

		volumeControl.getSliderValueListeners().add(new SliderValueListener()
		{
			@Override
			public void valueChanged(Slider slider, int previousValue)
			{
				mp3Player.setVolume((int) mp3Player.getVolumeMax() - slider.getValue());
			}
		});

		EventBus.subscribe(PandoraEvent.class, this);
	}

	@Override
	public void onEvent(PandoraEvent event)
	{
		if (event.getType().equals(PandoraEvent.Type.CONNECT))
		{
			active = true;

			// start the playback thread
			if (playbackThreadFuture == null || playbackThreadFuture.isDone())
				playbackThreadFuture = ThreadPools.playbackPool.submit(new Playback());
		}
		else
		{
			active = false;

			// stop the player
			mp3Player.stop();

			// stop the playback thread
			try
			{
				if (playbackThreadFuture != null)
					playbackThreadFuture.get(5, TimeUnit.SECONDS);
			}
			catch (Exception e)
			{
				log.error(e.getMessage(), e);
				playbackThreadFuture.cancel(true);
			}
		}

	}

	private String formatTime(long ms)
	{
		return String.format(
				"%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(ms),
				TimeUnit.MILLISECONDS.toSeconds(ms)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
	}

	public void stopPlayer()
	{
		mp3Player.stop();
	}

	public boolean isPaused()
	{
		return mp3Player.isPaused();
	}

	public boolean isActive()
	{
		return mp3Player.isActive();
	}

	public void pausePlayer()
	{
		mp3Player.pause();

		EventBus.publish(new PlaybackEvent(mp3Player.isPaused() ? PlaybackEvent.Type.SONG_PAUSE
				: PlaybackEvent.Type.SONG_RESUME, null));

		ApplicationContext.queueCallback(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					pauseButton.setButtonData(ResourcesUtil.getSVGImage(mp3Player.isPaused() ? "/img/play.svg"
							: "/img/pause.svg"));
				}
				catch (IOException e)
				{
					log.debug(e.getMessage());
				}
			}
		}, false);

	}

	private Runnable syncUI = new Runnable()
	{
		@Override
		public void run()
		{
			if (!mp3Player.isActive())
				return;

			final long position = mp3Player.getPosition();
			final float progressValue = position / (float) duration;
			final float cacheProgressValue = mp3Player.getCachePosition() / (float) duration;

			// update song position (playback can stop anytime)
			state.getSong().setPosition(position);

			ApplicationContext.queueCallback(new Runnable()
			{
				@Override
				public void run()
				{
					// update volume value (may have been changed from outside)
					volumeControl.setValue(mp3Player.getVolumeMax() - mp3Player.getVolume());

					// update progress bar
					currentTime.setText(formatTime(position));
					progress.setPercentage(progressValue);
					progressCache.setPercentage(cacheProgressValue);
				}
			}, true);
		}
	};

	private class Playback implements Runnable
	{
		@Override
		public void run()
		{
			// init failure counter
			int successiveFailures = 0;

			// start the UI synchronization thread
			playerUISyncFuture = ThreadPools.playerUIScheduler.scheduleAtFixedRate(syncUI, 0,
					UI_REFRESH_INTERVAL, TimeUnit.MILLISECONDS);

			Song song;
			while (true)
			{
				song = null;

				if (!active)
					break;

				try
				{
					if (successiveFailures == 0 || state.getSong() == null)
						song = PandoraPlaylist.getInstance().getNext();
					else
						// do not skip to next song if we failed before
						song = state.getSong();

					if (song == null)
					{
						Thread.sleep(500);
						continue;
					}

					log.debug("New song: " + song.getAdditionalAudioUrl());

					// initialize the player
					try
					{
						log.info("openingAudioStream");
						mp3Player.loadSong(song.getAdditionalAudioUrl());
						successiveFailures = 0;
					}
					catch (Exception e)
					{
						successiveFailures++;

						if (successiveFailures >= 3)
						{
							log.error(e.getMessage(), e);
							break;
						}
						else
						{
							log.info(e.getMessage(), e);
							Thread.sleep(2000);
						}
						continue;
					}

					duration = mp3Player.getDuration();
					song.setDuration(duration);

					// is there a better way to detect the Pandora skip
					// protection (42sec length mp3)?
					if (duration == 42569 && mp3Player.getBitrate() == 64000)
					{
						log.error("pandoraSkipProtection");
						break;
					}

					// guess if it's an ad (not very reliable)
					if (configuration.getAdsDetectionEnabled() && mp3Player.getBitrate() == 128000
							&& duration < 45000)
					{
						log.debug("Ad detected");

						// set the ad flag on the song, for the display and to
						// skip scrobbling
						song.setAd(true);

						// if ad silence configuration is enabled, notify the
						// player
						if (configuration.getAdsSilenceEnabled())
							mp3Player.mute(true);
					}

					// notify song started
					EventBus.publish(new PlaybackEvent(PlaybackEvent.Type.SONG_START, song));

					// initialize controls
					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							// calibrate volume control
							volumeControl.setStart(mp3Player.getVolumeMin());
							volumeControl.setEnd(mp3Player.getVolumeMax());

							// update song duration
							totalTime.setText(formatTime(duration));

							// update station name
							stationName.setText(state.getStation().getStationName());
						}
					}, false);

					// increase thread priority before starting playback
					Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

					// start playback
					mp3Player.play();

					// restore thread priority to normal
					Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

					// disable controls
					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							// set progress bar to full
							currentTime.setText(formatTime(duration));
							progress.setPercentage(1);
						}
					}, false);

					log.debug("Playback finished");
				}
				catch (Exception e)
				{
					log.error(e.getMessage(), e);
					break;
				}
				finally
				{
					if (song != null && song.getDuration() > 0)
						// notify song finished
						EventBus.publish(new PlaybackEvent(PlaybackEvent.Type.SONG_FINISH, song));
				}
			}

			log.debug("Exiting playback thread");

			// notify we are stopping the playback (if active, meaning the stop
			// has not been requested)
			if (active)
				EventBus.publish(new PlaybackEvent(PlaybackEvent.Type.PLAYBACK_STOP, null));

			// stop the UI thread
			playerUISyncFuture.cancel(false);
		}
	}
}
