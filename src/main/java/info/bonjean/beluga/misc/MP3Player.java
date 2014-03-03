/*
 * Copyright (C) 2012, 2013, 2014 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.misc;

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.exception.CommunicationException;

import java.io.IOException;
import java.net.MalformedURLException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.BelugaMP3Player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class MP3Player
{
	private static Logger log = LoggerFactory.getLogger(MP3Player.class);
	private static BelugaConfiguration configuration = BelugaConfiguration.getInstance();

	public static void main(String[] args) throws MalformedURLException, JavaLayerException, IOException, CommunicationException
	{
		configuration.load();
		// BelugaMP3Player mp3Player = new
		// BelugaMP3Player("http://www.soundjay.com/button/beep-5.mp3");
		BelugaMP3Player mp3Player = new BelugaMP3Player("http://www.hubharp.com/web_sound/WalloonLilliShort.mp3");
		// BelugaMP3Player mp3Player = new
		// BelugaMP3Player("http://robtowns.com/music/blind_willie.mp3");
		mp3Player.play();
		log.info("Playback finished");
	}

}
