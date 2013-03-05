package info.bonjean.beluga.gui.pivot;

import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Component;

public abstract class AsyncAction extends Action
{
	@Override
	public final void perform(final Component source)
	{
		UIPools.actionPool.execute(new Runnable()
		{
			@Override
			public void run()
			{
				asyncPerform(source);
			}
		});

	}

	public abstract void asyncPerform(Component source);
}
