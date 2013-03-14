/* Copyright (C) 2012, 2013 Julien Bonjean <julien@bonjean.info>
 * 
 * This file is part of Beluga.
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
import info.bonjean.beluga.log.Log;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

import org.slf4j.Logger;

import com.Ostermiller.util.CircularByteBuffer;

/**
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class CachedInputStream extends FilterInputStream
{
	@Log
	private static Logger log;

	private static final int CACHE_SIZE = 512 * 1024;
	private CircularByteBuffer circularByteBuffer;
	private int read;
	private InputStream input;
	private Future<?> future;

	public CachedInputStream(InputStream in)
	{
		super(null);
		this.input = in;
		circularByteBuffer = new CircularByteBuffer(CACHE_SIZE, true);
		super.in = circularByteBuffer.getInputStream();

		future = ThreadPools.streamPool.submit(new Runnable()
		{
			public void run()
			{
				// For network the optimal buffer size can be 2 KB to 8 KB (The underlying packet size is typically up to ~1.5 KB)
				byte[] buffer = new byte[8192];
				int length;
				while (true)
				{
					try
					{
						length = input.read(buffer);
						if (length == -1)
							throw new IOException("End of stream");
					}
					catch (IOException e)
					{
						// no more data to read
						try
						{
							log.debug(e.getMessage());
							circularByteBuffer.getOutputStream().close();
						}
						catch (IOException e1)
						{
							// never happen (circularByteBuffer inputstream close method never throw exception)
						}
						return;
					}
					try
					{
						circularByteBuffer.getOutputStream().write(buffer, 0, length);
					}
					catch (IOException e)
					{
						log.debug("InputStream has been closed by the reader");
						// reader has closed circularByteBuffer inputstream
						return;
					}
					read += length;
				}
			}
		});
	}

	@Override
	public void close()
	{
		// close circularByteBuffer outputstream, will not be feed anymore
		try
		{
			circularByteBuffer.getOutputStream().close();
		}
		catch (IOException e1)
		{
			// never happen
		}

		// block until thread is finished
		try
		{
			future.get();
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
		}
	}

	public int getPosition()
	{
		return read;
	}
}
