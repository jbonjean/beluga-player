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

import info.bonjean.beluga.gui.pivot.UIPools;
import info.bonjean.beluga.log.Log;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

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

	public CachedInputStream(final InputStream input)
	{
		super(null);
		circularByteBuffer = new CircularByteBuffer(CACHE_SIZE, true);
		super.in = circularByteBuffer.getInputStream();

		UIPools.streamPool.execute(new Runnable()
		{
			public void run()
			{
				// For network the optimal buffer size can be 2 KB to 8 KB (The underlying packet size is typically up to ~1.5 KB)
				byte[] buffer = new byte[8192];
				try
				{
					int length;
					while ((length = input.read(buffer)) != -1)
					{
						circularByteBuffer.getOutputStream().write(buffer, 0, length);
						read += length;
					}
					input.close();
					circularByteBuffer.getOutputStream().close();
					log.debug("Cached stream succesfully closed");
				}
				catch (IOException e)
				{
					log.error(e.getMessage(), e);
				}
			}
		});
	}

	public int getPosition()
	{
		return read;
	}
}
