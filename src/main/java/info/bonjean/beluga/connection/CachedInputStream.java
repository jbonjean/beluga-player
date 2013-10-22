/* Copyright (C) 2012, 2013 Julien Bonjean <julien@bonjean.info>
 * 
 * This file is part of Beluga Player.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.bonjean.beluga.connection;

import info.bonjean.beluga.gui.pivot.ThreadPools;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class CachedInputStream extends FilterInputStream
{
	private static Logger log = LoggerFactory.getLogger(CachedInputStream.class);

	private static final int CACHE_SIZE = 512 * 1024;
	private static final int INITIAL_CACHE_SIZE = 100 * 1024;
	private PipedOutputStream pipe;
	// private InputStream source;
	private Future<?> future;

	// public CachedInputStream(final InputStream in)
	public CachedInputStream(final HttpEntity entity)
	{
		super(new PipedInputStream(CACHE_SIZE));
		// source = in;
		// super.in = input;

		future = ThreadPools.streamPool.submit(new Runnable()
		{
			public void run()
			{
				try
				{
//					// For network the optimal buffer size can be 2 KB to 8 KB (The underlying packet size is typically up to ~1.5 KB)
//					byte[] buffer = new byte[8192];
//					int length;
//
//					// create the pipe, connect output (producer) to input stream (consumer)
//					pipe = new PipedOutputStream(input);
//
//					while (true)
//					{
//						length = in.read(buffer);
//						if (length == -1)
//							throw new IOException("End of stream");
//						pipe.write(buffer, 0, length);
//					}
					// create the pipe, connect output (producer) to input stream (consumer)
					pipe = new PipedOutputStream((PipedInputStream) in);
					System.out.println("pipe created");

					// feed stream to the pipe
					entity.writeTo(pipe);
					System.out.println("playback finished");
				}
				catch (IOException e1)
				{
					System.out.println(e1.getMessage());
				}
				if (pipe != null)
				{
					try
					{
						// close the original input stream
						// source.close();

						// no more data will be send, flush
						pipe.flush();
						System.out.println("pipe flushed");

						// close the producer, break the pipe
						pipe.close();
						System.out.println("pipe closed");
					}
					catch (IOException e2)
					{
						System.out.println(e2.getMessage());
					}
				}
				System.out.println("end of producer thread");
			}
		});

		// wait for enough cache
		try
		{
			while (in.available() < INITIAL_CACHE_SIZE)
			{
				System.out.println("caching stream (" + in.available() + "/" + INITIAL_CACHE_SIZE + ")");
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void close()
	{
		try
		{
			// close the original input stream
			// source.close();

			// break the pipe by closing the consumer
			in.close();
			System.out.println("pipe closed");

			// block until thread is finished
			future.get();
			System.out.println("producer thread ended");
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			log.error(e.getMessage(), e);
		}
	}
}
