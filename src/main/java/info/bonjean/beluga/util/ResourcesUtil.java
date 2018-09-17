/*
 * Copyright (C) 2012-2018 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.util;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGIcon;
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.gui.notification.Notification;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.pivot.wtk.media.Drawing;
import org.apache.pivot.wtk.media.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class ResourcesUtil {
	private static Logger log = LoggerFactory.getLogger(ResourcesUtil.class);
	private static final String SVG_NAME_PREFIX = "beluga_player_";

	public static String shorten(String str, int length) {
		if (str.length() > length)
			return str.substring(0, length - 3) + "...";
		return str;
	}

	public static SVGIcon getSVGIcon(String resource) throws IOException {
		SVGUniverse universe = SVGCache.getSVGUniverse();
		URI uri = universe.loadSVG(Notification.class.getResourceAsStream(resource),
				SVG_NAME_PREFIX + FilenameUtils.getBaseName(resource));
		SVGIcon svgIcon = new SVGIcon();
		svgIcon.setSvgURI(uri);
		return svgIcon;
	}

	public static Image getSVGImage(String resource) throws IOException {
		SVGUniverse universe = SVGCache.getSVGUniverse();
		URI uri = universe.loadSVG(Notification.class.getResourceAsStream(resource),
				SVG_NAME_PREFIX + FilenameUtils.getBaseName(resource));
		return new Drawing(universe.getDiagram(uri, true));
	}

	public static Image getPNGImage(String resource) throws IOException {
		return Image.loadFromCache(Notification.class.getResource(resource));
	}

	private static byte[] getResourceAsByteArray(String resource) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream bais;
		try {
			bais = ResourcesUtil.class.getResourceAsStream(resource);
			int c;
			while ((c = bais.read()) != -1) {
				baos.write(c);
			}
			bais.close();
			baos.close();
		} catch (Exception e) {
			log.error("Cannot load resource " + resource);
			System.exit(-1);
		}
		return baos.toByteArray();
	}

	public static String getResourceAsString(String resource) {
		return new String(getResourceAsByteArray(resource));
	}

	public static String getResourceBase64(String resource) {
		return Base64.encodeBase64String(getResourceAsByteArray(resource));
	}

	public static String getRemoteResourceBase64(String url) throws CommunicationException {
		return Base64.encodeBase64String(HTTPUtil.request(url));
	}
}
