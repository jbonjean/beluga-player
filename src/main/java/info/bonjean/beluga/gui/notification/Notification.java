/*
 * Copyright (C) 2012-2016 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.gui.notification;

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.util.ResourcesUtil;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.swingfx.twinkle.NotificationBuilder;
import ch.swingfx.twinkle.style.INotificationStyle;
import ch.swingfx.twinkle.style.theme.DarkDefaultNotification;
import ch.swingfx.twinkle.style.theme.LightDefaultNotification;
import ch.swingfx.twinkle.window.Positions;

import com.kitfox.svg.app.beans.SVGIcon;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class Notification
{
	private static Logger log = LoggerFactory.getLogger(Notification.class);

	public final static int TIMEOUT = 3000;

	public Notification(Song song)
	{
		Icon icon = null;

		if (!song.getAlbumArtUrl().isEmpty())
		{
			try
			{
				BufferedImage img = ImageIO.read(new URL(song.getAlbumArtUrl()));
				icon = new ImageIcon(img.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
			}
			catch (IOException e)
			{
				// nothing to do, fallback to the default icon
				log.debug(e.getMessage());
			}
		}

		if (icon == null)
		{
			try
			{
				SVGIcon svgIcon = ResourcesUtil.getSVGIcon("/img/beluga-player.svg");
				svgIcon.setPreferredSize(new Dimension(80, 80));
				svgIcon.setScaleToFit(true);
				icon = svgIcon;
			}
			catch (IOException e)
			{
				log.error(e.getMessage());
			}
		}

		INotificationStyle style = BelugaConfiguration.getInstance().getNotificationsStyle()
				.equals("dark") ? new DarkDefaultNotification() : new LightDefaultNotification();

		new NotificationBuilder().withStyle(style)
				.withTitle(ResourcesUtil.shorten(song.getSongName(), 60))
				.withMessage(song.getArtistName()).withIcon(icon).withDisplayTime(TIMEOUT)
				.withPosition(Positions.NORTH_EAST).showNotification();
	}
}
