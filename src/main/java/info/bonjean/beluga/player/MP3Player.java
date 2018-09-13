/*
 * Copyright (C) 2012-2018 Julien Bonjean <julien@bonjean.info>
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
import java.io.IOException;
import java.net.MalformedURLException;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Julien Bonjean <julien@bonjean.info>
 *
 */
public class MP3Player extends AudioPlayer {
	private static final Logger log = LoggerFactory.getLogger(MP3Player.class);

	private static final byte[] byteBuffer = new byte[SampleBuffer.OBUFFERSIZE * 2];

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
	public void play(boolean dummy) throws JavaLayerException, InternalException {
		audioInit(dummy, decoder.getOutputFrequency(), 16, decoder.getOutputChannels(), true, false);

		try {
			while (active) {
				Header h = bitstream.readFrame();
				if (h == null)
					continue;

				try {
					SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h, bitstream);
					int length = output.getBufferLength();
					audioWrite(toByteArray(output.getBuffer(), 0, length), length * 2);
				} finally {
					bitstream.closeFrame();
				}
			}
		} catch (JavaLayerException e) {
			throw e;
		} finally {
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

	private byte[] toByteArray(short[] samples, int offset, int length) {
		int idx = 0;
		short s;
		while (length-- > 0) {
			s = samples[offset++];
			byteBuffer[idx++] = (byte) s;
			byteBuffer[idx++] = (byte) (s >>> 8);
		}
		return byteBuffer;
	}
}
