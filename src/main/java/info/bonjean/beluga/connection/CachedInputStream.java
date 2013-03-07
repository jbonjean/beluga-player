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

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.Ostermiller.util.CircularByteBuffer;

/**
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class CachedInputStream extends FilterInputStream
{
	private static final int CACHE_SIZE = 4096;

	public CachedInputStream(final InputStream in)
	{
		super(null);

		final CircularByteBuffer cbb = new CircularByteBuffer(CACHE_SIZE, true);
		
		super.in = cbb.getInputStream();

		new Thread(new Runnable()
		{
			public void run()
			{
				BufferedInputStream bufferedIn = new BufferedInputStream(in);
				byte[] buffer = new byte[1024];
				try
				{
					while (true)
					{
						int read = bufferedIn.read(buffer);
						cbb.getOutputStream().write(buffer, 0, read);
						System.out.println("cached: " + (CACHE_SIZE - cbb.getSpaceLeft()));
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}
}
