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
package info.bonjean.beluga.client;

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.gui.Page;
import info.bonjean.beluga.gui.UI;
import info.bonjean.beluga.request.ArtistBookmark;
import info.bonjean.beluga.request.CreateStation;
import info.bonjean.beluga.request.CreateUser;
import info.bonjean.beluga.request.DeleteStation;
import info.bonjean.beluga.request.Feedback;
import info.bonjean.beluga.request.Method;
import info.bonjean.beluga.request.ParameterMap;
import info.bonjean.beluga.request.PartnerAuth;
import info.bonjean.beluga.request.PlayList;
import info.bonjean.beluga.request.Search;
import info.bonjean.beluga.request.SongBookmark;
import info.bonjean.beluga.request.SongSleep;
import info.bonjean.beluga.request.StationList;
import info.bonjean.beluga.request.UserLogin;
import info.bonjean.beluga.response.Result;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.response.Station;
import info.bonjean.beluga.util.HTMLUtil;
import info.bonjean.beluga.util.HTTPUtil;
import info.bonjean.beluga.util.PandoraUtil;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class PandoraClient
{
	private static final Logger log = LoggerFactory.getLogger(PandoraClient.class);
	private static PandoraClient instance;

	private static final BelugaState state = BelugaState.getInstance();
	private static final BelugaConfiguration configuration = BelugaConfiguration.getInstance();

	private PandoraClient()
	{
	}

	public static PandoraClient getInstance()
	{
		if (instance == null)
			instance = new PandoraClient();
		return instance;
	}

	public ParameterMap getDefaultParameterMap()
	{
		ParameterMap params = new ParameterMap();
		params.add("partner_id", state.getPartnerId());
		params.add("auth_token", state.getUserAuthToken());
		params.add("user_id", state.getUserId());

		return params;
	}

	public void partnerLogin() throws BelugaException
	{
		state.setPartnerId(null);
		state.setPartnerAuthToken(null);
		
		Result result = HTTPUtil.request(Method.PARTNER_LOGIN, null, new PartnerAuth(), false);

		state.setPartnerId(result.getPartnerId());
		state.setPartnerAuthToken(result.getPartnerAuthToken());
	}

	public void userLogin() throws BelugaException
	{
		state.setUserId(null);
		state.setUserAuthToken(null);
		
		ParameterMap params = new ParameterMap();
		params.add("partner_id", state.getPartnerId());
		params.add("auth_token", state.getPartnerAuthToken());

		UserLogin userLogin = new UserLogin();
		userLogin.setSyncTime(PandoraUtil.getSyncTime());
		userLogin.setPartnerAuthToken(state.getPartnerAuthToken());
		userLogin.setUsername(configuration.getUserName());
		userLogin.setPassword(configuration.getPassword());

		Result result = HTTPUtil.request(Method.USER_LOGIN, params, userLogin, true);

		state.setUserId(result.getUserId());
		state.setUserAuthToken(result.getUserAuthToken());
	}

	public void updateStationList() throws BelugaException
	{
		UI.reportInfo("retrieving.stations");
		state.setStationList(getStationList());
		if (state.getStation() == null && !state.getStationList().isEmpty())
			selectStation(state.getStationList().get(0));
	}

	private List<Station> getStationList() throws BelugaException
	{
		ParameterMap params = getDefaultParameterMap();

		StationList stationList = new StationList();
		stationList.setSyncTime(PandoraUtil.getSyncTime());
		stationList.setUserAuthToken(state.getUserAuthToken());

		Result result = HTTPUtil.request(Method.GET_STATION_LIST, params, stationList, true);

		state.setStationList(result.getStations());

		log.info("Retrieved " + state.getStationList().size() + " stations");

		return state.getStationList();
	}

	public void selectStation(Station station) throws BelugaException
	{
		log.info("Select station " + station.getStationName());

		state.setStation(station);
	}

	private Station getStationById(String stationId)
	{
		for (Station station : state.getStationList())
		{
			if (station.getStationId().equals(stationId))
				return station;
		}
		return null;
	}

	public void selectStation(String stationId) throws BelugaException
	{
		selectStation(getStationById(stationId));

		// clear the playlist
		state.setPlaylist(null);
	}

	public String nextSong() throws BelugaException
	{
		if (state.getPlaylist() == null || state.getPlaylist().isEmpty())
		{
			// retrieve playlist from Pandora
			UI.reportInfo("retrieving.playlist");
			state.setPlaylist(getPlaylist(state.getStation()));

			// update extra information
			UI.reportInfo("retrieving.song.extra.information");
			for (Song song : state.getPlaylist())
			{
				song.setFocusTraits(retrieveFocusTraits(song));
			}
			
			// retrieve covers
			UI.reportInfo("retrieving.album.covers");
			for (Song song : state.getPlaylist())
			{
				song.setAlbumArtBase64(retrieveAlbumArt(song));
			}
		}

		Song song = state.getPlaylist().get(0);
		state.setSong(song);
		state.getPlaylist().remove(song);

		log.info("Next song is " + song.getSongName() + " (" + song.getSongRating() + ")");

		return song.getAudioUrlMap().get("highQuality").getAudioUrl();
	}
	
	public static String retrieveAlbumArt(Song song)
	{
		String coverUrl = song.getAlbumArtUrl();
		log.debug("Retrieve cover from: " + coverUrl);
		String cover = null;
		if (coverUrl != null && !coverUrl.isEmpty())
		{
			try
			{
				cover = HTMLUtil.getURLContentAsBase64String(coverUrl);
			} catch (CommunicationException e)
			{
				log.error("Cannot retrieve cover: " + coverUrl);
			}
		}
		if (cover == null)
		{
			cover = HTMLUtil.getResourceAsBase64String(Page.IMG_PATH + "beluga.200x200.png");
		}
		return cover;
	}

	public static List<String> retrieveFocusTraits(Song song)
	{
		List<String> traits = new ArrayList<String>();
		try
		{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(song.getSongExplorerUrl());
			NodeList nodes = doc.getElementsByTagName("focusTrait");
			for (int i = 0; i < nodes.getLength(); i++)
			{
				traits.add(nodes.item(i).getTextContent());
			}
		} catch (Exception ex)
		{
			log.error("Cannot retrieve focus traits for song " + song.getSongName());
		}
		return traits;
	}

	private List<Song> getPlaylist(Station station) throws BelugaException
	{
		ParameterMap params = getDefaultParameterMap();

		PlayList playlist = new PlayList();
		playlist.setSyncTime(PandoraUtil.getSyncTime());
		playlist.setUserAuthToken(state.getUserAuthToken());
		playlist.setStationToken(station.getStationToken());

		Result result = HTTPUtil.request(Method.GET_PLAYLIST, params, playlist, true);

		List<Song> currentPlaylist = result.getItems();
		PandoraUtil.cleanItemList(currentPlaylist);

		log.info("Retrieved playlist (" + currentPlaylist.size() + " songs) for station " + station.getStationName());

		return currentPlaylist;
	}

	public void login() throws BelugaException
	{
		UI.reportInfo("connection.to.pandora");
		partnerLogin();
		userLogin();
	}

	public void addFeedback(boolean isPositive) throws BelugaException
	{
		ParameterMap params = getDefaultParameterMap();

		Feedback feedback = new Feedback();
		feedback.setSyncTime(PandoraUtil.getSyncTime());
		feedback.setUserAuthToken(state.getUserAuthToken());
		feedback.setPositive(isPositive);
		feedback.setTrackToken(state.getSong().getTrackToken());

		HTTPUtil.request(Method.ADD_FEEDBACK, params, feedback, true);

		if (isPositive)
			state.getSong().setSongRating(1);
		else
			state.getSong().setSongRating(0);
	}

	public void sleepSong() throws BelugaException
	{
		ParameterMap params = getDefaultParameterMap();

		SongSleep songSleep = new SongSleep();
		songSleep.setSyncTime(PandoraUtil.getSyncTime());
		songSleep.setUserAuthToken(state.getUserAuthToken());
		songSleep.setTrackToken(state.getSong().getTrackToken());

		HTTPUtil.request(Method.SLEEP_SONG, params, songSleep, true);
	}

	public void addSongBookmark() throws BelugaException
	{
		ParameterMap params = getDefaultParameterMap();

		SongBookmark songBookmark = new SongBookmark();
		songBookmark.setSyncTime(PandoraUtil.getSyncTime());
		songBookmark.setUserAuthToken(state.getUserAuthToken());
		songBookmark.setTrackToken(state.getSong().getTrackToken());

		HTTPUtil.request(Method.ADD_SONG_BOOKMARK, params, songBookmark, true);
	}

	public void addArtistBookmark() throws BelugaException
	{
		ParameterMap params = getDefaultParameterMap();

		ArtistBookmark artistBookmark = new ArtistBookmark();
		artistBookmark.setSyncTime(PandoraUtil.getSyncTime());
		artistBookmark.setUserAuthToken(state.getUserAuthToken());
		artistBookmark.setTrackToken(state.getSong().getTrackToken());

		HTTPUtil.request(Method.ADD_ARTIST_BOOKMARK, params, artistBookmark, true);
	}

	public Result search(String query) throws BelugaException
	{
		ParameterMap params = getDefaultParameterMap();

		Search search = new Search();
		search.setSyncTime(PandoraUtil.getSyncTime());
		search.setUserAuthToken(state.getUserAuthToken());
		search.setSearchText(query);

		return HTTPUtil.request(Method.SEARCH, params, search, true);
	}

	public void addStation(String musicToken) throws BelugaException
	{
		ParameterMap params = getDefaultParameterMap();

		CreateStation createStation = new CreateStation();
		createStation.setSyncTime(PandoraUtil.getSyncTime());
		createStation.setUserAuthToken(state.getUserAuthToken());
		createStation.setMusicToken(musicToken);

		HTTPUtil.request(Method.CREATE_STATION, params, createStation, true);
	}

	public void addStation(String type, String trackToken) throws BelugaException
	{
		ParameterMap params = getDefaultParameterMap();

		CreateStation createStation = new CreateStation();
		createStation.setSyncTime(PandoraUtil.getSyncTime());
		createStation.setUserAuthToken(state.getUserAuthToken());
		createStation.setMusicType(type);
		createStation.setTrackToken(trackToken);

		HTTPUtil.request(Method.CREATE_STATION, params, createStation, true);
	}

	public void deleteStation() throws BelugaException
	{
		ParameterMap params = getDefaultParameterMap();

		DeleteStation createStation = new DeleteStation();
		createStation.setSyncTime(PandoraUtil.getSyncTime());
		createStation.setUserAuthToken(state.getUserAuthToken());
		createStation.setStationToken(state.getStation().getStationToken());

		HTTPUtil.request(Method.DELETE_STATION, params, createStation, true);
	}

	public void createUser(String username, String password, String birthYearStr, String zipCode, String gender, String emailOptInStr) throws BelugaException
	{
		Integer birthYear = 0;
		try
		{
			birthYear = Integer.valueOf(birthYearStr);
		} catch (NumberFormatException e)
		{
			// we don't care, Pandora is going to reject it anyway
		}
		Boolean emailOptIn = Boolean.valueOf(emailOptInStr);

		ParameterMap params = new ParameterMap();
		params.add("partner_id", state.getPartnerId());
		params.add("auth_token", state.getPartnerAuthToken());

		CreateUser createUser = new CreateUser();
		createUser.setUsername(username);
		createUser.setPassword(password);
		createUser.setBirthYear(birthYear);
		createUser.setZipCode(zipCode);
		createUser.setGender(gender);
		createUser.setEmailOptIn(emailOptIn);

		createUser.setSyncTime(PandoraUtil.getSyncTime());
		createUser.setPartnerAuthToken(state.getPartnerAuthToken());

		HTTPUtil.request(Method.CREATE_USER, params, createUser, true);
	}
}
