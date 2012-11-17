/* Copyright (C) 2012 Julien Bonjean <julien@bonjean.info>
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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
package info.bonjean.beluga.exception;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public enum PandoraError
{
	INTERNAL(0), MAINTENANCE_MODE(1), URL_PARAM_MISSING_METHOD(2), URL_PARAM_MISSING_AUTH_TOKEN(3), URL_PARAM_MISSING_PARTNER_ID(4), URL_PARAM_MISSING_USER_ID(5), SECURE_PROTOCOL_REQUIRED(6), CERTIFICATE_REQUIRED(
			7), PARAMETER_TYPE_MISMATCH(8), PARAMETER_MISSING(9), PARAMETER_VALUE_INVALID(10), API_VERSION_NOT_SUPPORTED(11), LICENSING_RESTRICTIONS(12), INSUFFICIENT_CONNECTIVITY(13), UNKNOWN_1(14), UNKNOWN_2(
			15), READ_ONLY_MODE(1000), INVALID_AUTH_TOKEN(1001), UNKNOWN(1002);

	private static final Map<Long, PandoraError> lookup = new HashMap<Long, PandoraError>();

	static
	{
		for (PandoraError s : EnumSet.allOf(PandoraError.class))
			lookup.put(s.getCode(), s);
	}

	private final long code;

	private PandoraError(long code)
	{
		this.code = code;
	}

	public long getCode()
	{
		return code;
	}

	public static PandoraError get(long code)
	{
		return lookup.get(code);
	}
}
