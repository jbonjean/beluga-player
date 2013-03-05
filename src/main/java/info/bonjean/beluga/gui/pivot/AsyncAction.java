package info.bonjean.beluga.gui.pivot;

import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Component;

public abstract class AsyncAction extends Action
{
	@Override
	public final void perform(final Component source)
	{
		new Thread(){
			@Override
			public void run()
			{
				asyncPerform(source);
			}
		}.start();

	}

	public abstract void asyncPerform(Component source);
}
