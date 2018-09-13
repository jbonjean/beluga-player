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
	ERROR_CODE_UNKNOWN(-1, "pandoraErrorCodeUnknown"), INTERNAL(0, "pandoraInternal"), MAINTENANCE_MODE(
			1, "pandoraMaintenanceMode"), URL_PARAM_MISSING_METHOD(2,
			"pandoraUrlParamMissingMethod"), URL_PARAM_MISSING_AUTH_TOKEN(3,
			"pandoraUrlParamMissingAuthToken"), URL_PARAM_MISSING_PARTNER_ID(4,
			"pandoraUrlParamMissingPartnerId"), URL_PARAM_MISSING_USER_ID(5,
			"pandoraUrlParamMissingUserId"), SECURE_PROTOCOL_REQUIRED(6,
			"pandoraSecureProtocolRequired"), CERTIFICATE_REQUIRED(7, "pandoraCertificateRequired"), PARAMETER_TYPE_MISMATCH(
			8, "pandoraParameterTypeMismatch"), PARAMETER_MISSING(9, "pandoraParameterMissing"), PARAMETER_VALUE_INVALID(
			10, "pandoraParameterValueInvalid"), API_VERSION_NOT_SUPPORTED(11,
			"pandoraApiVersionNotSupported"), LICENSING_RESTRICTIONS(12,
			"pandoraLicensingRestrictions"), INSUFFICIENT_CONNECTIVITY(13,
			"pandoraInsufficientConnectivity"), UNKNOWN_14(14, "pandoraUnknown14"), UNKNOWN_15(15,
			"pandoraUnknown15"), READ_ONLY_MODE(1000, "pandoraReadOnlyMode"), INVALID_AUTH_TOKEN(
			1001, "pandoraInvalidAuthToken"), INVALID_CREDENTIALS(1002, "pandoraInvalidCredentials"), INVALID_STATION(
			1004, "pandoraInvalidStation"), STATION_DOES_NOT_EXIST(1006,
			"pandoraStationDoesNotExist"), CALL_NOT_ALLOWED(1008, "pandoraCallNotAllowed"), EMAIL_INVALID(
			1011, "pandoraEmailInvalid"), PASSWORD_INVALID(1012, "pandoraPasswordInvalid"), EMAIL_ALREADY_USED(
			1013, "pandoraEmailAlreadyUsed"), INVALID_ZIP(1024, "pandoraInvalidZip"), INVALID_BIRTHYEAR(
			1025, "pandoraInvalidBirthyear"), INVALID_BIRTHYEAR_AGE(1026,
			"pandoraInvalidBirthyearAge"), INVALID_GENDER(1027, "pandoraInvalidGender"), OUT_OF_PLAYS(
			1039, "pandoraOutOfPlays");

	private static final Map<Long, PandoraError> lookup = new HashMap<Long, PandoraError>();

	static
	{
		for (PandoraError s : EnumSet.allOf(PandoraError.class))
			lookup.put(s.getCode(), s);
	}

	private final long code;
	private final String key;

	private PandoraError(long code, String key)
	{
		this.code = code;
		this.key = key;
	}

	public long getCode()
	{
		return code;
	}

	public String getMessageKey()
	{
		return key;
	}

	public static PandoraError get(long code)
	{
		return lookup.get(code);
	}
}
