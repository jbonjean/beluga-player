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

import info.bonjean.beluga.gui.UI;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 *         Hybrid DNS resolver
 * 
 */
public class BelugaDNSResolver implements DnsResolver
{
	private static final Logger log = LoggerFactory.getLogger(BelugaDNSResolver.class);
	private static final int DNS_PROXY_MAX_RETRY = 2;
	private static final int DNS_TIMEOUT_SECONDS = 5;
	private Lookup dnsProxyLookup;
	private DnsResolver fallbackDNSResolver;
	private String pandoraURL;

	public BelugaDNSResolver(String pandoraURL, String proxyDNS)
	{
		fallbackDNSResolver = new SystemDefaultDnsResolver();
		this.pandoraURL = pandoraURL;

		try
		{
			SimpleResolver resolver = new SimpleResolver(proxyDNS);
			resolver.setTimeout(DNS_TIMEOUT_SECONDS);
			dnsProxyLookup = new Lookup(pandoraURL, Type.A);
			dnsProxyLookup.setResolver(resolver);
		}
		catch (Exception e)
		{
			UI.reportError("cannot.configure.proxy");
			dnsProxyLookup = null;
		}
	}

	@Override
	public InetAddress[] resolve(String host) throws UnknownHostException
	{
		if (dnsProxyLookup != null && pandoraURL.equalsIgnoreCase(host))
		{
			List<InetAddress> addresses = new ArrayList<InetAddress>();

			for (int i = 0; i < DNS_PROXY_MAX_RETRY; i++)
			{
				Record[] records = dnsProxyLookup.run();

				if (records != null)
				{
					for (Record record : records)
					{
						if (record instanceof ARecord)
							addresses.add(((ARecord) record).getAddress());
					}
				}
				if (!addresses.isEmpty())
					break;
				else
					log.warn("DNS proxy problem");
			}
			if (addresses.isEmpty())
			{
				log.error("Cannot resolve pandora address using DNS proxy");
				throw new UnknownHostException("dns.proxy.error");
			}
			log.debug("Resolved Pandora address using DNS proxy");
			return addresses.toArray(new InetAddress[addresses.size()]);
		}
		return fallbackDNSResolver.resolve(host);
	}
}
