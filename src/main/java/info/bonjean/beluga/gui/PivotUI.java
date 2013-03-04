package info.bonjean.beluga.gui;

import info.bonjean.beluga.gui.pivot.MainWindow;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;

public class PivotUI implements Application
{
	private Window window = null;
	public static final String BXML_PATH = "/bxml/";

	public static void startDesktopUI()
	{
		try
		{
			Preferences preferences = Preferences.userNodeForPackage(DesktopApplicationContext.class);
			preferences = preferences.node(PivotUI.class.getName());
			preferences.clear();
			preferences.putInt("width", 600);
			preferences.putInt("height", 400);
			preferences.putBoolean("resizable", false);
			preferences.flush();
		}
		catch (BackingStoreException e)
		{
		}

		DesktopApplicationContext.main(PivotUI.class, new String[] {});
	}

	public static void main(String[] args)
	{
		startDesktopUI();
	}

	@Override
	public void startup(Display display, Map<String, String> properties) throws Exception
	{
		BXMLSerializer bxmlSerializer = new BXMLSerializer();
		window = (Window) bxmlSerializer.readObject(MainWindow.class, BXML_PATH + "main.bxml");
		window.open(display);
	}
	


	@Override
	public boolean shutdown(boolean optional)
	{
		if (window != null)
		{
			window.close();
		}

		return false;
	}

	@Override
	public void suspend()
	{
	}

	@Override
	public void resume()
	{
	}
}