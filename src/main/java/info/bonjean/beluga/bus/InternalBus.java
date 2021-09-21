/*
 * Copyright (C) 2012-2021 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.bus;

import info.bonjean.beluga.gui.pivot.ThreadPools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalBus {
	private static Logger log = LoggerFactory.getLogger(InternalBus.class);
	private static final BlockingQueue<PlaybackEvent> queue = new ArrayBlockingQueue<PlaybackEvent>(10);
	private static final List<InternalBusSubscriber> subscribers = new ArrayList<InternalBusSubscriber>();

	public static void publish(PlaybackEvent event) {
		queue.add(event);
	}

	public static void subscribe(InternalBusSubscriber subscriber) {
		subscribers.add(subscriber);
	}

	public static void start() {
		for (int i = 0; i < ThreadPools.INTERNAL_BUS_POOL_SIZE; i++)
			ThreadPools.internalBusPool.submit(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							PlaybackEvent event = queue.take();
							log.debug("received event {}", event.getType().name());
							for (InternalBusSubscriber subscriber : subscribers)
								subscriber.receive(event);
						} catch (InterruptedException e) {
							log.debug("thread interrupted");
						}
					}
				}
			});
	}
}
