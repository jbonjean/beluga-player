package info.bonjean.beluga.gui.pivot;

import info.bonjean.beluga.client.BelugaState;
import info.bonjean.beluga.client.PandoraClient;
import info.bonjean.beluga.client.PandoraPlaylist;
import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.exception.BelugaException;
import info.bonjean.beluga.gui.PivotUI;
import info.bonjean.beluga.response.Song;
import info.bonjean.beluga.response.Station;

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.TablePane;
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
	TablePane.Row contentWrapper;

	@BXML
	PlayerUI playerUI;

	@BXML
	Menu.Section stations;
	
	@BXML
	MenuUI menuUI;
	
	Component content;

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

		Action.getNamedActions().put("nextSong", new AsyncAction()
		{
			@Override
			public void asyncPerform(Component source)
			{
				playerUI.stopPlayer();
			}
		});

		Action.getNamedActions().put("stationSelect", new AsyncAction()
		{
			@Override
			public void asyncPerform(Component source)
			{
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						menuUI.setEnabled(false);
						content.setEnabled(false);
						playerUI.setEnabled(false);
					}
				},true);
				
				Station station = (Station) source.getUserData().get("station");
				try
				{
					selectStation(station);
					playerUI.stopPlayer();
				}
				catch (BelugaException e)
				{
					e.printStackTrace();
				}
				
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						menuUI.setEnabled(true);
						content.setEnabled(true);
						playerUI.setEnabled(true);
					}
				},false);
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
		updateContent("loader.bxml");
		startPandora();
	}

	public void songChanged(Song song)
	{
		log.info("Received song changed notification");
		gotoSong();
	}

	private void updateContent(String bxmlFile)
	{
		try
		{
			BXMLSerializer bxmlSerializer = new BXMLSerializer();
			content = (Component) bxmlSerializer.readObject(MainWindow.class, PivotUI.BXML_PATH + bxmlFile);
			contentWrapper.remove(0, contentWrapper.getLength());
			contentWrapper.add(content);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void gotoSong()
	{
		updateContent("song.bxml");
	}

	private void startPandora()
	{
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					pandoraClient.partnerLogin();
					pandoraClient.userLogin();
					state.reset();

					selectStation(null);
				}
				catch (BelugaException e)
				{
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void selectStation(Station newStation) throws BelugaException
	{
		// update station list
		state.setStationList(pandoraClient.getStationList());

		// rebuild menu entry
		ApplicationContext.queueCallback(new Runnable()
		{
			@Override
			public void run()
			{
				stations.remove(0, stations.getLength());
				for (Station station : state.getStationList())
				{
					Menu.Item item = new Menu.Item(station.getStationName());
					item.getUserData().put("station", station);
					item.setAction(Action.getNamedActions().get("stationSelect"));
					stations.add(item);
				}
			}
		});

		// if no station requested, select configuration one
		if (newStation == null)
		{
			for (Station station : state.getStationList())
			{
				if (station.getStationId().equals(configuration.getDefaultStationId()))
				{
					newStation = station;
					break;
				}
			}
		}
		// else check if station requested is valid
		else
		{
			boolean found = false;
			for (Station station : state.getStationList())
			{
				if (station.getStationId().equals(newStation.getStationId()))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				log.warn("Requested station does not exist anymore");
				newStation = null;
			}
		}

		// at this point, if no station has been selected, there has been a problem, select first one
		if(newStation == null)
			newStation = state.getStationList().get(0);

		// check if station changed
		if (state.getStation() == null || !newStation.getStationId().equals(state.getStation().getStationId()))
		{
			// invalidate playlist
			PandoraPlaylist.getInstance().clear();

			// update the configuration
			configuration.setDefaultStationId(newStation.getStationId());
			configuration.store();
		}

		state.setStation(newStation);
		log.info("New station selected: " + state.getStation().getStationName());
	}
}