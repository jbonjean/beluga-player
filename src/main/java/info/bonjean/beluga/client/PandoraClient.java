/*
 * Copyright (C) 2012-2020 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.client;

import com.google.gson.reflect.TypeToken;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.request.ArtistBookmarkDeleteRequest;
import info.bonjean.beluga.request.ArtistBookmarkRequest;
import info.bonjean.beluga.request.BookmarksRequest;
import info.bonjean.beluga.request.CreateStationRequest;
import info.bonjean.beluga.request.CreateUserRequest;
import info.bonjean.beluga.request.DeleteStationRequest;
import info.bonjean.beluga.request.FeedbackDeleteRequest;
import info.bonjean.beluga.request.FeedbackRequest;
import info.bonjean.beluga.request.Method;
import info.bonjean.beluga.request.NoParameterRequest;
import info.bonjean.beluga.request.ParameterMap;
import info.bonjean.beluga.request.PartnerAuthRequest;
import info.bonjean.beluga.request.PlayListRequest;
import info.bonjean.beluga.request.RenameStationRequest;
import info.bonjean.beluga.request.SearchRequest;
import info.bonjean.beluga.request.SetQuickMixRequest;
import info.bonjean.beluga.request.SongBookmarkDeleteRequest;
import info.bonjean.beluga.request.SongBookmarkRequest;
import info.bonjean.beluga.request.SongSleepRequest;
import info.bonjean.beluga.request.StationListRequest;
import info.bonjean.beluga.request.StationRequest;
import info.bonjean.beluga.request.UserLoginRequest;
import info.bonjean.beluga.response.Bookmarks;
import info.bonjean.beluga.response.Category;
import info.bonjean.beluga.response.Feedback;
import info.bonjean.beluga.response.Response;
import info.bonjean.beluga.response.Result;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.response.Station;
import info.bonjean.beluga.util.HTTPUtil;
import info.bonjean.beluga.util.PandoraUtil;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class PandoraClient {
	private static final Logger log = LoggerFactory.getLogger(PandoraClient.class);
	private static PandoraClient instance;
	private String partnerId;
	private String partnerAuthToken;
	private String userAuthToken;
	private String userId;

	private static final BelugaConfiguration configuration = BelugaConfiguration.getInstance();

	private PandoraClient() {
	}

	public static PandoraClient getInstance() {
		if (instance == null)
			instance = new PandoraClient();
		return instance;
	}

	public void reset() {
		partnerId = null;
		userId = null;
		partnerAuthToken = null;
		userAuthToken = null;
	}

	public boolean isPandoraReachable() {
		return partnerAuthToken != null && partnerAuthToken.length() > 0;
	}

	public boolean isLoggedIn() {
		return userAuthToken != null && userAuthToken.length() > 0;
	}

	private ParameterMap getDefaultParameterMap() {
		ParameterMap params = new ParameterMap();
		params.add("partner_id", partnerId);
		params.add("auth_token", userAuthToken);
		params.add("user_id", userId);

		return params;
	}

	public void partnerLogin() throws BelugaException {
		partnerId = null;
		partnerAuthToken = null;

		Result result = HTTPUtil.<Result> request(Method.PARTNER_LOGIN, null, new PartnerAuthRequest(), false,
				new TypeToken<Response<Result>>() {
				});

		partnerId = result.getPartnerId();
		partnerAuthToken = result.getPartnerAuthToken();
	}

	public void userLogin() throws BelugaException {
		userId = null;
		userAuthToken = null;

		ParameterMap params = new ParameterMap();
		params.add("partner_id", partnerId);
		params.add("auth_token", partnerAuthToken);

		UserLoginRequest userLogin = new UserLoginRequest();
		userLogin.setSyncTime(PandoraUtil.getSyncTime());
		userLogin.setPartnerAuthToken(partnerAuthToken);
		userLogin.setUsername(configuration.getUserName());
		userLogin.setPassword(configuration.getPassword());

		Result result = HTTPUtil.<Result> request(Method.USER_LOGIN, params, userLogin, true,
				new TypeToken<Response<Result>>() {
				});

		userId = result.getUserId();
		userAuthToken = result.getUserAuthToken();
	}

	public List<Station> getStationList() throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		StationListRequest stationList = new StationListRequest();
		stationList.setSyncTime(PandoraUtil.getSyncTime());
		stationList.setUserAuthToken(userAuthToken);

		Result result = HTTPUtil.<Result> request(Method.GET_STATION_LIST, params, stationList, true,
				new TypeToken<Response<Result>>() {
				});

		return result.getStations();
	}

	public List<Category> getGenreStationList() throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		NoParameterRequest stationList = new NoParameterRequest();
		stationList.setSyncTime(PandoraUtil.getSyncTime());
		stationList.setUserAuthToken(userAuthToken);

		Result result = HTTPUtil.<Result> request(Method.GET_GENRE_STATIONS, params, stationList, true,
				new TypeToken<Response<Result>>() {
				});

		return result.getCategories();
	}

	public Station getStation(Station station) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();
		StationRequest stationRequest = new StationRequest();
		stationRequest.setSyncTime(PandoraUtil.getSyncTime());
		stationRequest.setUserAuthToken(userAuthToken);
		stationRequest.setStationToken(station.getStationToken());
		stationRequest.setIncludeExtendedAttributes(true);

		Station stationFull = HTTPUtil.<Station> request(Method.GET_STATION, params, stationRequest, true,
				new TypeToken<Response<Station>>() {
				});
		return stationFull;
	}

	public List<String> retrieveFocusTraits(Song song) {
		List<String> traits = new ArrayList<String>();
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(song.getSongExplorerUrl());
			NodeList nodes = doc.getElementsByTagName("focusTrait");
			for (int i = 0; i < nodes.getLength(); i++) {
				traits.add(nodes.item(i).getTextContent());
			}
		} catch (Exception ex) {
			log.debug("Cannot retrieve focus traits for song " + song.getSongName());
		}
		return traits;
	}

	public List<Song> getPlaylist(Station station) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		PlayListRequest playlist = new PlayListRequest();
		playlist.setSyncTime(PandoraUtil.getSyncTime());
		playlist.setUserAuthToken(userAuthToken);
		playlist.setStationToken(station.getStationToken());
		playlist.setAdditionalAudioUrl("HTTP_128_MP3");

		Result result = HTTPUtil.<Result> request(Method.GET_PLAYLIST, params, playlist, true,
				new TypeToken<Response<Result>>() {
				});

		List<Song> currentPlaylist = result.getItems();
		PandoraUtil.cleanItemList(currentPlaylist);

		log.debug("Playlist updated");

		return currentPlaylist;
	}

	public Bookmarks getBookmarks() throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		BookmarksRequest playlist = new BookmarksRequest();
		playlist.setSyncTime(PandoraUtil.getSyncTime());
		playlist.setUserAuthToken(userAuthToken);

		return HTTPUtil.<Bookmarks> request(Method.GET_BOOKMARKS, params, playlist, true,
				new TypeToken<Response<Bookmarks>>() {
				});
	}

	public Feedback addFeedback(Song song, boolean isPositive) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		FeedbackRequest feedbackRequest = new FeedbackRequest();
		feedbackRequest.setSyncTime(PandoraUtil.getSyncTime());
		feedbackRequest.setUserAuthToken(userAuthToken);
		feedbackRequest.setPositive(isPositive);
		feedbackRequest.setTrackToken(song.getTrackToken());

		Feedback feedback = HTTPUtil.<Feedback> request(Method.ADD_FEEDBACK, params, feedbackRequest, true,
				new TypeToken<Response<Feedback>>() {
				});

		if (isPositive)
			song.setSongRating(1);

		return feedback;
	}

	public void deleteSongBookmark(String bookmarkToken) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		SongBookmarkDeleteRequest songBookmarkDeleteRequest = new SongBookmarkDeleteRequest();
		songBookmarkDeleteRequest.setSyncTime(PandoraUtil.getSyncTime());
		songBookmarkDeleteRequest.setUserAuthToken(userAuthToken);
		songBookmarkDeleteRequest.setBookmarkToken(bookmarkToken);

		HTTPUtil.<Result> request(Method.DELETE_SONG_BOOKMARK, params, songBookmarkDeleteRequest, true,
				new TypeToken<Response<Result>>() {
				});
	}

	public void deleteArtistBookmark(String bookmarkToken) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		ArtistBookmarkDeleteRequest artistBookmarkDeleteRequest = new ArtistBookmarkDeleteRequest();
		artistBookmarkDeleteRequest.setSyncTime(PandoraUtil.getSyncTime());
		artistBookmarkDeleteRequest.setUserAuthToken(userAuthToken);
		artistBookmarkDeleteRequest.setBookmarkToken(bookmarkToken);

		HTTPUtil.<Result> request(Method.DELETE_ARTIST_BOOKMARK, params, artistBookmarkDeleteRequest, true,
				new TypeToken<Response<Result>>() {
				});
	}

	public void deleteFeedback(String feedbackId) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		FeedbackDeleteRequest feedbackDeleteRequest = new FeedbackDeleteRequest();
		feedbackDeleteRequest.setSyncTime(PandoraUtil.getSyncTime());
		feedbackDeleteRequest.setUserAuthToken(userAuthToken);
		feedbackDeleteRequest.setFeedbackId(feedbackId);

		HTTPUtil.<Result> request(Method.DELETE_FEEDBACK, params, feedbackDeleteRequest, true,
				new TypeToken<Response<Result>>() {
				});
	}

	public void sleepSong(Song song) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		SongSleepRequest songSleep = new SongSleepRequest();
		songSleep.setSyncTime(PandoraUtil.getSyncTime());
		songSleep.setUserAuthToken(userAuthToken);
		songSleep.setTrackToken(song.getTrackToken());

		HTTPUtil.<Result> request(Method.SLEEP_SONG, params, songSleep, true, new TypeToken<Response<Result>>() {
		});
	}

	public void addSongBookmark(String trackToken) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		SongBookmarkRequest songBookmark = new SongBookmarkRequest();
		songBookmark.setSyncTime(PandoraUtil.getSyncTime());
		songBookmark.setUserAuthToken(userAuthToken);
		songBookmark.setTrackToken(trackToken);

		HTTPUtil.<Result> request(Method.ADD_SONG_BOOKMARK, params, songBookmark, true,
				new TypeToken<Response<Result>>() {
				});
	}

	public void addArtistBookmark(String trackToken) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		ArtistBookmarkRequest artistBookmark = new ArtistBookmarkRequest();
		artistBookmark.setSyncTime(PandoraUtil.getSyncTime());
		artistBookmark.setUserAuthToken(userAuthToken);
		artistBookmark.setTrackToken(trackToken);

		HTTPUtil.<Result> request(Method.ADD_ARTIST_BOOKMARK, params, artistBookmark, true,
				new TypeToken<Response<Result>>() {
				});
	}

	public Result search(String query) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		SearchRequest search = new SearchRequest();
		search.setSyncTime(PandoraUtil.getSyncTime());
		search.setUserAuthToken(userAuthToken);
		search.setSearchText(query);

		return HTTPUtil.<Result> request(Method.SEARCH, params, search, true, new TypeToken<Response<Result>>() {
		});
	}

	public void addStation(String musicToken) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		CreateStationRequest createStation = new CreateStationRequest();
		createStation.setSyncTime(PandoraUtil.getSyncTime());
		createStation.setUserAuthToken(userAuthToken);
		createStation.setMusicToken(musicToken);

		HTTPUtil.<Result> request(Method.CREATE_STATION, params, createStation, true,
				new TypeToken<Response<Result>>() {
				});
	}

	public void addStation(String type, String trackToken) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		CreateStationRequest createStation = new CreateStationRequest();
		createStation.setSyncTime(PandoraUtil.getSyncTime());
		createStation.setUserAuthToken(userAuthToken);
		createStation.setMusicType(type);
		createStation.setTrackToken(trackToken);

		HTTPUtil.<Result> request(Method.CREATE_STATION, params, createStation, true,
				new TypeToken<Response<Result>>() {
				});
	}

	public void deleteStation(Station station) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		DeleteStationRequest deleteStation = new DeleteStationRequest();
		deleteStation.setSyncTime(PandoraUtil.getSyncTime());
		deleteStation.setUserAuthToken(userAuthToken);
		deleteStation.setStationToken(station.getStationToken());

		HTTPUtil.<Result> request(Method.DELETE_STATION, params, deleteStation, true,
				new TypeToken<Response<Result>>() {
				});
	}

	public void renameStation(Station station, String newName) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		RenameStationRequest renameStation = new RenameStationRequest();
		renameStation.setSyncTime(PandoraUtil.getSyncTime());
		renameStation.setUserAuthToken(userAuthToken);
		renameStation.setStationToken(station.getStationToken());
		renameStation.setStationName(newName);

		HTTPUtil.<Result> request(Method.RENAME_STATION, params, renameStation, true,
				new TypeToken<Response<Result>>() {
				});
	}

	public void setQuickMix(List<String> quickMixStationIds) throws BelugaException {
		ParameterMap params = getDefaultParameterMap();

		SetQuickMixRequest setQuickMix = new SetQuickMixRequest();
		setQuickMix.setSyncTime(PandoraUtil.getSyncTime());
		setQuickMix.setUserAuthToken(userAuthToken);
		setQuickMix.setQuickMixStationIds(quickMixStationIds);

		HTTPUtil.<Result> request(Method.SET_QUICK_MIX, params, setQuickMix, true, new TypeToken<Response<Result>>() {
		});
	}

	public void createUser(String username, String password, String birthYearStr, String zipCode, String gender,
			String emailOptInStr) throws BelugaException {
		Integer birthYear = 0;
		try {
			birthYear = Integer.valueOf(birthYearStr);
		} catch (NumberFormatException e) {
			// we don't care, Pandora is going to reject it anyway
		}
		Boolean emailOptIn = Boolean.valueOf(emailOptInStr);

		ParameterMap params = new ParameterMap();
		params.add("partner_id", partnerId);
		params.add("auth_token", partnerAuthToken);

		CreateUserRequest createUser = new CreateUserRequest();
		createUser.setUsername(username);
		createUser.setPassword(password);
		createUser.setBirthYear(birthYear);
		createUser.setZipCode(zipCode);
		createUser.setGender(gender);
		createUser.setEmailOptIn(emailOptIn);

		createUser.setSyncTime(PandoraUtil.getSyncTime());
		createUser.setPartnerAuthToken(partnerAuthToken);

		HTTPUtil.<Result> request(Method.CREATE_USER, params, createUser, true, new TypeToken<Response<Result>>() {
		});
	}
}
