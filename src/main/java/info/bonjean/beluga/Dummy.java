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
package info.bonjean.beluga;

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.gui.Page;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.response.Station;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class Dummy
{
	public static void setup() {
		Station station1 = new Station();
		station1.setStationId("1");
		station1.setStationName("QuickMix");
		station1.setQuickMix(true);
		station1.setStationDetailUrl("");
		station1.setStationToken("abc");
		
		Station station2 = new Station();
		station2.setStationId("2");
		station2.setStationName("Station 1");
		station2.setQuickMix(false);
		station2.setStationDetailUrl("");
		station2.setStationToken("cba");
		
		List<Station> stationList = new ArrayList<Station>();
		stationList.add(station1);
		stationList.add(station2);
		
		Song song = new Song();
		song.setAlbumArtBase64("");
		song.setAlbumName("ALBUM NAME");
		song.setArtistBookmarked(true);
		song.setArtistName("ARTIST NAME");
		song.setSongBookmarked(true);
		song.setSongName("SONG NAME");
		song.setSongRating(1);
		song.setStationId("2");
		song.setTrackToken("abc");
		song.setAlbumArtBase64(PandoraClient.retrieveAlbumArt(song));
		song.setFocusTraits(PandoraClient.retrieveFocusTraits(song));
		
		List<Song> playlist = new ArrayList<Song>();
		playlist.add(song);
		
		BelugaState.getInstance().setStationList(stationList);
		BelugaState.getInstance().setStation(station2);
		BelugaState.getInstance().setPlaylist(playlist);
		BelugaState.getInstance().setSong(song);
		BelugaState.getInstance().setPage(Page.SONG);
	}
}
