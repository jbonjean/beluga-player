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
package info.bonjean.beluga.gui.pivot;

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.log.Log;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.slf4j.Logger;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class SongUI extends TablePane implements Bindable
{
	@Log
	private static Logger log;

	private final BelugaState state = BelugaState.getInstance();
	private final PandoraClient pandoraClient = PandoraClient.getInstance();

	@BXML
	Label songTitle;
	@BXML
	Label albumTitle;
	@BXML
	Label artistName;
	@BXML
	Label songTraits;
	@BXML
	ImageView albumCover;
	@BXML
	PushButton likeButton;
	@BXML
	PushButton banButton;
	@BXML
	PushButton sleepButton;

	boolean likeButtonEnabled = true;

	public SongUI()
	{
		Action.getNamedActions().put("like", new AsyncAction(MainWindow.getInstance())
		{
			@Override
			public void asyncPerform(final Component source)
			{
				try
				{
					pandoraClient.addFeedback(state.getSong(), true);
					likeButtonEnabled = false;
					log.info("feedbackSent");
				}
				catch (BelugaException e)
				{
					log.error(e.getMessage(), e);
				}
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						// load(newPage);
					}
				}, false);
			}
		});
		Action.getNamedActions().put("ban", new AsyncAction(MainWindow.getInstance())
		{
			@Override
			public void asyncPerform(final Component source)
			{
				try
				{
					pandoraClient.addFeedback(state.getSong(), false);
					log.info("feedbackSent");
					MainWindow.getInstance().stopPlayer();
				}
				catch (BelugaException e)
				{
					log.error(e.getMessage(), e);
				}
			}
		});
		Action.getNamedActions().put("sleep", new AsyncAction(MainWindow.getInstance())
		{
			@Override
			public void asyncPerform(final Component source)
			{
				try
				{
					pandoraClient.sleepSong(state.getSong());
					log.info("feedbackSent");
					MainWindow.getInstance().stopPlayer();
				}
				catch (BelugaException e)
				{
					log.error(e.getMessage(), e);
				}
			}
		});
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		songTitle.setText(state.getSong().getSongName());
		albumTitle.setText(state.getSong().getAlbumName());
		artistName.setText("by " + state.getSong().getArtistName());

		StringBuffer focusTraits = new StringBuffer();
		for (String focusTrait : state.getSong().getFocusTraits())
		{
			if (focusTraits.length() > 0)
				focusTraits.append(", ");
			focusTraits.append(focusTrait);
		}
		if (focusTraits.length() > 0)
		{
			// capitalize first letter
			focusTraits.setCharAt(0, Character.toUpperCase(focusTraits.charAt(0)));
			// finish with a period
			focusTraits.append(".");
		}
		songTraits.setText(focusTraits.toString());
		try
		{
			URL imageURL = state.getSong().getAlbumArtUrl().isEmpty() ? SongUI.class.getResource("/img/beluga.200x200.png") : new URL(state.getSong()
					.getAlbumArtUrl());
			albumCover.setImage(imageURL);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}

		likeButtonEnabled = state.getSong().getSongRating() > 0 ? false : true;
		if (!likeButtonEnabled)
		{
			likeButton.getAction().setEnabled(false);
			likeButton.setEnabled(false);
		}
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		banButton.getAction().setEnabled(enabled);
		sleepButton.getAction().setEnabled(enabled);

		banButton.setEnabled(enabled);
		sleepButton.setEnabled(enabled);

		if (likeButtonEnabled)
		{
			likeButton.getAction().setEnabled(enabled);
			likeButton.setEnabled(enabled);
		}
	}
}