package info.bonjean.beluga.gui.pivot;

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.player.MusicPlayer;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.response.Station;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.player.Player;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainWindow extends Window implements Bindable
{
	private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
	private final BelugaState state = BelugaState.getInstance();
	private final PandoraClient pandoraClient = PandoraClient.getInstance();
	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();

	@BXML
	TablePane.Row content;
	@BXML
	Label stationName;
	@BXML
	Label currentTime;
	@BXML
	Label totalTime;
	@BXML
	Meter progress;

	public MainWindow()
	{
		Action.getNamedActions().put("exit", new Action()
		{
			@Override
			public void perform(Component source)
			{
				System.exit(0);
			}
		});

		Action.getNamedActions().put("fileNew", new Action()
		{
			@Override
			public void perform(Component source)
			{
				System.out.println("fileNew");
			}
		});

		Action.getNamedActions().put("refresh", new Action()
		{
			@Override
			public void perform(Component source)
			{
				try
				{
					System.out.println("Refresh");
					BXMLSerializer bxmlSerializer = new BXMLSerializer();
					Window oldWindow = (Window) source.getAncestor(MainWindow.class);
					Window newWindow = (Window) bxmlSerializer.readObject(MainWindow.class, PivotUI.BXML_PATH + "main.bxml");
					newWindow.open(getDisplay());
					oldWindow.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		stationName.setText("Pandora");
		currentTime.setText("00:00");
		totalTime.setText("00:00");
		progress.setPercentage(0);

		updateContent("loader.bxml");

		startPandora();
	}

	private void updateContent(String bxmlFile)
	{
		try
		{
			BXMLSerializer bxmlSerializer = new BXMLSerializer();
			Component contentPane = (Component) bxmlSerializer.readObject(MainWindow.class, PivotUI.BXML_PATH + bxmlFile);
			content.remove(0, content.getLength());
			content.add(contentPane);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void gotoSong()
	{
		stationName.setText(state.getStation().getStationName());
		currentTime.setText("00:00");
		totalTime.setText("00:00");
		progress.setPercentage(0);

		updateContent("song.bxml");

		log.info(state.getSong().getAdditionalAudioUrl());

		final TaskListener<String> taskListener = new TaskListener<String>(){
			@Override
			public void taskExecuted(Task<String> task)
			{
			}
			@Override
			public void executeFailed(Task<String> task)
			{
			}
		};
		
		try
		{
			new Task<String>()
			{
				@Override
				public String execute() throws TaskExecutionException
				{
					try
					{
						URL url = new URL(state.getSong().getAdditionalAudioUrl());
						URLConnection uc = url.openConnection();

						Bitstream bs = new Bitstream(uc.getInputStream());
						final Header header = bs.readFrame();

						final long duration = (long) header.total_ms(Integer.parseInt(uc.getHeaderField("Content-Length")));

						final String durationStr = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration), TimeUnit.MILLISECONDS.toSeconds(duration)
								- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));

						ApplicationContext.queueCallback(new Runnable() {
	                        @Override
	                        public void run() {
	                        	totalTime.setText(durationStr);
	                        }
	                    });

						final Player player = new Player(uc.getInputStream());

						new Task<String>()
						{
							@Override
							public String execute() throws TaskExecutionException
							{
								while (true)
								{
									final long position = player.getPosition();
									final String positionStr = String.format(
											"%02d:%02d",
											TimeUnit.MILLISECONDS.toMinutes(position),
											TimeUnit.MILLISECONDS.toSeconds(position)
													- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(position)));
									
									final float progressValue = position/(float)duration;

									ApplicationContext.queueCallback(new Runnable() {
				                        @Override
				                        public void run() {
				                        	currentTime.setText(positionStr);
				                        	progress.setPercentage(progressValue);
				                        }
				                    });
									
									try
									{
										Thread.sleep(100);
									}
									catch (InterruptedException e)
									{
										e.printStackTrace();
									}
								}
							};
						}.execute(new TaskAdapter<String>(taskListener));

						player.play();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					return "";
				}
			}.execute(new TaskAdapter<String>(taskListener));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}

	private void startPandora()
	{
		TaskListener<String> taskListener = new TaskListener<String>()
		{
			@Override
			public void taskExecuted(Task<String> task)
			{
//                activityIndicator.setActive(false);
//                setEnabled(true);
				System.out.println("Synchronous task execution complete: \"" + task.getResult() + "\"");
				if (state.getSong() != null)
					gotoSong();
			}

			@Override
			public void executeFailed(Task<String> task)
			{
//                activityIndicator.setActive(false);
//                setEnabled(true);
				System.err.println(task.getFault());
			}
		};

		new Task<String>()
		{
			@Override
			public String execute() throws TaskExecutionException
			{
				try
				{
					pandoraClient.partnerLogin();
					pandoraClient.userLogin();
					state.reset();

					updatePandoraData();
				}
				catch (BelugaException e)
				{
					e.printStackTrace();
					return ":'(";
				}

				return "Rock'n'roll!!!";
			}
		}.execute(new TaskAdapter<String>(taskListener));
	}

	private void updatePandoraData() throws BelugaException
	{
		String currentStationId = state.getStation() == null ? null : state.getStation().getStationId();
		String selectedStationId = currentStationId == null ? configuration.getDefaultStationId() : currentStationId;

		// update station list
		state.setStationList(pandoraClient.getStationList());

		// if no station, go to station creation page
//		if (state.getStationList().isEmpty())
//		{
//			updateUI(Page.STATION_ADD);
//			return;
//		}

		// select station
		state.setStation(null);
		if (selectedStationId == null)
			state.setStation(state.getStationList().get(0));
		else
		{
			for (Station station : state.getStationList())
			{
				if (station.getStationId().equals(selectedStationId))
				{
					state.setStation(station);
					break;
				}
			}
			if (state.getStation() == null)
				state.setStation(state.getStationList().get(0));
		}

		// retrieve station full information
//		reportInfo("retrieving.station.data");
		state.setStation(pandoraClient.getStation(state.getStation()));

		// station changed
		if (!state.getStation().getStationId().equals(currentStationId))
		{
			// if station changed, reset playlist
			state.setPlaylist(null);

			// update the configuration
			configuration.setDefaultStationId(state.getStation().getStationId());
			configuration.store();

			// and prevent delete, the station does not exist anymore!
//			if (command.equals(Command.DELETE_STATION))
//				command = Command.NEXT;

		}
		if (state.getPlaylist() == null || state.getPlaylist().isEmpty())
		{
			// retrieve playlist from Pandora
//			reportInfo("retrieving.playlist");
			state.setPlaylist(pandoraClient.getPlaylist(state.getStation()));

			// update extra information
//			reportInfo("retrieving.song.extra.information");
			for (Song song : state.getPlaylist())
				song.setFocusTraits(pandoraClient.retrieveFocusTraits(song));

			// retrieve covers
//			reportInfo("retrieving.album.covers");
//			for (Song song : state.getPlaylist())
//				song.setAlbumArtBase64(pandoraClient.retrieveCover(song.getAlbumArtUrl()));
		}

		// check song
		if (state.getSong() == null)
		{
			state.setSong(state.getPlaylist().get(0));
			state.getPlaylist().remove(state.getSong());
			// new Notification(HTMLUtil.getPageHTML(Page.NOTIFICATION));
		}
	}
}