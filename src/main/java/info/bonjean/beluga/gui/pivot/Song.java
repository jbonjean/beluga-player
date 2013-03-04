package info.bonjean.beluga.gui.pivot;

import info.bonjean.beluga.client.BelugaState;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TablePane;

public class Song extends TablePane implements Bindable
{
	private final BelugaState state = BelugaState.getInstance();
	
	@BXML
	Label songTitle;
	@BXML
	Label albumTitle;
	@BXML
	Label artistName;
	@BXML
	Label songTraits;
	@BXML
	ImageView albumCover;

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		songTitle.setText(state.getSong().getSongName());
		albumTitle.setText(state.getSong().getAlbumName());
		artistName.setText("by " + state.getSong().getArtistName());
		
		StringBuffer focusTraits = new StringBuffer();
		for(String focusTrait : state.getSong().getFocusTraits())
		{
			if(focusTraits.length() > 0)
				focusTraits.append(", ");
			focusTraits.append(focusTrait);
		}
		songTraits.setText(focusTraits.toString());
		try
		{
			albumCover.setImage(new URL(state.getSong().getAlbumArtUrl()));
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}
}
