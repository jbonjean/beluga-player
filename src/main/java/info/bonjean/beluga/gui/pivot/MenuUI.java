package info.bonjean.beluga.gui.pivot;

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuBar.Item;
import org.apache.pivot.wtk.TablePane;

public class MenuUI extends TablePane implements Bindable
{
	@BXML
	MenuBar menubar;
	
	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		for(Item item : menubar.getItems())
			item.setEnabled(enabled);
		super.setEnabled(enabled);
	}
}
