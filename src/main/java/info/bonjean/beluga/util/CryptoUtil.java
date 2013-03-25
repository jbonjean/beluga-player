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
package info.bonjean.beluga.util;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class CryptoUtil
{
	private static final Logger log = LoggerFactory.getLogger(CryptoUtil.class);

	private static final Map<String, Cipher> encryptCiphers = new HashMap<String, Cipher>();
	private static final Map<String, Cipher> decryptCiphers = new HashMap<String, Cipher>();

	private static final String ENCRYPT_KEY = "6#26FRL$ZWD";
	private static final String DECRYPT_KEY = "R=U!LH$O2B#";

	private static Cipher getCipher(String strKey, boolean encoder)
	{
		Cipher cipher = null;
		if (encoder)
			cipher = encryptCiphers.get(strKey);
		else
			cipher = decryptCiphers.get(strKey);

		if (cipher != null)
			return cipher;

		try
		{
			SecretKeySpec key = new SecretKeySpec(strKey.getBytes(), "Blowfish");
			cipher = Cipher.getInstance("Blowfish/ECB/NoPadding");
			cipher.init(encoder ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, key);

			if (encoder)
				encryptCiphers.put(strKey, cipher);
			else
				decryptCiphers.put(strKey, cipher);
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
		}

		return cipher;
	}

	public static String passwordDecrypt(String text, String strKey)
	{
		return new String(decryptBlowfish(Base64.decodeBase64(text.getBytes()), strKey)).trim();
	}

	public static String passwordEncrypt(String text, String strKey)
	{
		return new String(Base64.encodeBase64(encryptBlowfish(text, strKey)));
	}

	public static String pandoraDecrypt(String text)
	{
		try
		{
			return new String(decryptBlowfish(Hex.decodeHex(text.toCharArray()), DECRYPT_KEY)).trim();
		}
		catch (DecoderException e)
		{
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public static String pandoraEncrypt(String text)
	{
		return new String(Hex.encodeHex(encryptBlowfish(text, ENCRYPT_KEY)));
	}

	public static byte[] encryptBlowfish(String strToEncrypt, String strKey)
	{
		if (strToEncrypt == null || strToEncrypt.isEmpty())
			return new byte[] {};

		byte[] toEncrypt = strToEncrypt.getBytes();

		// padding with null bytes if not a multiple of 8
		if (toEncrypt.length % 8 != 0)
		{
			byte[] padded = new byte[toEncrypt.length + 8 - (toEncrypt.length % 8)];
			System.arraycopy(toEncrypt, 0, padded, 0, toEncrypt.length);
			toEncrypt = padded;
		}

		try
		{
			return getCipher(strKey, true).doFinal(toEncrypt);
		}
		catch (Exception e)
		{
			// TODO: throw the exception, should be handled in higher layers
			log.error(e.getMessage(), e);
			return new byte[] {};
		}
	}

	public static byte[] decryptBlowfish(byte[] strToDecrypt, String strKey)
	{
		if (strToDecrypt == null || strToDecrypt.length == 0)
			return new byte[] {};

		byte[] decrypt = strToDecrypt;// .getBytes();

		try
		{
			return getCipher(strKey, false).doFinal(decrypt);
		}
		catch (Exception e)
		{
			// TODO: throw the exception, should be handled in higher layers
			log.error(e.getMessage(), e);
			return new byte[] {};
		}
	}
}
