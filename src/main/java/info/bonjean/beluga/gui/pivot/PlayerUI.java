/*
 * Copyright (C) 2012-2020 Julien Bonjean <julien@bonjean.info>
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

import info.bonjean.beluga.bus.InternalBus;
import info.bonjean.beluga.bus.PlaybackEvent;
import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.PandoraPlaylist;
import info.bonjean.beluga.configuration.AudioQuality;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.exception.PandoraError;
import info.bonjean.beluga.exception.PandoraException;
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.player.AACPlayer;
import info.bonjean.beluga.player.AudioDevice;
import info.bonjean.beluga.player.AudioPlayer;
import info.bonjean.beluga.player.MP3Player;
import info.bonjean.beluga.response.Audio;
import info.bonjean.beluga.response.Song;
import org.apache.commons.lang3.StringUtils;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.content.ButtonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PlayerUI extends TablePane implements Bindable {
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
	protected LinkButton muteButton;

	private final BelugaState state = BelugaState.getInstance();
	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();
	private static final int UI_REFRESH_INTERVAL = 200;
	private Future<?> playerUISyncFuture;
	private Future<?> playbackThreadFuture;

	private volatile AudioPlayer audioPlayer;
	private volatile long duration;
	private volatile boolean playing = false;
	private volatile boolean stop = false;

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		resetUI();
	}

	private String formatTime(long ms) {
		return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(ms),
				TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
	}

	private void resetUI() {
		enableUI(false);
		stationName.setText("");
		currentTime.setText("00:00");
		totalTime.setText("00:00");
		progress.setPercentage(0);
		progressCache.setPercentage(0);
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

		// stop the player.
		audioPlayer.stop();

		// stop the playback thread.
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
		if (audioPlayer == null || !audioPlayer.isActive())
			return;

		// stop the player to skip the song
		audioPlayer.stop();
	}

	private Runnable syncUI = new Runnable() {
		@Override
		public void run() {
			if (audioPlayer == null || !audioPlayer.isActive())
				return;

			// update song position (playback can stop anytime)
			state.getSong().setPosition(audioPlayer.getPosition());

			ApplicationContext.queueCallback(new Runnable() {
				@Override
				public void run() {
					// update progress bar
					currentTime.setText(formatTime(audioPlayer.getPosition()));
					progress.setPercentage(audioPlayer.getProgressRatio());
					progressCache.setPercentage(audioPlayer.getCacheProgressRatio());
				}
			}, true);
		}
	};

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
			// start the UI synchronization thread
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
						// there was a problem, probably session expired.
						break;
					}
					log.debug("new song: {} by {}", song.getSongName(), song.getArtistName());

					String songURL = resolveSongURL(song, configuration.getAudioQuality());
					if (StringUtils.isBlank(songURL)) {
						log.error("resolvingAudioURL");
						continue;
					}
					log.debug("new song URL: {}", songURL);

					// initialize the player, retry one time.
					for (int attempt = 0; attempt < 2; attempt++) {
						try {
							audioPlayer = configuration.getAudioQuality().equals(AudioQuality.MP3) ? new MP3Player()
									: new AACPlayer();
							log.info("openingAudioStream");
							audioPlayer.loadSong(songURL);
							log.debug("audio format: {}", configuration.getAudioQuality().toString());
							log.debug("bitrate: {}", audioPlayer.getBitrate());
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

					duration = audioPlayer.getDuration();
					song.setDuration(duration);

					// is there a better way to detect the Pandora skip protection (42sec length mp3)?
					// TODO: not why this was done initially, why skip? shouldn't we just play it instead? TBD when the
					// situation happens again (didn't hit skip protection for a long time).
					if (duration == 42569 && audioPlayer.getBitrate() == 64000) {
						log.error("pandoraSkipProtection");
						continue;
					}

					// guess if it's an ad (not very reliable).
					if (configuration.getAdsDetectionEnabled() && audioPlayer.getBitrate() == 128000
							&& duration < 45000) {
						log.debug("ad detected");

						// set the ad flag on the song, for the display and to skip scrobbling.
						song.setAd(true);
					}

					// notify other components the song is starting.
					InternalBus.publish(new PlaybackEvent(PlaybackEvent.Type.SONG_START, song));

					playing = true;

					// initialize controls
					ApplicationContext.queueCallback(new Runnable() {
						@Override
						public void run() {
							// update song duration
							totalTime.setText(formatTime(duration));

							// update station name
							stationName.setText(state.getStation().getStationName());

							enableUI(true);
						}
					}, false);

					// check ad and add configuration to know if we should force mute.
					boolean forceMute = configuration.getAdsSilenceEnabled() && song.isAd();
					boolean muted = audioPlayer.isMuted();
					if (forceMute && !muted) {
						toggleMute();
					}

					// increase thread priority before starting playback
					Thread.currentThread().setPriority(Thread.MAX_PRIORITY - 1);

					// start playback
					audioPlayer.play();

					// restore thread priority to normal
					Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

					// unmute if we forced mute before.
					if (forceMute && !muted && audioPlayer.isMuted()) {
						toggleMute();
					}

					log.debug("playback finished");

					// reinitialize the successive failure counter.
					successiveFailures = 0;

				} catch (Exception e) {
					// handle session expiration gracefully.
					if (e instanceof PandoraException) {
						if (PandoraError.INVALID_AUTH_TOKEN.equals(((PandoraException) e).getError())) {
							stop = true;
							break;
						}
					}
					log.warn(e.getMessage(), e);
				} finally {
					playing = false;

					// reset UI between plays.
					ApplicationContext.queueCallback(new Runnable() {
						@Override
						public void run() {
							enableUI(false);

							if (!stop) {
								// set progress bar to full.
								currentTime.setText(formatTime(duration));
								progress.setPercentage(1);
							}
						}
					}, !stop); // don't wait when we are stopping to avoid deadlock on with UI thread.

					// close player, we don't reuse it.
					if (audioPlayer != null && audioPlayer.isActive()) {
						log.debug("closing audio player");
						audioPlayer.close();
					}

					if (song != null && song.getDuration() > 0) {
						// notify song finished.
						log.debug("publishing SONG_FINISH event");
						InternalBus.publish(new PlaybackEvent(PlaybackEvent.Type.SONG_FINISH, song));
					}

					log.debug("finished player loop post-processing");
				}
			}

			log.debug("exiting playback thread");

			// stop the UI sync thread.
			playerUISyncFuture.cancel(false);

			// reset UI.
			ApplicationContext.queueCallback(new Runnable() {
				@Override
				public void run() {
					resetUI();
				}
			}, false);

			// notify we are disconnected.
			InternalBus.publish(new PlaybackEvent(PlaybackEvent.Type.PANDORA_DISCONNECTED, null));

			// also shutdown audio.
			AudioDevice.getInstance().shutdown();
		}
	}

	public void enableUI(boolean enabled) {
		boolean enable = playing && enabled;
		PivotUI.enableComponent(muteButton, enable);
		PivotUI.enableComponent(nextButton, enable);
		PivotUI.enableComponent(currentTime, enable);
		PivotUI.enableComponent(totalTime, enable);
		PivotUI.enableComponent(progress, enable);
		PivotUI.enableComponent(progressCache, enable);
	}

	public void refreshStationName() {
		ApplicationContext.queueCallback(new Runnable() {
			@Override
			public void run() {
				// update station name
				stationName.setText(state.getStation().getStationName());
			}
		}, false);
	}

	public void toggleMute() {
		if (audioPlayer == null) {
			return;
		}
		audioPlayer.toggleMuted();
		ButtonData buttonData = (ButtonData) muteButton.getButtonData();
		buttonData.setIcon(audioPlayer.isMuted() ? "/img/mute.svg" : "/img/volume.svg");
	}
}
