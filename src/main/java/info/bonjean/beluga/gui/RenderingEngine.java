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
package info.bonjean.beluga.gui;

import info.bonjean.beluga.exception.InternalException;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class RenderingEngine
{
	private static RenderingEngine instance;
	private VelocityEngine engine;

	private RenderingEngine()
	{
		engine = new VelocityEngine();
		engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		engine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "true");
		engine.setProperty(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
		engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, true);
		engine.init();
	}

	public static RenderingEngine getInstance()
	{
		if (instance == null)
			instance = new RenderingEngine();
		return instance;
	}

	public String render(VelocityContext context, String templateFile) throws InternalException
	{
		try
		{
			Template template = engine.getTemplate(templateFile, "UTF-8");
			StringWriter writer = new StringWriter();
			template.merge(context, writer);
			return writer.toString();
		}
		catch(Exception e)
		{
			throw new InternalException(e);
		}
	}
}
