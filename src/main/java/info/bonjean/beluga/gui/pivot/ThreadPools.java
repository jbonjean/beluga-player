/*
 * Copyright (C) 2012-2016 Julien Bonjean <julien@bonjean.info>
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class ThreadPools
{
	public static final int INTERNAL_BUS_POOL_SIZE = 2;

	public static ExecutorService playbackPool = Executors.newFixedThreadPool(1);
	public static ExecutorService actionPool = Executors.newFixedThreadPool(1);
	public static ExecutorService streamPool = Executors.newFixedThreadPool(1);
	public static ExecutorService internalBusPool = Executors
			.newFixedThreadPool(INTERNAL_BUS_POOL_SIZE);
	public static ScheduledExecutorService statusBarScheduler = new ScheduledThreadPoolExecutor(1);
	public static ScheduledExecutorService playerUIScheduler = new ScheduledThreadPoolExecutor(1);
}
