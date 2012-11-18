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
package info.bonjean.beluga.util;

import info.bonjean.beluga.exception.CryptoException;
import info.bonjean.beluga.log.Logger;

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
	private static Logger log = new Logger(CryptoUtil.class);

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
		} catch (Exception e)
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
		} catch (Exception e)
		{
			throw new CryptoException();
		}
	}
}
