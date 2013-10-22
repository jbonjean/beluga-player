/*
 * 11/26/04		Buffer size modified to support JRE 1.5 optimizations.
 *              (CPU usage < 1% under P4/2Ghz, RAM < 12MB).
 *              jlayer@javazoom.net
 * 11/19/04		1.0 moved to LGPL.
 * 06/04/01		Too fast playback fixed. mdm@techie.com
 * 29/01/00		Initial version. mdm@techie.com
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package javazoom.jl.player;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.JavaLayerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>JavaSoundAudioDevice</code> implements an audio device by using the JavaSound API.
 * 
 * @since 0.0.8
 * @author Mat McGowan
 */
public class JavaSoundAudioDevice extends AudioDeviceBase
{
	private static Logger log = LoggerFactory.getLogger(JavaSoundAudioDevice.class);

	private SourceDataLine source = null;

	private AudioFormat fmt = null;

	private byte[] byteBuf = new byte[4096];

	private FloatControl floatControl = null;

	private static float masterGain = Float.MAX_VALUE;

	protected void setAudioFormat(AudioFormat fmt0)
	{
		fmt = fmt0;
	}

	protected AudioFormat getAudioFormat()
	{
		if (fmt == null)
		{
			Decoder decoder = getDecoder();
			fmt = new AudioFormat(decoder.getOutputFrequency(), 16, decoder.getOutputChannels(), true, false);
		}
		return fmt;
	}

	protected DataLine.Info getSourceLineInfo()
	{
		AudioFormat fmt = getAudioFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
		return info;
	}

	public void open(AudioFormat fmt) throws JavaLayerException
	{
		if (!isOpen())
		{
			setAudioFormat(fmt);
			openImpl();
			setOpen(true);
		}
	}

	protected void openImpl() throws JavaLayerException
	{
	}

	// createSource fix.
	protected void createSource() throws JavaLayerException
	{
		Throwable t = null;
		try
		{
			Line line = AudioSystem.getLine(getSourceLineInfo());
			if (line instanceof SourceDataLine)
			{
				source = (SourceDataLine) line;
				source.open(fmt);

				if (source.isControlSupported(FloatControl.Type.MASTER_GAIN))
				{
					log.debug("Control found: " + FloatControl.Type.MASTER_GAIN);
					floatControl = (FloatControl) source.getControl(FloatControl.Type.MASTER_GAIN);

					// in some conditions, gain value is not persistent between playback, so we
					// manually restore it
					if (masterGain == Float.MAX_VALUE)
						masterGain = floatControl.getValue();
					else
					{
						if (masterGain != floatControl.getValue())
						{
							floatControl.setValue(masterGain);
							log.debug("Master gain value manually restored");
						}
					}
				}
				else if (source.isControlSupported(FloatControl.Type.VOLUME))
				{
					floatControl = (FloatControl) source.getControl(FloatControl.Type.VOLUME);
					log.debug("Control found: " + FloatControl.Type.VOLUME);
				}
				else
					log.info("noAudioControlFound");

				source.start();

			}
		}
		catch (RuntimeException ex)
		{
			t = ex;
		}
		catch (LinkageError ex)
		{
			t = ex;
		}
		catch (LineUnavailableException ex)
		{
			t = ex;
		}
		if (source == null)
			throw new JavaLayerException("cannot obtain source audio line", t);
	}

	public int millisecondsToBytes(AudioFormat fmt, int time)
	{
		return (int) (time * (fmt.getSampleRate() * fmt.getChannels() * fmt.getSampleSizeInBits()) / 8000.0);
	}

	protected void closeImpl()
	{
		if (source != null)
		{
			// backup master gain value before closing source
			if (floatControl != null && floatControl.getType() == FloatControl.Type.MASTER_GAIN)
				masterGain = floatControl.getValue();
			
			source.close();
		}
	}

	protected void writeImpl(short[] samples, int offs, int len) throws JavaLayerException
	{
		if (source == null)
			createSource();

		byte[] b = toByteArray(samples, offs, len);
		source.write(b, 0, len * 2);
	}

	protected byte[] getByteArray(int length)
	{
		if (byteBuf.length < length)
		{
			byteBuf = new byte[length + 1024];
		}
		return byteBuf;
	}

	protected byte[] toByteArray(short[] samples, int offs, int len)
	{
		byte[] b = getByteArray(len * 2);
		int idx = 0;
		short s;
		while (len-- > 0)
		{
			s = samples[offs++];
			b[idx++] = (byte) s;
			b[idx++] = (byte) (s >>> 8);
		}
		return b;
	}

	protected void flushImpl()
	{
		if (source != null)
		{
			source.drain();
		}
	}

	public int getPosition()
	{
		int pos = 0;
		if (source != null)
		{
			pos = (int) (source.getMicrosecondPosition() / 1000);
		}
		return pos;
	}

	/**
	 * Runs a short test by playing a short silent sound.
	 */
	public void test() throws JavaLayerException
	{
		try
		{
			open(new AudioFormat(22050, 16, 1, true, false));
			short[] data = new short[22050 / 10];
			write(data, 0, data.length);
			flush();
			close();
		}
		catch (RuntimeException ex)
		{
			throw new JavaLayerException("Device test failed: " + ex);
		}

	}

	public FloatControl getFloatControl()
	{
		return floatControl;
	}
}
