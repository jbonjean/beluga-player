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
package info.bonjean.beluga.player;

import java.util.Arrays;

import info.bonjean.beluga.exception.InternalException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.SampleBuffer;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class AudioDevice
{
	private static final Logger log = LoggerFactory.getLogger(AudioDevice.class);

	private static final int MAX_VOLUME = Integer.MAX_VALUE;

	private SourceDataLine sourceDataLine;
	private static Port port;
	private FloatControl floatControl;
	private static final byte[] byteBuffer = new byte[SampleBuffer.OBUFFERSIZE * 2];
	private boolean mute;

	public AudioDevice(Decoder decoder) throws InternalException
	{

		try
		{
			AudioFormat audioFormat = new AudioFormat(decoder.getOutputFrequency(), 16,
					decoder.getOutputChannels(), true, false);
			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
			Line line = AudioSystem.getLine(dataLineInfo);

			if (!(line instanceof SourceDataLine))
				return;

			// open the dataline
			sourceDataLine = (SourceDataLine) line;
			sourceDataLine.open(audioFormat);

			if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0 && port == null)
			{
				/*
				 * JavaSound is a bit messy, if we are on windows, try to find a
				 * Port to control the volume
				 */
				if (AudioSystem.isLineSupported(Port.Info.LINE_OUT))
					port = (Port) AudioSystem.getLine(Port.Info.LINE_OUT);
				else if (AudioSystem.isLineSupported(Port.Info.HEADPHONE))
					port = (Port) AudioSystem.getLine(Port.Info.HEADPHONE);
				else if (AudioSystem.isLineSupported(Port.Info.SPEAKER))
					port = (Port) AudioSystem.getLine(Port.Info.SPEAKER);
				else
					log.error("cannotFindAudioPort");

				if (port != null)
				{
					log.debug("Found control port");
					port.open();
					floatControl = (FloatControl) port.getControl(FloatControl.Type.VOLUME);
					return;
				}
			}

			if (sourceDataLine.isControlSupported(FloatControl.Type.VOLUME))
			{
				log.debug("Found volume control");
				floatControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.VOLUME);
				return;
			}

			if (sourceDataLine.isControlSupported(FloatControl.Type.MASTER_GAIN))
			{
				// XXX: this probably won't work, gain needs to be linearized
				log.debug("Found gain control");
				floatControl = (FloatControl) sourceDataLine
						.getControl(FloatControl.Type.MASTER_GAIN);
				return;
			}
		}
		catch (LineUnavailableException e)
		{
			// nothing to do, handle everything in the finally block
		}
		finally
		{
			if (sourceDataLine == null)
				throw new InternalException("cannotGetSourceDataLine");

			if (floatControl == null)
			{
				log.error("cannotGetFloatControl");
				return;
			}

			sourceDataLine.start();
		}
	}

	public SourceDataLine getSourceDataLine()
	{
		return sourceDataLine;
	}

	public boolean isVolumeControlAvailable()
	{
		return floatControl != null;
	}

	public int getVolume()
	{
		if (floatControl == null)
			return 0;

		return (int) Math.floor(MAX_VOLUME * (floatControl.getValue() / floatControl.getMaximum()));
	}

	public void setVolume(int value)
	{
		floatControl.setValue(value * floatControl.getMaximum() / MAX_VOLUME);
	}

	public int getVolumeMin()
	{
		return 0;
	}

	public int getVolumeMax()
	{
		return MAX_VOLUME;
	}

	public void close()
	{
		sourceDataLine.drain();
		sourceDataLine.close();
	}

	protected byte[] toByteArray(short[] samples, int offset, int length)
	{
		if (mute)
			// TODO: avoid decoding instead of doing everything to finally
			// fill with zeros... (Maybe implementing a DummySampleBuffer)
			Arrays.fill(byteBuffer, 0, length, (byte) 0);
		else
		{
			int idx = 0;
			short s;
			while (length-- > 0)
			{
				s = samples[offset++];
				byteBuffer[idx++] = (byte) s;
				byteBuffer[idx++] = (byte) (s >>> 8);
			}
		}
		return byteBuffer;
	}

	public void write(SampleBuffer output)
	{
		int length = output.getBufferLength();
		sourceDataLine.write(toByteArray(output.getBuffer(), 0, length), 0, length * 2);
	}

	public void mute()
	{
		mute = !mute;
	}
}
