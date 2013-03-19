/*
 * 11/19/04		1.0 moved to LGPL.
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

import info.bonjean.beluga.connection.BelugaHTTPClient;
import info.bonjean.beluga.connection.CachedInputStream;
import info.bonjean.beluga.exception.CommunicationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.sound.sampled.FloatControl;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

/**
 * The <code>BelugaMP3Player</code> class implements a simple player for playback of an MPEG audio stream.
 * 
 * @author Mat McGowan
 * @author Julien Bonjean <julien@bonjean.info>
 * @since 0.0.8
 */
public class BelugaMP3Player
{
	private Bitstream bitstream;
	private CachedInputStream cachedInputStream;
	private Decoder decoder;
	private AudioDevice audio;
	private boolean closed = false;
	private boolean complete = false;
	private long duration;
	private HttpGet httpGet;
	private int bitrate;

	public BelugaMP3Player(String url) throws JavaLayerException, MalformedURLException, IOException, CommunicationException
	{
		httpGet = new HttpGet(url);
		HttpResponse httpResponse = BelugaHTTPClient.getInstance().getClient().execute(httpGet);
		InputStream inputStream = httpResponse.getEntity().getContent();

		cachedInputStream = new CachedInputStream(inputStream);

		bitstream = new Bitstream(cachedInputStream);
		decoder = new Decoder();

		FactoryRegistry r = FactoryRegistry.systemRegistry();
		audio = r.createAudioDevice();

		audio.open(decoder);

		// get the first frame header to get bitrate
		bitrate = bitstream.readFrame().bitrate();
		bitstream.unreadFrame();

		// get file size from HTTP headers
		long songSize = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());

		// calculate the duration
		duration = ((songSize * 1000) / (bitrate / 8));

		// is there a better way to detect the Pandora skip protection (42sec length mp3)?
		if (songSize == 340554 && bitrate == 64000)
		{
			close();
			throw new CommunicationException("pandoraSkipProtection");
		}
	}

	public FloatControl getFloatControl()
	{
		if (audio instanceof JavaSoundAudioDevice)
			return ((JavaSoundAudioDevice) audio).getFloatControl();

		return null;
	}

	public void play() throws JavaLayerException
	{
		while (decodeFrame())
			;

		AudioDevice out = audio;
		if (out != null)
		{
			out.flush();
			synchronized (this)
			{
				complete = (!closed);
				close();
			}
		}

		// if close called from outside, allow it to exit before us
		synchronized (this)
		{
		}
	}

	public synchronized void close()
	{
		AudioDevice out = audio;
		if (out != null)
		{
			closed = true;
			audio = null;
			out.close();
			try
			{
				bitstream.close();
			}
			catch (BitstreamException e)
			{
				e.printStackTrace();
			}
			// clean up http connection
			httpGet.releaseConnection();
		}
	}

	public synchronized boolean isComplete()
	{
		return complete;
	}

	public long getCachePosition()
	{
		return (cachedInputStream.getPosition() * 1000) / (bitrate / 8);
	}

	public long getPosition()
	{
		return (bitstream.getPosition() * 1000) / (bitrate / 8);
	}

	protected boolean decodeFrame() throws JavaLayerException
	{
		try
		{
			AudioDevice out = audio;
			if (out == null)
				return false;

			synchronized (this)
			{
				Header h = bitstream.readFrame();

				if (h == null)
					return false;

				SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h, bitstream);

				out = audio;
				if (out != null)
				{
					out.write(output.getBuffer(), 0, output.getBufferLength());
				}

				bitstream.closeFrame();
			}

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
}
