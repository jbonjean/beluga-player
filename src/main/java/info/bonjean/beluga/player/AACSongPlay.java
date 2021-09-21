/*
 * Copyright (C) 2012-2021 Julien Bonjean <julien@bonjean.info>
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
package info.bonjean.beluga.player;

import info.bonjean.beluga.exception.InternalException;
import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AACSongPlay extends SongPlay {
	private static final Logger log = LoggerFactory.getLogger(AACSongPlay.class);

	private Decoder decoder;
	private AudioTrack track;
	private volatile long position;

	public AACSongPlay(AudioDeviceManager audioDeviceManager) {
		super(audioDeviceManager);
	}

	@Override
	public void loadSong(String url) throws Exception {
		// create container
		final MP4Container cont = new MP4Container(openStream(url));
		final Movie movie = cont.getMovie();
		setDuration((long) Math.floor(movie.getDuration() * 1000));

		// find AAC track
		final List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
		if (tracks.isEmpty()) {
			throw new InternalException("noAACTrackFound");
		}
		track = (AudioTrack) tracks.get(0);

		// create AAC decoder
		decoder = new Decoder(track.getDecoderSpecificInfo());
		setBitrate(track.getSampleRate());
		position = 0;
	}

	@Override
	public void play() throws Exception {
		audioInit(track.getSampleRate(), track.getSampleSize(), track.getChannelCount(), true, true);

		Frame frame;
		SampleBuffer buf = new SampleBuffer();
		int errorCount = 0;
		try {
			while (active && track.hasMoreFrames()) {
				frame = track.readNextFrame();
				position = (long) Math.floor(frame.getTime() * 1000);
				try {
					decoder.decodeFrame(frame.getData(), buf);
					byte[] b = buf.getData();
					audioWrite(b, b.length);
				} catch (AACException e) {
					// since the frames are separate, decoding can continue if one fails
					log.debug(e.getMessage());
					errorCount++;
					if (errorCount >= 5) {
						throw new InternalException("tooManyDecodingErrors");
					}
				}
			}
		} finally {
			if (active) {
				finish();
			}
			close();
		}
	}

	@Override
	public long getPosition() {
		return position;
	}
}
