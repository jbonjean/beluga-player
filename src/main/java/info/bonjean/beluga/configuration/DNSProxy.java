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
package info.bonjean.beluga.configuration;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public enum DNSProxy
{
	PROXY_DNS("proxy-dns", "Proxy DNS", "74.207.242.213", "50.116.28.138"), PORTALLER("portaller",
			"Portaller", "107.170.15.247", "77.88.8.8");
	private final String id;
	private final String name;
	private final String primaryServer;
	private final String secondaryServer;
	private static final Map<String, DNSProxy> lookup = new HashMap<String, DNSProxy>();

	static
	{
		for (DNSProxy s : EnumSet.allOf(DNSProxy.class))
			lookup.put(s.getId(), s);
	}

	public static DNSProxy get(String id)
	{
		return lookup.get(id);
	}

	private DNSProxy(String id, String name, String primaryServer, String secondaryServer)
	{
		this.id = id;
		this.name = name;
		this.primaryServer = primaryServer;
		this.secondaryServer = secondaryServer;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getPrimaryServer()
	{
		return primaryServer;
	}

	public String getSecondaryServer()
	{
		return secondaryServer;
	}

	public String toString()
	{
		return name;
	}
}
