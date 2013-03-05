package info.bonjean.beluga.gui.pivot;

import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;

public abstract class AsyncAction extends Action
{
	private MainWindow mainWindow;

	public AsyncAction(MainWindow mainWindow)
	{
		this.mainWindow = mainWindow;
	}

	@Override
	public final void perform(final Component source)
	{
		UIPools.actionPool.execute(new Runnable()
		{
			@Override
			public void run()
			{
				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						mainWindow.setEnabled(false);
					}
				}, true);

				asyncPerform(source);

				ApplicationContext.queueCallback(new Runnable()
				{
					@Override
					public void run()
					{
						mainWindow.setEnabled(true);
					}
				}, true);
			}
		});
	}

	public abstract void asyncPerform(Component source);
}
