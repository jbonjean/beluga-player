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
package info.bonjean.beluga.exception;

import info.bonjean.beluga.request.Method;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class PandoraException extends BelugaException
{
	private static final long serialVersionUID = -3262873433055296370L;
	private Method method;
	private String message;
	private long code;
	
	public PandoraException(Method method, String message, long code){
		super(message);
		this.method = method;
		this.message = message;
		this.code = code;
	}
	
	@Override
	public String toString() {
		return "Pandora returned an error when calling " + method + ", code: " + code + ", message: " + message;
	}
	
	public PandoraError getError()
	{
		return PandoraError.get(code);
	}
	
	public Method getMethod() {
		return method;
	}
}
