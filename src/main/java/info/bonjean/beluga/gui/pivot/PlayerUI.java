package info.bonjean.beluga.gui.pivot;

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.PandoraPlaylist;
import info.bonjean.beluga.response.Song;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerUI extends TablePane implements Bindable
{
	private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
	
	private final BelugaState state = BelugaState.getInstance();

	@BXML
	MainWindow main;
	@BXML
	Label stationName;
	@BXML
	Label currentTime;
	@BXML
	Label totalTime;
	@BXML
	Meter progress;
	@BXML
	PushButton nextButton;

	javazoom.jl.player.Player mp3Player;
	long duration;

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		stationName.setText("Pandora");
		currentTime.setText("00:00");
		totalTime.setText("00:00");
		progress.setPercentage(0);

		// start the UI sync thread
		new SyncUI().start();

		// start the playback thread
		new Playback().start();
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		nextButton.getAction().setEnabled(enabled);
		nextButton.setEnabled(enabled);
		
		super.setEnabled(enabled);
	}

	private String formatTime(long ms)
	{
		return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(ms),
				TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
	}

	public void stopPlayer()
	{
		mp3Player.close();
	}

	private class Playback extends Thread
	{
		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					try
					{
						Thread.sleep(500);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}

					final Song song = PandoraPlaylist.getInstance().getNext();

					if (song == null)
					{
						log.warn("Playlist returned nothing :'(");
						continue;
					}

					log.info("New song: " + song.getAdditionalAudioUrl());
					
					URL songURL = new URL(song.getAdditionalAudioUrl());
					URLConnection songURLConnection = songURL.openConnection();
					InputStream inputStream = songURLConnection.getInputStream();

					// get the first frame header to get bitrate
					Bitstream bitstream = new Bitstream(inputStream);
					Header header = bitstream.readFrame();
					bitstream.unreadFrame();

					// get file size from HTTP headers
					long songSize = Long.parseLong(songURLConnection.getHeaderField("Content-Length"));

					// calculate the duration
					duration = (long) header.total_ms((int) songSize);

					// update UI
					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							// update song duration
							totalTime.setText(formatTime(duration));
							
							// update station name
							stationName.setText(state.getStation().getStationName());
							
							// notify main window
							main.songChanged(song);
						}
					});

					// initialize the player
					mp3Player = new javazoom.jl.player.Player(songURLConnection.getInputStream());

					// start playback
					mp3Player.play();
					
					log.info("Playback finished");
					
					// make things clean in the UI
					ApplicationContext.queueCallback(new Runnable()
					{
						@Override
						public void run()
						{
							currentTime.setText(formatTime(duration));
							progress.setPercentage(1);
						}
					});

					// playback done, close the stream
					inputStream.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					// always stop the player when done
					if(mp3Player != null)
						mp3Player.close();
					mp3Player = null;
				}
			}
		}
	}

	private class SyncUI extends Thread
	{
		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				if (mp3Player == null || mp3Player.isComplete())
					continue;

				final float progressValue = mp3Player.getPosition() / (float) duration;
				final long position = mp3Player.getPosition();
				
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						currentTime.setText(formatTime(position));
						progress.setPercentage(progressValue);
					}
				});
			}
		}
	}
}
