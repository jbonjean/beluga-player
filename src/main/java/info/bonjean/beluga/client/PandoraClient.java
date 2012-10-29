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
package info.bonjean.beluga.client;

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.log.Logger;
import info.bonjean.beluga.request.Feedback;
import info.bonjean.beluga.request.Method;
import info.bonjean.beluga.request.ParameterMap;
import info.bonjean.beluga.request.PartnerAuth;
import info.bonjean.beluga.request.PlayList;
import info.bonjean.beluga.request.SleepSong;
import info.bonjean.beluga.request.StationList;
import info.bonjean.beluga.request.UserLogin;
import info.bonjean.beluga.response.Result;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.response.Station;
import info.bonjean.beluga.statefull.BelugaState;
import info.bonjean.beluga.util.HTTPUtil;
import info.bonjean.beluga.util.PandoraUtil;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class PandoraClient
{
	private static final Logger log = new Logger(PandoraClient.class);
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

	public ParameterMap getDefaultParameterMap() throws UnsupportedEncodingException
	{
		ParameterMap params = new ParameterMap();
		params.add("partner_id", state.getPartnerId());
		params.add("auth_token", state.getUserAuthToken());
		params.add("user_id", state.getUserId());
		return params;
	}

	public void partnerLogin() throws Exception
	{
		Result result = HTTPUtil.request(Method.PARTNER_LOGIN, null, new PartnerAuth(), false);

		state.setPartnerId(result.getPartnerId());
		state.setPartnerAuthToken(result.getPartnerAuthToken());
	}

	public void userLogin() throws Exception
	{
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

	public void updateStationList() throws Exception
	{
		state.setStationList(getStationList());
	}

	private List<Station> getStationList() throws Exception
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

	public void selectStation(Station station) throws Exception
	{
		log.info("Select station " + station.getStationName());

		state.setStation(station);
		state.setPlaylist(getPlaylist(station));
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

	public void selectStation(String stationId) throws Exception
	{
		selectStation(getStationById(stationId));
	}

	public String nextSong() throws Exception
	{
		if (state.getPlaylist().isEmpty())
			state.setPlaylist(getPlaylist(state.getStation()));

		Song song = state.getPlaylist().get(0);
		state.setSong(song);
		state.getPlaylist().remove(song);

		log.info("Next song is " + song.getSongName() + " (" + song.getSongRating() + ")");

		return song.getAudioUrlMap().get("highQuality").getAudioUrl();
	}

	private List<Song> getPlaylist(Station station) throws Exception
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

	public void login() throws Exception
	{
		partnerLogin();
		userLogin();
	}

	public void addFeedback(boolean isPositive) throws Exception
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

	public void sleepSong() throws Exception
	{
		ParameterMap params = getDefaultParameterMap();

		SleepSong sleepSong = new SleepSong();
		sleepSong.setSyncTime(PandoraUtil.getSyncTime());
		sleepSong.setUserAuthToken(state.getUserAuthToken());
		sleepSong.setTrackToken(state.getSong().getTrackToken());

		HTTPUtil.request(Method.SLEEP_SONG, params, sleepSong, true);
	}
}
