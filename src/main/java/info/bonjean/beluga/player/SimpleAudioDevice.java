/*
 * Copyright (C) 2012-2016 Julien Bonjean <julien@bonjean.info>
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

import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.SampleBuffer;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class SimpleAudioDevice implements AudioDevice
{
	private static final byte[] byteBuffer = new byte[SampleBuffer.OBUFFERSIZE * 2];
	private SourceDataLine sourceDataLine;

	public SimpleAudioDevice(Decoder decoder) throws InternalException
	{
		try
		{
			AudioFormat audioFormat = new AudioFormat(decoder.getOutputFrequency(), 16,
					decoder.getOutputChannels(), true, false);
			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
			Line line = AudioSystem.getLine(dataLineInfo);

			if (!(line instanceof SourceDataLine))
				throw new InternalException("cannotGetSourceDataLine");

			// open the dataline
			sourceDataLine = (SourceDataLine) line;
			sourceDataLine.open(audioFormat);
			sourceDataLine.start();
		}
		catch (LineUnavailableException e)
		{
			throw new InternalException("cannotGetSourceDataLine");
		}
	}

	@Override
	public void close()
	{
		sourceDataLine.drain();
		sourceDataLine.close();
	}

	@Override
	public void write(SampleBuffer output)
	{
		int length = output.getBufferLength();
		sourceDataLine.write(toByteArray(output.getBuffer(), 0, length), 0, length * 2);
	}

	protected byte[] toByteArray(short[] samples, int offset, int length)
	{
		int idx = 0;
		short s;
		while (length-- > 0)
		{
			s = samples[offset++];
			byteBuffer[idx++] = (byte) s;
			byteBuffer[idx++] = (byte) (s >>> 8);
		}
		return byteBuffer;
	}
}
