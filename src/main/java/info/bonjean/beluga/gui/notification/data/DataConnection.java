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
package info.bonjean.beluga.gui.notification.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.bind.DatatypeConverter;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 * Code from: http://stackoverflow.com/questions/9388264/jeditorpane-with-inline-image
 * 
 */
public class DataConnection extends URLConnection {

    public DataConnection(URL u) {
        super(u);
    }

    @Override
    public void connect() throws IOException {
        connected = true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        String data = url.toString();
        data = data.replaceFirst("^.*;base64,", "");
        byte[] bytes = DatatypeConverter.parseBase64Binary(data);
        return new ByteArrayInputStream(bytes);
    }

}
