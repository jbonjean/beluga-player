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

import info.bonjean.beluga.exception.InternalException;

import javax.sound.sampled.AudioFormat;

import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.SampleBuffer;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class DummyAudioDevice implements AudioDevice {
	private static final int TIME_SLEEP_OFFSET = 500;
	private AudioFormat audioFormat;
	private int timeBuffer;

	public DummyAudioDevice(Decoder decoder) throws InternalException {
		audioFormat = new AudioFormat(decoder.getOutputFrequency(), 16, decoder.getOutputChannels(), true, false);
		timeBuffer = 0;
	}

	@Override
	public void close() {
	}

	@Override
	public void write(SampleBuffer output) {
		int length = output.getBufferLength() * 2;
		if (length % audioFormat.getFrameSize() != 0)
			throw new IllegalArgumentException(
					"Number of bytes does not represent an integral number of sample frames.");
		int flen = length / audioFormat.getFrameSize();
		long time = (long) (flen * (1000.0 / (double) audioFormat.getSampleRate()));

		timeBuffer += time;

		// we try to have a long sleep instead of multiple short ones
		if (timeBuffer < TIME_SLEEP_OFFSET)
			return;

		try {
			Thread.sleep(timeBuffer);
		} catch (InterruptedException e) {
		} finally {
			timeBuffer = 0;
		}
	}
}
