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
package info.bonjean.beluga.connection;

import info.bonjean.beluga.gui.pivot.ThreadPools;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class CachedInputStream extends FilterInputStream
{
	private static Logger log = LoggerFactory.getLogger(CachedInputStream.class);

	private static final int OUTPUT_CACHE_SIZE = 512 * 1024;
	private static final int INITIAL_CACHE_SIZE = 100 * 1024;
	private static final byte[] buffer = new byte[8192];
	private PipedOutputStream pipe;
	private Future<?> future;
	private boolean closed = false;

	public CachedInputStream(final InputStream inputstream)
	{
		super(new PipedInputStream(OUTPUT_CACHE_SIZE));

		future = ThreadPools.streamPool.submit(new Runnable()
		{
			public void run()
			{
				try
				{
					// create the pipe, connect output (producer) to input
					// stream (consumer)
					pipe = new PipedOutputStream((PipedInputStream) in);
					log.debug("producer: pipe created");

					// feed stream to the pipe we do it manually because the
					// method writeTo from HttpEntity calls the close method of
					// ChunkedInputStream that do some work to prepare for the
					// next response but this can be slow and we don't really
					// need it.
					int length;
					while ((length = inputstream.read(buffer)) != -1)
						pipe.write(buffer, 0, length);

					log.debug("producer: stream finished");
				}
				catch (IOException e)
				{
					log.debug(e.getMessage());
				}
				if (pipe != null)
				{
					try
					{
						// no more data will be send, flush
						pipe.flush();
						log.debug("producer: pipe flushed");

						// close the producer, break the pipe
						pipe.close();
						log.debug("producer: pipe closed");
					}
					catch (IOException e)
					{
						log.debug(e.getMessage());
					}
				}
				log.debug("producer: end of thread");
				closed = true;
			}
		});

		// wait for enough cache
		try
		{
			while (in.available() < INITIAL_CACHE_SIZE && !closed)
			{
				log.debug("caching stream (" + in.available() + "/" + INITIAL_CACHE_SIZE + ")");
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					log.debug(e.getMessage());
				}
			}
		}
		catch (IOException e)
		{
			log.debug(e.getMessage());
		}
	}

	@Override
	public void close()
	{
		try
		{
			// break the pipe by closing the consumer
			in.close();
			log.debug("consumer: pipe closed");

			// block until thread is finished
			future.get();
			log.debug("consumer: producer thread ended");
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
		}
	}
}
