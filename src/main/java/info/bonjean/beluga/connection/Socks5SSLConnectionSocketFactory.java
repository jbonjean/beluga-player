/*
 * Copyright (C) 2012-2020 Julien Bonjean <julien@bonjean.info>
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.protocol.HttpContext;

@SuppressWarnings("deprecation")
public class Socks5SSLConnectionSocketFactory extends SSLConnectionSocketFactory {
	private final Proxy socks5Proxy;

	public Socks5SSLConnectionSocketFactory(String socks5Host, int socks5Port) {
		super(SSLContexts.createSystemDefault());
		socks5Proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socks5Host, socks5Port));
	}

	@Override
	public Socket createSocket(final HttpContext context) throws IOException {
		return new Socket(socks5Proxy);
	}

	@Override
	public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
			InetSocketAddress localAddress, HttpContext context) throws IOException {
		InetSocketAddress unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(),
				remoteAddress.getPort());
		return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
	}
}
