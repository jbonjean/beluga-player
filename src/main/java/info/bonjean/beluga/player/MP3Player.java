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
package info.bonjean.beluga.player;

import info.bonjean.beluga.exception.CommunicationException;
import info.bonjean.beluga.exception.InternalException;
import javazoom.jl.decoder.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;

public class MP3Player extends AudioPlayer {
	private static final Logger log = LoggerFactory.getLogger(MP3Player.class);

	private Bitstream bitstream;
	private Decoder decoder;

	@Override
	public void loadSong(String url)
			throws JavaLayerException, MalformedURLException, IOException, CommunicationException, InternalException {
		bitstream = new Bitstream(openStream(url));

		// get the first frame header to get bitrate and compute duration
		Header frame = bitstream.readFrame();
		if (frame == null) {
			close();
			throw new IOException("noAudioStream");
		}
		setBitrate(frame.bitrate());
		bitstream.unreadFrame();
		setDuration((getContentLength() * 1000) / (getBitrate() / 8));

		// set the decoder
		decoder = new Decoder();

		// decode the first frame to initialize the decoder
		decoder.decodeFrame(frame, bitstream);
	}

	@Override
	public void play() throws JavaLayerException, InternalException {
		audioInit(decoder.getOutputFrequency(), 16, decoder.getOutputChannels(), true, false);

		try {
			while (active) {
				Header h = bitstream.readFrame();
				if (h == null) {
					log.debug("end of the stream has been reached");
					break;
				}

				try {
					SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h, bitstream);
					int length = output.getBufferLength();
					audioWrite(output.getBuffer(), length);
				} finally {
					bitstream.closeFrame();
				}
			}
		} catch (JavaLayerException e) {
			if (StringUtils.isNotBlank(e.getMessage())) {
				log.error(e.getMessage());
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
		return (bitstream.getPosition() * 1000) / (getBitrate() / 8);
	}

	@Override
	public void close() {
		super.close();
		// cleanup bitstream
		try {
			if (bitstream != null)
				bitstream.close();
		} catch (BitstreamException e) {
			log.debug(e.getMessage());
		}
	}
}
