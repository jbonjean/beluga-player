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
package info.bonjean.beluga.gui.pivot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class ThreadPools
{
	public static ExecutorService playbackPool = new ThreadPoolExecutor(1, 1, 0L,
			TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
	public static ExecutorService actionPool = new ThreadPoolExecutor(1, 1, 0L,
			TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
	public static ExecutorService streamPool = new ThreadPoolExecutor(1, 1, 0L,
			TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
	public static ScheduledExecutorService statusBarScheduler = new ScheduledThreadPoolExecutor(1);
	public static ScheduledExecutorService playerUIScheduler = new ScheduledThreadPoolExecutor(1);
}
