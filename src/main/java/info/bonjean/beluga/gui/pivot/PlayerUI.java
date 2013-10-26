/* Copyright (C) 2012, 2013 Julien Bonjean <julien@bonjean.info>
 * 
 * This file is part of Beluga Player.
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
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.gui.notification.Notification;
import info.bonjean.beluga.response.Song;

import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javazoom.jl.player.BelugaMP3Player;

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
import org.apache.pivot.wtk.content.ButtonDataRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
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
	private BelugaMP3Player mp3Player;
	private long duration;
	private Future<?> animationFuture;

	private Runnable animation = new Runnable()
	{
		private final static int DEFAULT_SIZE = 30;
		private int currentSize = DEFAULT_SIZE;
		private ButtonDataRenderer buttonDataRenderer;

		private void setSize(int size)
		{
			currentSize = size;
			buttonDataRenderer.setIconWidth(size);
			buttonDataRenderer.setIconHeight(size);
			ApplicationContext.queueCallback(new Runnable()
			{
				@Override
				public void run()
				{
					pauseButton.repaint();
				}
			}, true);
		}

		@Override
		public void run()
		{
			buttonDataRenderer = (ButtonDataRenderer) pauseButton.getDataRenderer();
			while (true)
			{
				try
				{
					Thread.sleep(700);
				}
				catch (InterruptedException e)
				{
					setSize(DEFAULT_SIZE);
					return;
				}
				setSize(currentSize == DEFAULT_SIZE ? DEFAULT_SIZE + 2 : DEFAULT_SIZE);
			}

		}
	};

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
				if (mp3Player != null && mp3Player.getVolumeControl() != null)
					mp3Player.getVolumeControl().setValue(slider.getValue());
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
		return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(ms),
				TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
	}

	public void stopPlayer()
	{
		if (mp3Player != null)
			mp3Player.close();
	}

	public void clearAnimation()
	{
		if (animationFuture != null && !animationFuture.isDone())
			animationFuture.cancel(true);
	}

	public void pausePlayer()
	{
		if (mp3Player != null)
			mp3Player.pause();

		if (mp3Player.isPaused())
			animationFuture = ThreadPools.animationPool.submit(animation);
		else
			animationFuture.cancel(true);
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
					final Song song;
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

					// update UI for song information as soon as possible
					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							// notify main window
							mainWindow.playbackStarted(song);
						}
					}, false);

					log.debug("New song: " + song.getAdditionalAudioUrl());

					// initialize the player
					try
					{
						log.info("openingAudioStream");
						mp3Player = new BelugaMP3Player(song.getAdditionalAudioUrl());
						successiveFailures = 0;
					}
					catch (CommunicationException e)
					{
						log.error("pandoraSkipProtection");
						mainWindow.disconnect();
						return;
					}
					catch (Exception e)
					{
						successiveFailures++;

						if (successiveFailures >= 3)
						{
							log.error(e.getMessage(), e);
							successiveFailures = 0;
							mainWindow.disconnect();
							return;
						}
						else
						{
							log.info(e.getMessage(), e);
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
							totalTime.setText(formatTime(duration));

							// update station name
							stationName.setText(state.getStation().getStationName());

							// display desktop notification
							new Notification(state.getSong());
						}
					}, false);

					// increase thread priority before starting playback
					Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

					// start playback
					mp3Player.play();

					// restore thread priority to normal
					Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

					log.debug("Playback finished");
					// notify main window playback is finished (will trigger last.fm update)
					if (mp3Player != null)
						mainWindow.playbackFinished(song, mp3Player.getPosition(), mp3Player.getDuration());
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

					// and clear the animation
					clearAnimation();
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

				if (mp3Player == null)
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
								volumeControl.setStart((int) mp3Player.getVolumeControl().getMinimum());
								volumeControl.setEnd((int) mp3Player.getVolumeControl().getMaximum());

								volumeControl.setEnabled(true);
								PivotUI.setEnable(nextButton, true);
								PivotUI.setEnable(pauseButton, true);
							}
							volumeControl.setValue((int) mp3Player.getVolumeControl().getValue());
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
