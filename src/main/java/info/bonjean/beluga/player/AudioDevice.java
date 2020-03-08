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
package info.bonjean.beluga.player;

import info.bonjean.beluga.exception.InternalException;
import javax.sound.sampled.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class AudioDevice {
	private static final Logger log = LoggerFactory.getLogger(AudioDevice.class);
	private static final AudioDevice INSTANCE = new AudioDevice();

	float sampleRate;
	float sampleSizeInBits;
	int channels;
	boolean signed;
	boolean bigEndian;

	private boolean muted;

	private Thread audioThread;

	private BufferQueue bufferQueue;
	private byte[] zeroBuffer;
	private volatile boolean shutdown;

	private AudioDevice() {
	}

	public static AudioDevice getInstance() {
		return INSTANCE;
	}

	private void init(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian)
			throws InternalException {
		shutdown();

		SourceDataLine sourceDataLine;
		try {
			AudioFormat audioFormat = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
			Line line = AudioSystem.getLine(dataLineInfo);

			if (!(line instanceof SourceDataLine))
				throw new InternalException("cannotGetSourceDataLine");

			// open the dataline
			sourceDataLine = (SourceDataLine) line;
			sourceDataLine.open(audioFormat);
			sourceDataLine.start();

		} catch (LineUnavailableException e) {
			throw new InternalException("cannotGetSourceDataLine");
		}

		this.sampleRate = sampleRate;
		this.sampleSizeInBits = sampleSizeInBits;
		this.channels = channels;
		this.signed = signed;
		this.bigEndian = bigEndian;

		// compute size of 1 second worth of audio.
		int bytesPerSeconds = (int) sampleRate * channels * (sampleSizeInBits / 8);
		log.debug("Computed B/s: {}", bytesPerSeconds);

		// prepare the buffer of zero (to fill missing data)
		zeroBuffer = new byte[bytesPerSeconds];

		bufferQueue = new BufferQueue(bytesPerSeconds);

		audioThread = new Thread(new Runnable() {
			@Override
			public void run() {
				log.debug("Starting audio thread");
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

				byte[] buffer;

				while (!shutdown) {
					buffer = bufferQueue.read();

					// warning: it seems that SourceDataLine swallows interrupt so we use a volatile variable.
					if (buffer == null) {
						log.debug("No data to read");
						sourceDataLine.write(zeroBuffer, 0, zeroBuffer.length);
					} else {
						sourceDataLine.write(buffer, 0, buffer.length);
					}
				}
				sourceDataLine.drain();
				sourceDataLine.close();
				log.debug("Exiting audio thread");
			}
		});

		shutdown = false;
		audioThread.start();
	}

	public void shutdown() {
		if (audioThread != null) {
			shutdown = true;
			try {
				audioThread.join();
			} catch (InterruptedException e) {
				log.debug("Interrupted during audio thread join");
				Thread.currentThread().interrupt();
				return;
			}
		}
		if (bufferQueue != null) {
			bufferQueue.close();
		}
	}

	public void setup(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian)
			throws InternalException {
		if (audioThread == null) {
			init(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
			return;
		}

		if (sampleRate != this.sampleRate || sampleSizeInBits != this.sampleSizeInBits || channels != this.channels
				|| signed != this.signed || bigEndian != this.bigEndian) {
			log.warn("Audio format changed, reinitializing audio device");
			init(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
			return;
		}

		log.debug("Reusing current audio line");
	}

	public void finish() {
		while (bufferQueue.available() > 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void clear() {
		bufferQueue.clear();
	}

	public void write(byte[] b, int len) {
		byte[] bufferCopy = new byte[len];
		if (!muted) {
			// create a copy of the buffer because the players reuse their buffer.
			System.arraycopy(b, 0, bufferCopy, 0, len);
		}
		bufferQueue.write(bufferCopy);
	}

	public void write(short[] s, int len) {
		byte[] bufferCopy = new byte[len * 2];
		if (!muted) {
			for (int i = 0; i < len; i++) {
				bufferCopy[i * 2] = (byte) s[i];
				bufferCopy[i * 2 + 1] = (byte) (s[i] >>> 8);
			}
		}
		bufferQueue.write(bufferCopy);
	}

	public void toggleMuted() {
		muted = !muted;
		if (bufferQueue != null) {
			bufferQueue.clear();
		}
	}

	public boolean isMuted() {
		return muted;
	}

	public static class BufferQueue {
		private final int size;
		final byte[] buffer;
		private final Semaphore available;
		private final ReentrantLock readLock = new ReentrantLock(true);
		private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();

		public BufferQueue(int size) {
			this.size = size;
			buffer = new byte[size];
			available = new Semaphore(size, true);
		}

		public void clear() {
			readLock.lock();
			try {
				int length = queue.size();
				for (int i = 0; i < length; i++) {
					_read();
				}
			} finally {
				readLock.unlock();
			}
		}

		public void close() {
		}

		public int available() {
			return size - available.availablePermits();
		}

		public void write(byte[] b) {
			try {
				available.acquire(b.length);
				queue.add(b);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		public byte[] read() {
			readLock.lock();
			try {
				return _read();
			} finally {
				readLock.unlock();
			}
		}

		private byte[] _read() {
			byte[] data = queue.poll();
			if (data != null) {
				available.release(data.length);
			}
			return data;
		}
	}
}
