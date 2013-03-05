package info.bonjean.beluga;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;

public class Player
{
	private Bitstream bitstream;
	private Decoder decoder;
	private AudioDevice audio;
	private boolean closed = false;
	private boolean complete = false;
	private int lastPosition = 0;
	private final long duration;

	public Player(String url) throws JavaLayerException, MalformedURLException, IOException
	{
		URLConnection songURLConnection = new URL(url).openConnection();
		InputStream inputStream = songURLConnection.getInputStream();
		
		bitstream = new Bitstream(inputStream);
		decoder = new Decoder();

		FactoryRegistry r = FactoryRegistry.systemRegistry();
		audio = r.createAudioDevice();

		audio.open(decoder);
		
		// get the first frame header to get bitrate
		Header header = bitstream.readFrame();
		bitstream.unreadFrame();
		
		// get file size from HTTP headers
		long songSize = Long.parseLong(songURLConnection.getHeaderField("Content-Length"));

		// calculate the duration
		duration = (long) header.total_ms((int) songSize);
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
			catch (BitstreamException ex)
			{
			}
		}
	}

	public synchronized boolean isComplete()
	{
		return complete;
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

			Header h = bitstream.readFrame();

			if (h == null)
				return false;

			SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h, bitstream);

			synchronized (this)
			{
				out = audio;
				if (out != null)
				{
					out.write(output.getBuffer(), 0, output.getBufferLength());
				}
			}

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
}
