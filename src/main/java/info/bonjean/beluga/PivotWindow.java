package info.bonjean.beluga;

import java.net.URL;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;

public class PivotWindow extends Window implements Bindable
{
	private final Window instance = this;

	public PivotWindow()
	{
		super();

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
					Window newWindow = (Window) bxmlSerializer.readObject(PivotUI.class, "main.bxml");
					newWindow.open(getDisplay());
					instance.close();
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
		Label songTitle = (Label) namespace.get("songTitle");
		songTitle.setText("Song Title");
		
		Label albumTitle = (Label) namespace.get("albumTitle");
		albumTitle.setText("Album Title");

		Label artistName = (Label) namespace.get("artistName");
		artistName.setText("by Artist Name");
		
		Label stationName = (Label) namespace.get("stationName");
		stationName.setText("Radio Nova");

		Label songTraits = (Label) namespace.get("songTraits");
		songTraits
				.setText(" Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam at iaculis erat. Vestibulum a sem a lacus vehicula aliquam. Integer vel porttitor turpis. Proin facilisis elementum neque id adipiscing. Morbi at lectus egestas nisi dignissim malesuada. Integer quis laoreet ipsum. Duis fringilla eleifend volutpat. Mauris tincidunt dapibus auctor. Integer nec massa elit. Ut eget turpis magna. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Curabitur dignissim fringilla risus, et suscipit quam blandit eget. \nMaecenas porta ornare sagittis. Pellentesque venenatis sapien sit amet elit lobortis rutrum in venenatis lorem. Suspendisse nec mi sit amet erat tincidunt pretium quis at leo. Suspendisse nec diam id neque accumsan consectetur malesuada dictum sem. Donec nec lobortis eros. Ut laoreet molestie accumsan. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Morbi malesuada dolor lacinia arcu volutpat eu aliquam erat elementum. Mauris eget gravida elit. Ut tincidunt aliquet libero. Nulla fermentum nulla vel diam condimentum sodales. Vestibulum sed volutpat erat. Phasellus quis erat ac odio suscipit aliquam ac eget lorem.\nNam dapibus venenatis enim. Vestibulum eu ipsum non metus congue accumsan sit amet sed arcu. Integer vel orci molestie nisi iaculis imperdiet vitae et diam. Aliquam eu turpis ut magna consectetur pretium. Aliquam accumsan adipiscing purus, a gravida risus consectetur in. Nam luctus ultrices massa, consequat vulputate nibh vehicula luctus. Sed auctor diam nec lectus consequat et imperdiet justo pulvinar. Nulla metus sapien, mollis auctor dapibus id, volutpat pellentesque massa.");
		
		Label currentTime = (Label) namespace.get("currentTime");
		currentTime.setText("0:59");
		
		Label totalTime = (Label) namespace.get("totalTime");
		totalTime.setText("4:32");
		
		Meter progress = (Meter) namespace.get("progress");
		progress.setPercentage(0.27);
		
		PushButton pushButton = (PushButton)namespace.get("exitButton");
        pushButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
            	System.exit(0);
            }
        });
	}
}