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

import info.bonjean.beluga.exception.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aregner.pandora.Blowfish;
import com.aregner.pandora.PandoraKeys;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class CryptoUtil
{
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(CryptoUtil.class);

	private static final Blowfish blowfish_decode = new Blowfish(PandoraKeys.in_key_p, PandoraKeys.in_key_s);
	private static final Blowfish blowfish_encode = new Blowfish(PandoraKeys.out_key_p, PandoraKeys.out_key_s);

	private static String fromHex(String hexText)
	{
		String decodedText = null;
		String chunk = null;
		if (hexText != null && hexText.length() > 0)
		{
			int numBytes = hexText.length() / 2;
			char[] rawToByte = new char[numBytes];
			int offset = 0;
			for (int i = 0; i < numBytes; i++)
			{
				chunk = hexText.substring(offset, offset + 2);
				offset += 2;
				rawToByte[i] = (char) (Integer.parseInt(chunk, 16) & 0x000000FF);
			}
			decodedText = new String(rawToByte);
		}
		return decodedText;
	}

	private static String pad(String s, int l)
	{
		String result = s;
		while (l - s.length() > 0)
		{
			result += '\0';
			l--;
		}
		return result;
	}

	public static String pandoraDecrypt(String s) throws CryptoException
	{
		try
		{
			StringBuilder result = new StringBuilder();
			int length = s.length();
			int i16 = 0;
			for (int i = 0; i < length; i += 16)
			{
				i16 = (i + 16 > length) ? (length - 1) : (i + 16);
				result.append(blowfish_decode.decrypt(pad(fromHex(s.substring(i, i16)), 8).toCharArray()));
			}
			return result.toString().trim();
		}
		catch (Exception e)
		{
			throw new CryptoException();
		}
	}

	public static String pandoraEncrypt(String s) throws CryptoException
	{
		try
		{
			int length = s.length();
			StringBuilder result = new StringBuilder(length * 2);
			int i8 = 0;
			for (int i = 0; i < length; i += 8)
			{
				i8 = (i + 8 >= length) ? (length) : (i + 8);
				String substring = s.substring(i, i8);
				String padded = pad(substring, 8);
				long[] blownstring = blowfish_encode.encrypt(padded.toCharArray());
				for (int c = 0; c < blownstring.length; c++)
				{
					if (blownstring[c] < 0x10)
						result.append("0");
					result.append(Integer.toHexString((int) blownstring[c]));
				}
			}
			return result.toString();
		}
		catch (Exception e)
		{
			throw new CryptoException();
		}
	}

	public static String encryptBlowfish(String to_encrypt, String strkey)
	{
		if (to_encrypt == null || to_encrypt.isEmpty())
			return to_encrypt;
		
		try
		{
			SecretKeySpec key = new SecretKeySpec(strkey.getBytes(), "Blowfish");
			Cipher cipher = Cipher.getInstance("Blowfish");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return new String(Base64.encodeBase64(cipher.doFinal(to_encrypt.getBytes())));
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static String decryptBlowfish(String to_decrypt, String strkey)
	{
		if (to_decrypt == null || to_decrypt.isEmpty())
			return to_decrypt;

		try
		{
			SecretKeySpec key = new SecretKeySpec(strkey.getBytes(), "Blowfish");
			Cipher cipher = Cipher.getInstance("Blowfish");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] decrypted = cipher.doFinal(Base64.decodeBase64(to_decrypt));
			return new String(decrypted);
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
