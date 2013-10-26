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
import java.net.MalformedURLException;

import javax.sound.sampled.FloatControl;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>BelugaMP3Player</code> class implements a simple player for playback of an MPEG audio stream.
 * 
 * @author Mat McGowan
 * @author Julien Bonjean <julien@bonjean.info>
 * @since 0.0.8
 */
public class BelugaMP3Player
{
	private static final Logger log = LoggerFactory.getLogger(BelugaMP3Player.class);

	private Bitstream bitstream;
	private CachedInputStream cachedInputStream;
	private Decoder decoder;
	private AudioDevice audio;
	private boolean close = true;
	private boolean pause = false;
	private long duration;
	private HttpGet httpGet;
	private int bitrate;
	private FloatControl volumeControl;

	public BelugaMP3Player(String url) throws JavaLayerException, MalformedURLException, IOException, CommunicationException
	{
		httpGet = new HttpGet(url);
		HttpResponse httpResponse = BelugaHTTPClient.getInstance().getClient().execute(httpGet);

		if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
		{
			close();
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

			throw new IOException("streamNotAvailable");
		}

		cachedInputStream = new CachedInputStream(httpResponse.getEntity());

		bitstream = new Bitstream(cachedInputStream);
		decoder = new Decoder();

		FactoryRegistry r = FactoryRegistry.systemRegistry();
		audio = r.createAudioDevice();

		audio.open(decoder);

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

		// is there a better way to detect the Pandora skip protection (42sec length mp3)?
		if (songSize == 340554 && bitrate == 64000)
		{
			close();
			throw new CommunicationException("pandoraSkipProtection");
		}
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

		if (audio != null)
		{
			audio.flush();
			audio.close();
		}
		try
		{
			bitstream.close();
		}
		catch (BitstreamException e)
		{
			log.debug(e.getMessage());
		}
		// clean up http connection
		httpGet.releaseConnection();
	}

	public void close()
	{
		close = true;
		pause = false;
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
			return ((cachedInputStream.available() + bitstream.getPosition()) * 1000) / (bitrate / 8);
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
			audio.write(output.getBuffer(), 0, output.getBufferLength());
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
}
