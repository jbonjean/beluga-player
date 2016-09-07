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
package info.bonjean.beluga.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 *
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class StringUtil
{
	private static String sanitizeVersion(String version)
	{
		String sanitized = StringUtils.strip(version, " \n\r");
		return StringUtils.isBlank(sanitized) ? null : sanitized;
	}

	/**
	 * Returns a negative integer, zero, or a positive integer as this version1
	 * is less than, equal to, or greater than version2.
	 */
	public static int compareVersions(String version1, String version2)
	{
		DefaultArtifactVersion v1 = new DefaultArtifactVersion(sanitizeVersion(version1));
		DefaultArtifactVersion v2 = new DefaultArtifactVersion(sanitizeVersion(version2));
		return v1.compareTo(v2);
	}
}
