/*
 * Copyright (C) 2012-2018 Julien Bonjean <julien@bonjean.info>
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

import info.bonjean.beluga.connection.BelugaHTTPClient;
import info.bonjean.beluga.connection.CachedInputStream;
import info.bonjean.beluga.exception.InternalException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

/**
 *
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public abstract class AudioPlayer {
	private AudioDevice audioDeviceManager;
	protected volatile boolean active;
	private CachedInputStream cachedInputStream;
	private HttpGet streamRequest;
	private long contentLength;
	private long duration;
	private int bitrate;

	public abstract void loadSong(String url) throws Exception;

	public abstract void play(boolean dummy) throws Exception;

	public abstract long getPosition();

	public final void stop() {
		active = false;
	}

	public final long getDuration() {
		return duration;
	}

	public final void setDuration(long duration) {
		this.duration = duration;
	}

	public final int getBitrate() {
		return bitrate;
	}

	public final void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

	public final double getProgressRatio() {
		return cachedInputStream.getOutCount() / (double) contentLength;
	}

	public final double getCacheProgressRatio() {
		return cachedInputStream.getInCount() / (double) contentLength;
	}

	public final boolean isActive() {
		return active;
	}

	public final long getContentLength() {
		return contentLength;
	}

	public void close() {
		active = false;
		if (audioDeviceManager != null)
			audioDeviceManager.close();
		if (streamRequest != null)
			streamRequest.abort();
		if (cachedInputStream != null)
			cachedInputStream.close();
	}

	public AudioPlayer() {
		this.active = false;
	}

	protected void audioInit(boolean dummy, float sampleRate, int sampleSizeInBits, int channels, boolean signed,
			boolean bigEndian) throws InternalException {
		if (dummy)
			audioDeviceManager = new DummyAudioDevice(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		else
			audioDeviceManager = new SimpleAudioDevice(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		active = true;
	}

	protected void audioWrite(byte[] b, int len) {
		audioDeviceManager.write(b, len);
	}

	protected final InputStream openStream(String url) throws UnsupportedOperationException, IOException {
		streamRequest = new HttpGet(url);
		HttpResponse httpResponse = BelugaHTTPClient.AUDIO_STREAM_INSTANCE.getStream(streamRequest);
		contentLength = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());
		cachedInputStream = new CachedInputStream(httpResponse.getEntity().getContent());
		return cachedInputStream;
	}

	protected long getStreamInCount() {
		return cachedInputStream.getInCount();
	}

	protected long getStreamOutCount() {
		return cachedInputStream.getOutCount();
	}
}
