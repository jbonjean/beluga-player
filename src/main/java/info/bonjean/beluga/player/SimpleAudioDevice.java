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
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class SimpleAudioDevice implements AudioDevice {
	private SourceDataLine sourceDataLine;

	public SimpleAudioDevice(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian)
			throws InternalException {
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
	}

	@Override
	public void close() {
		sourceDataLine.drain();
		sourceDataLine.close();
	}

	@Override
	public void write(byte[] b, int len) {
		sourceDataLine.write(b, 0, len);
	}
}
