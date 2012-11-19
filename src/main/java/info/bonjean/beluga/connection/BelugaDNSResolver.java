/* Copyright (C) 2012 Julien Bonjean <julien@bonjean.info>
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 *         Hybrid DNS resolver
 * 
 */
public class BelugaDNSResolver implements DnsResolver
{
	private Map<String, InetAddress[]> dnsMap;
	private DnsResolver fallbackDNSResolver;

	public BelugaDNSResolver()
	{
		dnsMap = new ConcurrentHashMap<String, InetAddress[]>();
		fallbackDNSResolver = new SystemDefaultDnsResolver();
	}

	public void add(final String host, final InetAddress... ips)
	{
		if (host == null)
			throw new IllegalArgumentException("Host name may not be null");
		if (ips == null)
			throw new IllegalArgumentException("Array of IP addresses may not be null");
		dnsMap.put(host, ips);
	}

	@Override
	public InetAddress[] resolve(String host) throws UnknownHostException
	{
		InetAddress[] resolvedAddresses = dnsMap.get(host);
		if (resolvedAddresses == null)
			return fallbackDNSResolver.resolve(host);
		return resolvedAddresses;
	}
}
