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
package info.bonjean.beluga.response;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class Audio
{
	private String audioUrl;
	private String bitrate;
	private String encoding;
	private String protocol;

	public String getAudioUrl()
	{
		return audioUrl;
	}

	public void setAudioUrl(String audioUrl)
	{
		this.audioUrl = audioUrl;
	}

	public String getBitrate()
	{
		return bitrate;
	}

	public void setBitrate(String bitrate)
	{
		this.bitrate = bitrate;
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	public String getProtocol()
	{
		return protocol;
	}

	public void setProtocol(String protocol)
	{
		this.protocol = protocol;
	}
}
