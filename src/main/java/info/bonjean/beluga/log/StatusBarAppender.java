package info.bonjean.beluga.log;

import info.bonjean.beluga.gui.pivot.AccountCreationUI;
import info.bonjean.beluga.gui.pivot.MainWindow;
import info.bonjean.beluga.gui.pivot.PreferencesUI;

import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Label;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class StatusBarAppender<E> extends AppenderBase<E>
{
	private static Label statusBar = null;
	private static final String[] exceptions = { MainWindow.class.getName(), PreferencesUI.class.getName(), AccountCreationUI.class.getName() };

	private boolean isException(String loggerName)
	{
		for (String exception : exceptions)
		{
			if (exception.equals(loggerName))
				return true;
		}
		return false;
	}

	@Override
	protected void append(final E eventObject)
	{
		// if not a loggingevent, what is it?
		if (!(eventObject instanceof LoggingEvent))
			return;

		LoggingEvent event = (LoggingEvent) eventObject;

		// don't log if less than warn, except for a some exceptions
		// (not easy to do in logback.xml)
		if (!event.getLevel().isGreaterOrEqual(Level.WARN) && !isException(event.getLoggerName()))
			return;

		if (statusBar != null)
		{
			ApplicationContext.queueCallback(new Runnable()
			{
				@Override
				public void run()
				{
					statusBar.setText(eventObject.toString());
				}
			}, false);
		}
	}

	public static void setStatusBar(Label statusBar)
	{
		StatusBarAppender.statusBar = statusBar;
	}
}