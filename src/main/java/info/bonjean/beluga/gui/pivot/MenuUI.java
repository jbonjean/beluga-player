package info.bonjean.beluga.gui.pivot;

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuBar.Item;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.TablePane;

public class MenuUI extends TablePane implements Bindable
{
	@BXML
	MenuBar menubar;

	@BXML
	MenuButton stations;

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		if(System.getProperty("debug") != null)
		{
			Menu.Item debugEntry = new Menu.Item("Refresh");
			debugEntry.setAction(Action.getNamedActions().get("refresh"));
			menubar.getItems().get(0).getMenu().getSections().get(0).insert(debugEntry,0);
		}
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		for (Item item : menubar.getItems())
			item.setEnabled(enabled);
		stations.setEnabled(enabled);
		super.setEnabled(enabled);
	}
}
