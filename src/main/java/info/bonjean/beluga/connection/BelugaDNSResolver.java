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
package info.bonjean.beluga.connection;

import info.bonjean.beluga.configuration.DNSProxy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.DnsResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Options;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 * Custom DNS resolver
 * 
 */
public class BelugaDNSResolver implements DnsResolver
{
	private static final Logger log = LoggerFactory.getLogger(BelugaDNSResolver.class);
	private static final int MAX_RETRIES = 1;
	private static final int TIMEOUT_SECONDS = 2;
	private ExtendedResolver resolver;
	private List<String> backlistedAddresses = new ArrayList<String>();

	public BelugaDNSResolver(DNSProxy dnsProxy)
	{
		if ("debug".equals(System.getProperty("log.level")))
			Options.set("verbose", "true");
		try
		{
			resolver = new ExtendedResolver(new String[] { dnsProxy.getPrimaryServer(),
					dnsProxy.getSecondaryServer() });
			resolver.setTimeout(TIMEOUT_SECONDS);
			resolver.setRetries(MAX_RETRIES);
		}
		catch (UnknownHostException e)
		{
			// XXX
			// throw new InternalException(e);
		}
	}

	@Override
	public InetAddress[] resolve(String host) throws UnknownHostException
	{
		try
		{
			Lookup dnsProxyLookup = new Lookup(host, Type.A);
			dnsProxyLookup.setResolver(resolver);

			List<InetAddress> addresses = new ArrayList<InetAddress>();

			Record[] records = dnsProxyLookup.run();

			if (records != null)
			{
				for (Record record : records)
				{
					if (record instanceof ARecord)
					{
						InetAddress address = ((ARecord) record).getAddress();
						if (!backlistedAddresses.contains(address.getHostAddress()))
							addresses.add(address);
					}
				}
			}
			if (addresses.isEmpty())
				throw new UnknownHostException("dnsProxyError");

			log.debug("Resolved Pandora address using DNS proxy");

			return addresses.toArray(new InetAddress[addresses.size()]);
		}
		catch (TextParseException e)
		{
			throw new UnknownHostException("dns.proxy.error");
		}
	}

	public void blacklistAddress(InetAddress address)
	{
		String strAddress = address.getHostAddress();
		log.debug("Add " + strAddress + " to blacklist");
		backlistedAddresses.add(strAddress);
	}
}
