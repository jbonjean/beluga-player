/* Copyright (C) 2012, 2013 Julien Bonjean <julien@bonjean.info>
 * 
 * This file is part of Beluga Player.
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
package info.bonjean.beluga.configuration;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

/**
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class CustomAction
{
	public enum Type
	{
		URL("url", "url"), COMMAND("cmd", "command");

		private final String id;
		private final String key;
		private static final Map<String, Type> lookup = new HashMap<String, Type>();

		static
		{
			for (Type s : EnumSet.allOf(Type.class))
				lookup.put(s.getId(), s);
		}

		public static Type get(String id)
		{
			return lookup.get(id);
		}

		private Type(String id, String key)
		{
			this.id = id;
			this.key = key;
		}

		public String getId()
		{
			return id;
		}

		public String getKey()
		{
			return key;
		}
	}

	private Type type = Type.URL;
	private String name = "";
	private String action = "";

	public CustomAction(String serialized)
	{
		String[] customActionParts = serialized.split(":", 3);
		if (customActionParts.length == 3)
		{
			type = Type.get(customActionParts[0]);
			name = new String(Base64.decodeBase64(customActionParts[1]));
			action = new String(Base64.decodeBase64(customActionParts[2]));
		}
	}

	public CustomAction(Type type, String name, String action)
	{
		this.type = type;
		this.name = name;
		this.action = action;
	}

	public boolean isValid()
	{
		if (type == null || name == null || action == null)
			return false;

		if (name.isEmpty() || action.isEmpty())
			return false;

		return true;
	}

	public String serialize()
	{
		if (!isValid())
			return "";

		StringBuffer serialized = new StringBuffer();

		// use base64, easy way to escape strings
		serialized.append(type.getId());
		serialized.append(":");
		serialized.append(Base64.encodeBase64String(name.getBytes()));
		serialized.append(":");
		serialized.append(Base64.encodeBase64String(action.getBytes()));

		return serialized.toString();
	}

	public Type getType()
	{
		return type;
	}

	public void setType(Type type)
	{
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}
}
