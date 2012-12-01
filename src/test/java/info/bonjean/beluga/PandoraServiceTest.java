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
package info.bonjean.beluga;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.gui.Page;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.response.Station;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
@RunWith(OrderedRunner.class)
public class PandoraServiceTest
{
	private PandoraClient pandoraService = PandoraClient.getInstance();
	private BelugaState state = BelugaState.getInstance();

	@Test
	public void _1_testLogin() throws Exception
	{
		pandoraService.partnerLogin();
		pandoraService.userLogin();

		assertNotNull(state.getPartnerAuthToken());
		assertNotNull(state.getPartnerId());
		assertNotNull(state.getUserAuthToken());
		assertNotNull(state.getUserId());
	}

	@Test
	public void _2_getStationList() throws Exception
	{
		pandoraService.updateStationList();

		assertNotNull(state.getStationList());
		assertFalse(state.getStationList().isEmpty());
	}

	@Test
	public void _3_testGetPlaylist() throws Exception
	{
		List<Station> stations = state.getStationList();
		pandoraService.selectStation(stations.get(0));

		assertNotNull(state.getPlaylist());
		assertFalse(state.getPlaylist().isEmpty());
	}

	@Test
	public void _4_testNextSong() throws Exception
	{
		assertNull(state.getSong());

		pandoraService.nextSong();

		assertNotNull(state.getSong());
	}
	
	public void setup() {
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

		//ui.updatePage(Page.STATION_ADD, null);
	}
}
