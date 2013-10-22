/* Copyright (C) 2012, 2013 Julien Bonjean <julien@bonjean.info>
 * 
 * This file is part of Beluga Player.
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
package info.bonjean.beluga.request;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public enum Method
{
	GET_ADD_METADATA("ad.getAdMetadata"), PARTNER_LOGIN("auth.partnerLogin"), USER_LOGIN("auth.userLogin"), ADD_ARTIST_BOOKMARK(
			"bookmark.addArtistBookmark"), ADD_SONG_BOOKMARK("bookmark.addSongBookmark"), DELETE_ARTIST_BOOKMARK("bookmark.deleteArtistBookmark"), DELETE_SONG_BOOKMARK(
			"bookmark.deleteSongBookmark"), SEARCH("music.search"), SHARE_MUSIC("music.shareMusic"), ADD_FEEDBACK("station.addFeedback"), ADD_MUSIC(
			"station.addMusic"), CREATE_STATION("station.createStation"), DELETE_FEEDBACK("station.deleteFeedback"), DELETE_MUSIC(
			"station.deleteMusic"), DELETE_STATION("station.deleteStation"), GET_GENRE_STATIONS("station.getGenreStations"), GET_GENRE_STATIONS_CHECKSUM(
			"station.getGenreStationsChecksum"), GET_PLAYLIST("station.getPlaylist"), GET_STATION("station.getStation"), SHARE_STATION(
			"station.shareStation"), RENAME_STATION("station.renameStation"), TRANSFORM_SHARED_STATION("station.transformSharedStation"), ECHO(
			"test.echo"), EXPLAIN_TRACK("track.explainTrack"), ACKNOWLEDGE_SUBSCRIPTION_EXPIRATION("user.acknowledgeSubscriptionExpiration"), USER_ASSOCIATE_DEVICE(
			"user.associateDevice"), CAN_SUBSCRIBE("user.canSubscribe"), CREATE_USER("user.createUser"), EMAIL_PASSWORD("user.emailPassword"), GET_BOOKMARKS(
			"user.getBookmarks"), GET_STATION_LIST("user.getStationList"), GET_STATION_LIST_CHECKSUM("user.getStationListChecksum"), PURCHASE_ITUNE_SUBSCRIPTION(
			"user.purchaseItunesSubscription"), SET_QUICK_MIX("user.setQuickMix"), SLEEP_SONG("user.sleepSong"), START_COMPIMENTARY_TRIAL(
			"user.startComplimentaryTrial");

	private String name;

	private Method(String n)
	{
		name = n;
	}

	public String getName()
	{
		return name;
	}
}
