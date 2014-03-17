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
import info.bonjean.beluga.event.PlaybackEvent;
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.player.MP3Player;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.util.HTMLUtil;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javazoom.jl.decoder.JavaLayerException;

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
public class PlayerUI extends TablePane implements Bindable
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
	private LinkButton nextButton;
	@BXML
	private LinkButton pauseButton;
	@BXML
	private Slider volumeControl;

	private final BelugaState state = BelugaState.getInstance();
	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private final MP3Player mp3Player = new MP3Player();
	private long duration;

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		PivotUI.setEnable(nextButton, false);
		PivotUI.setEnable(pauseButton, false);

		currentTime.setText("00:00");
		totalTime.setText("00:00");
		progress.setPercentage(0);
		progressCache.setPercentage(0);

		volumeControl.getSliderValueListeners().add(new SliderValueListener()
		{
			@Override
			public void valueChanged(Slider slider, int previousValue)
			{
				if (mp3Player.isActive() && mp3Player.getVolumeControl() != null)
					mp3Player.getVolumeControl().setValue(
							(int) mp3Player.getVolumeControl().getMaximum() - slider.getValue());
			}
		});

		// start the UI sync thread
		ThreadPools.playerUISyncPool.execute(new SyncUI());

		// start the playback thread
		ThreadPools.playbackPool.execute(new Playback());
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		// nothing to do, everything is handled in playerUISync thread
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
		if (mp3Player.isActive())
			mp3Player.release();
	}

	public boolean isPaused()
	{
		return mp3Player.isActive() && mp3Player.isPaused();
	}

	public void pausePlayer()
	{
		if (mp3Player.isActive())
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
					pauseButton.setButtonData(HTMLUtil.getSVGImage(mp3Player.isPaused() ? "/img/play.svg"
							: "/img/pause.svg"));
				}
				catch (IOException e)
				{
					log.debug(e.getMessage());
				}
			}
		}, false);

	}

	private class Playback implements Runnable
	{
		@Override
		public void run()
		{
			int successiveFailures = 0;
			try
			{
				mp3Player.openAudioDevice();
			}
			catch (JavaLayerException e)
			{
				log.error("cannotOpenAudioDevice", e);
			}
			while (true)
			{
				Song song = null;
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
							successiveFailures = 0;
							state.reset();
							EventBus.publish(new PlaybackEvent(PlaybackEvent.Type.PLAYBACK_STOP,
									null));
							continue;
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
						state.reset();
						mp3Player.release();
						EventBus.publish(new PlaybackEvent(PlaybackEvent.Type.PLAYBACK_STOP, null));
						continue;
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
							mp3Player.setSilence(true);
					}

					// notify song started
					EventBus.publish(new PlaybackEvent(PlaybackEvent.Type.SONG_START, song));

					// update UI
					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
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

					log.debug("Playback finished");
				}
				catch (Exception e)
				{
					log.error(e.getMessage(), e);
				}
				finally
				{
					if (mp3Player.isActive())
					{
						song.setPosition(mp3Player.getPosition());

						// notify song finished
						EventBus.publish(new PlaybackEvent(PlaybackEvent.Type.SONG_FINISH, song));
					}

					// always stop the player when done
					mp3Player.release();
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
					Thread.sleep(200);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				if (!mp3Player.isActive())
					continue;

				final long position = mp3Player.getPosition();
				final float progressValue = position / (float) duration;
				final float cacheProgressValue = mp3Player.getCachePosition() / (float) duration;

				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						if (mp3Player != null && mp3Player.getVolumeControl() != null)
						{
							if (!volumeControl.isEnabled())
							{
								volumeControl.setStart((int) mp3Player.getVolumeControl()
										.getMinimum());
								volumeControl.setEnd((int) mp3Player.getVolumeControl()
										.getMaximum());

								volumeControl.setEnabled(true);
								PivotUI.setEnable(nextButton, true);
								PivotUI.setEnable(pauseButton, true);
							}
							volumeControl.setValue((int) mp3Player.getVolumeControl().getMaximum()
									- (int) mp3Player.getVolumeControl().getValue());
						}
						else
						{
							volumeControl.setEnabled(false);
							PivotUI.setEnable(nextButton, false);
							PivotUI.setEnable(pauseButton, false);
						}

						currentTime.setText(formatTime(position));
						progress.setPercentage(progressValue);
						progressCache.setPercentage(cacheProgressValue);
					}
				}, true);
			}
		}
	}
}
