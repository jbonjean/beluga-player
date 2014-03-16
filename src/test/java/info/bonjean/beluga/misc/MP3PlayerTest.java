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

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.player.MP3Player;

import java.io.IOException;
import java.net.MalformedURLException;

import javazoom.jl.decoder.JavaLayerException;

/**
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class MP3PlayerTest
{
	private static BelugaConfiguration configuration = BelugaConfiguration.getInstance();

	public static void main(String[] args) throws MalformedURLException, JavaLayerException, IOException, CommunicationException
	{
		BelugaState.getInstance().setVersion("dev");
		configuration.load();
		configuration.setDNSProxy("");
		MP3Player mp3Player = new MP3Player("http://www.soundjay.com/button/beep-10.mp3");
		mp3Player.setSilence(true);
		mp3Player.play();
		System.out.println("Playback finished");
	}

}
