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

import info.bonjean.beluga.connection.BelugaHTTPClient;
import info.bonjean.beluga.connection.CachedInputStream;
import info.bonjean.beluga.exception.CommunicationException;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.sound.sampled.FloatControl;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.JavaSoundAudioDevice;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class MP3Player
{
	private static final Logger log = LoggerFactory.getLogger(MP3Player.class);

	private Bitstream bitstream;
	private CachedInputStream cachedInputStream;
	private Decoder decoder;
	private AudioDevice audio;
	private boolean close = true;
	private boolean pause = false;
	private boolean active = false;
	private long duration;
	private HttpGet httpGet;
	private int bitrate;
	private FloatControl volumeControl;
	private boolean silence = false;

	public void openAudioDevice() throws JavaLayerException
	{
		if (audio != null)
		{
			log.error("audioDeviceAlreadyOpen");
			return;
		}
		FactoryRegistry r = FactoryRegistry.systemRegistry();
		audio = r.createAudioDevice();
	}

	public void closeAudioDevice()
	{
		if (audio != null)
		{
			audio.flush();
			audio.close();
		}
	}

	public MP3Player()
	{
	}

	public void loadSong(String url) throws JavaLayerException, MalformedURLException, IOException,
			CommunicationException
	{
		httpGet = new HttpGet(url);
		HttpResponse httpResponse = BelugaHTTPClient.getInstance().getClient().execute(httpGet);

		if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
		{
			release();
			log.debug("Got response: " + httpResponse.getStatusLine().getReasonPhrase());

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN)
			{
				try
				{
					// blacklist address used for this request
					BelugaHTTPClient.getInstance().blacklist(httpResponse);
				}
				catch (Exception e)
				{
					// not critical, we just didn't blacklist it
					log.info(e.getMessage(), e);
				}
			}

			throw new IOException("Server reply: " + httpResponse.getStatusLine().getReasonPhrase());
		}

		cachedInputStream = new CachedInputStream(httpResponse.getEntity());

		bitstream = new Bitstream(cachedInputStream);

		// get the first frame header to get bitrate
		Header frame = bitstream.readFrame();
		if (frame == null)
			throw new IOException("noAudioStream");
		bitrate = frame.bitrate();
		bitstream.unreadFrame();

		// get file size from HTTP headers
		long songSize = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());

		// calculate the duration
		duration = (songSize * 1000) / (bitrate / 8);
	}

	public FloatControl getVolumeControl()
	{
		if (close)
			return null;

		if (volumeControl == null && audio != null && audio instanceof JavaSoundAudioDevice)
			volumeControl = ((JavaSoundAudioDevice) audio).getFloatControl();

		return volumeControl;
	}

	public void play() throws JavaLayerException
	{
		close = false;
		active = true;
		decoder = new Decoder();
		audio.open(decoder);
		try
		{
			while (decodeFrame())
			{
				while (pause)
				{
					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
						break;
					}
				}
			}
		}
		catch (JavaLayerException e)
		{
			active = false;
			throw e;
		}
		audio.flush();
		cleanResources();
	}

	private void cleanResources()
	{
		active = false;
		try
		{
			if (bitstream != null)
				bitstream.close();
		}
		catch (BitstreamException e)
		{
			log.debug(e.getMessage());
		}
		// clean up http connection
		if (httpGet != null)
			httpGet.releaseConnection();
	}

	public void release()
	{
		close = true;
		pause = false;
		if (!active)
			cleanResources();
	}

	public void pause()
	{
		pause = !pause;
	}

	public void mute()
	{
		pause = !pause;
	}

	public long getCachePosition()
	{
		try
		{
			return ((cachedInputStream.available() + bitstream.getPosition()) * 1000)
					/ (bitrate / 8);
		}
		catch (IOException e)
		{
			log.debug(e.getMessage());
			return 0;
		}
	}

	public long getPosition()
	{
		return (bitstream.getPosition() * 1000) / (bitrate / 8);
	}

	protected boolean decodeFrame() throws JavaLayerException
	{
		if (close)
			return false;

		try
		{
			Header h = bitstream.readFrame();

			if (h == null)
				return false;

			SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h, bitstream);
			/*
			 * This is where we handle silenced song.
			 * We don't change audio device settings, and read the stream
			 * as we were really playing it.
			 */
			short[] buffer = silence ? new short[output.getBufferLength()] : output.getBuffer();
			audio.write(buffer, 0, output.getBufferLength());
			bitstream.closeFrame();
		}
		catch (RuntimeException ex)
		{
			throw new JavaLayerException("Exception decoding audio frame", ex);
		}
		return true;
	}

	public long getDuration()
	{
		return duration;
	}

	public boolean isPaused()
	{
		return pause;
	}

	public int getBitrate()
	{
		return bitrate;
	}

	public void setSilence(boolean silence)
	{
		this.silence = silence;
	}

	public boolean isActive()
	{
		return active;
	}
}
