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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javazoom.jl.decoder.Bitstream;
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
	private int lastPosition = 0;
	private final float duration;
	private Header header;

	public BelugaMP3Player(String url) throws JavaLayerException, MalformedURLException, IOException
	{
		HttpResponse httpResponse = BelugaHTTPClient.getInstance().getClient().execute(new HttpGet(url));
		InputStream inputStream = httpResponse.getEntity().getContent();

		cachedInputStream = new CachedInputStream(inputStream);

		bitstream = new Bitstream(cachedInputStream);
		decoder = new Decoder();

		FactoryRegistry r = FactoryRegistry.systemRegistry();
		audio = r.createAudioDevice();

		audio.open(decoder);

		// get the first frame header to get bitrate
		header = bitstream.readFrame();
		bitstream.unreadFrame();

		// get file size from HTTP headers
		int songSize = Integer.parseInt(httpResponse.getFirstHeader("Content-Length").getValue());

		// calculate the duration
		duration = header.total_ms(songSize);
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
	}

	public synchronized void close()
	{
		AudioDevice out = audio;
		if (out != null)
		{
			closed = true;
			audio = null;
			out.close();
			lastPosition = out.getPosition();
			try
			{
				bitstream.close();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public synchronized boolean isComplete()
	{
		return complete;
	}

	public float getCachePosition()
	{
		return header.total_ms(cachedInputStream.getPosition());
	}

	public int getPosition()
	{
		int position = lastPosition;

		AudioDevice out = audio;
		if (out != null)
		{
			position = out.getPosition();
		}
		return position;
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

	public float getDuration()
	{
		return duration;
	}
}
