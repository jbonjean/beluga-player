/*
 * Copyright (C) 2012-2021 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.player;

import info.bonjean.beluga.bus.InternalBus;
import info.bonjean.beluga.bus.PlaybackEvent;
import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.PandoraPlaylist;
import info.bonjean.beluga.configuration.AudioQuality;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.configuration.Theme;
import info.bonjean.beluga.exception.PandoraError;
import info.bonjean.beluga.exception.PandoraException;
import info.bonjean.beluga.gui.pivot.MainWindow;
import info.bonjean.beluga.gui.pivot.ThreadPools;
import info.bonjean.beluga.response.Audio;
import info.bonjean.beluga.response.Song;
import org.apache.commons.lang3.StringUtils;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.content.ButtonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AudioPlayer {
	private static Logger log = LoggerFactory.getLogger(AudioPlayer.class);

	private final BelugaState state = BelugaState.getInstance();
	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private final AudioDeviceManager audioDeviceManager = new AudioDeviceManager();

	private static final int UI_REFRESH_INTERVAL = 200;
	private Future<?> playerUISyncFuture;
	private Future<?> playbackThreadFuture;

	private volatile SongPlay songPlay;
	private volatile long duration;
	private volatile boolean playing = false;
	private volatile boolean stop = false;

	private final MainWindow mainWindow;

	public AudioPlayer(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	public void start() {
		if (playbackThreadFuture != null && !playbackThreadFuture.isDone()) {
			log.error("playbackThreadAlreadyRunning");
			return;
		}

		stop = false;
		playbackThreadFuture = ThreadPools.playbackPool.submit(new Playback());
	}

	public void stop() {
		if (playbackThreadFuture == null || playbackThreadFuture.isDone()) {
			return;
		}

		stop = true;

		// Stop the player.
		songPlay.stop();

		// Stop the playback thread.
		try {
			playbackThreadFuture.get(10, TimeUnit.SECONDS);
		} catch (Exception e) {
			playbackThreadFuture.cancel(true);
			while (!playbackThreadFuture.isDone()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
				}
				log.debug("waiting for playback thread to finish");
			}
		}
	}

	public void skip() {
		if (songPlay == null || !songPlay.isActive())
			return;

		// Stop the player to skip the song.
		songPlay.stop();
	}

	public void toggleMute() {
		if (songPlay == null) {
			return;
		}
		songPlay.toggleMuted();
		uiUpdateMuteButton();
	}

	public void uiUpdateStationName() {
		if (state.getStation() == null) {
			return;
		}
		// Update station name.
		mainWindow.getPlayerUI().getStationName().setText(state.getStation().getStationName());
	}

	private void uiUpdateMuteButton() {
		if (songPlay == null) {
			return;
		}
		// Update mute button state.
		Theme theme = BelugaConfiguration.getInstance().getTheme();
		ButtonData buttonData = (ButtonData) mainWindow.getPlayerUI().getMuteButton().getButtonData();
		buttonData.setIcon(songPlay.isMuted() ? theme.getMuteImagePath() : theme.getVolumeImagePath());
	}

	private void uiUpdateTotalTime() {
		mainWindow.getPlayerUI().getTotalTime().setText(formatTime(duration));
	}

	public void uiResync() {
		uiUpdateStationName();
		uiUpdateMuteButton();
		uiUpdateTotalTime();

		ApplicationContext.queueCallback(new Runnable() {
			@Override
			public void run() {
				mainWindow.getPlayerUI().enableUI(playing);
			}
		}, false);

		// The rest will be handled by the sync-ui thread.
	}

	private Runnable syncUI = new Runnable() {
		@Override
		public void run() {
			if (songPlay == null || !songPlay.isActive())
				return;

			// Update song position (playback can stop anytime).
			state.getSong().setPosition(songPlay.getPosition());

			ApplicationContext.queueCallback(new Runnable() {
				@Override
				public void run() {
					// Update progress bar.
					mainWindow.getPlayerUI().getCurrentTime().setText(formatTime(songPlay.getPosition()));
					mainWindow.getPlayerUI().getProgress().setPercentage(songPlay.getProgressRatio());
					mainWindow.getPlayerUI().getProgressCache().setPercentage(songPlay.getCacheProgressRatio());
				}
			}, true);
		}
	};

	private String formatTime(long ms) {
		return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(ms),
				TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
	}

	private class Playback implements Runnable {
		private String resolveSongURL(Song song, AudioQuality audioQuality) {
			Audio audio = null;
			switch (audioQuality) {
			case HIGH:
				audio = song.getAudioUrlMap().get("highQuality");
				if (audio != null && !StringUtils.isBlank(audio.getAudioUrl()))
					return audio.getAudioUrl();
			case MEDIUM:
				audio = song.getAudioUrlMap().get("mediumQuality");
				if (audio != null && !StringUtils.isBlank(audio.getAudioUrl()))
					return audio.getAudioUrl();
			case LOW:
				audio = song.getAudioUrlMap().get("lowQuality");
				if (audio != null)
					return audio.getAudioUrl();
			case MP3:
				return song.getAdditionalAudioUrl();
			}
			return null;
		}

		@Override
		public void run() {
			// Start the UI synchronization thread.
			playerUISyncFuture = ThreadPools.playerUIScheduler.scheduleAtFixedRate(syncUI, 0, UI_REFRESH_INTERVAL,
					TimeUnit.MILLISECONDS);

			int successiveFailures = 0;
			while (!stop) {
				if (successiveFailures >= 3) {
					log.error("tooManyFailures");
					break;
				}

				successiveFailures++;

				Song song = null;
				try {
					log.debug("getting next song");
					song = PandoraPlaylist.getInstance().getNext();
					if (song == null) {
						// There was a problem, probably session expired.
						break;
					}
					log.debug("new song: {} by {}", song.getSongName(), song.getArtistName());

					String songURL = resolveSongURL(song, configuration.getAudioQuality());
					if (StringUtils.isBlank(songURL)) {
						log.error("resolvingAudioURL");
						continue;
					}
					log.debug("new song URL: {}", songURL);

					// Initialize the player, retry one time.
					for (int attempt = 0; attempt < 2; attempt++) {
						try {
							songPlay = configuration.getAudioQuality().equals(AudioQuality.MP3)
									? new MP3SongPlay(audioDeviceManager)
									: new AACSongPlay(audioDeviceManager);
							log.info("openingAudioStream");
							songPlay.loadSong(songURL);
							log.debug("audio format: {}", configuration.getAudioQuality().toString());
							log.debug("bitrate: {}", songPlay.getBitrate());
							break;
						} catch (Exception e) {
							if (attempt == 0) {
								log.info(e.getMessage(), e);
								Thread.sleep(1000);
							} else {
								throw e;
							}
						}
					}

					duration = songPlay.getDuration();
					song.setDuration(duration);

					// Is there a better way to detect the Pandora skip protection (42sec length mp3)?
					// TODO: Not why this was done initially, why skip? shouldn't we just play it instead? TBD when the
					// situation happens again (didn't hit skip protection for a long time).
					if (duration == 42569 && songPlay.getBitrate() == 64000) {
						log.error("pandoraSkipProtection");
						continue;
					}

					// Guess if it's an ad (not very reliable).
					if (configuration.getAdsDetectionEnabled() && songPlay.getBitrate() == 128000 && duration < 45000) {
						log.debug("ad detected");

						// Set the ad flag on the song, for the display and to skip scrobbling.
						song.setAd(true);
					}

					// Notify other components the song is starting.
					InternalBus.publish(new PlaybackEvent(PlaybackEvent.Type.SONG_START, song));

					playing = true;

					// Initialize controls.
					ApplicationContext.queueCallback(new Runnable() {
						@Override
						public void run() {
							// Update song duration.
							uiUpdateTotalTime();

							// Update station name.
							uiUpdateStationName();

							mainWindow.getPlayerUI().enableUI(true);
						}
					}, false);

					// Check ad and add configuration to know if we should force mute.
					boolean forceMute = configuration.getAdsSilenceEnabled() && song.isAd();
					boolean muted = songPlay.isMuted();
					if (forceMute && !muted) {
						toggleMute();
					}

					// Increase thread priority before starting playback.
					Thread.currentThread().setPriority(Thread.MAX_PRIORITY - 1);

					// Start playback.
					songPlay.play();

					// Restore thread priority to normal.
					Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

					// Unmute if we forced mute before.
					if (forceMute && !muted && songPlay.isMuted()) {
						toggleMute();
					}

					log.debug("playback finished");

					// Reinitialize the successive failure counter.
					successiveFailures = 0;

				} catch (Exception e) {
					// Handle session expiration gracefully.
					if (e instanceof PandoraException) {
						if (PandoraError.INVALID_AUTH_TOKEN.equals(((PandoraException) e).getError())) {
							stop = true;
							break;
						}
					}
					log.warn(e.getMessage(), e);
				} finally {
					playing = false;

					// Reset UI between plays.
					ApplicationContext.queueCallback(new Runnable() {
						@Override
						public void run() {
							mainWindow.getPlayerUI().enableUI(false);

							if (!stop) {
								// Set progress bar to full.
								mainWindow.getPlayerUI().getCurrentTime().setText(formatTime(duration));
								mainWindow.getPlayerUI().getProgress().setPercentage(1);
							}
						}
					}, !stop); // Don't wait when we are stopping to avoid deadlock on with UI thread.

					// Close player, we don't reuse it.
					if (songPlay != null && songPlay.isActive()) {
						log.debug("closing audio player");
						songPlay.close();
					}

					if (song != null && song.getDuration() > 0) {
						// Notify song finished.
						log.debug("publishing SONG_FINISH event");
						InternalBus.publish(new PlaybackEvent(PlaybackEvent.Type.SONG_FINISH, song));
					}

					log.debug("finished player loop post-processing");
				}
			}

			log.debug("exiting playback thread");

			// Stop the UI sync thread.
			playerUISyncFuture.cancel(false);

			// Reset UI.
			ApplicationContext.queueCallback(new Runnable() {
				@Override
				public void run() {
					mainWindow.getPlayerUI().enableUI(false);
					mainWindow.getPlayerUI().resetUI();
				}
			}, false);

			// Notify we are disconnected.
			InternalBus.publish(new PlaybackEvent(PlaybackEvent.Type.PANDORA_DISCONNECTED, null));

			// Also shutdown audio.
			audioDeviceManager.shutdown();
		}
	}

}
