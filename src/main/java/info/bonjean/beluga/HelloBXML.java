package info.bonjean.beluga;

import java.awt.Dimension;
import java.net.URL;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;

public class HelloBXML implements Application
{
	private Window window = null;

	public static void main(String[] args) throws BackingStoreException
	{
		Preferences preferences = Preferences.userNodeForPackage(DesktopApplicationContext.class);
		preferences = preferences.node(HelloBXML.class.getName());
		preferences.clear();
		preferences.putInt("width", 600);
		preferences.putInt("height", 400);
		preferences.putBoolean("resizable", false);
		preferences.flush();

		DesktopApplicationContext.main(HelloBXML.class, new String[]{});
	}
	
	@Override
	public void startup(Display display, Map<String, String> properties) throws Exception
	{
		BXMLSerializer bxmlSerializer = new BXMLSerializer();
		window = (Window) bxmlSerializer.readObject(HelloBXML.class, "hello.bxml");
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