package info.bonjean.beluga.log;

import info.bonjean.beluga.gui.pivot.ThreadPools;
import info.bonjean.beluga.util.ResourcesUtil;

import java.io.Serializable;
import java.util.Date;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.helpers.Booleans;
import org.apache.logging.log4j.core.layout.HTMLLayout;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Label;

@Plugin(name = "StatusBar", category = "Core", elementType = "appender", printObject = true)
public final class StatusBarAppender extends AbstractAppender
{
	private static final long LOG_MESSAGE_DURATION = 3 * 1000;

	private static Label statusBarText;
	private static Resources resources;
	private long lastMessageDisplayTime = 0L;
	private LogEvent messageDisplayed = null;

	protected StatusBarAppender(String name, Filter filter, Layout<? extends Serializable> layout,
			boolean ignoreExceptions)
	{
		super(name, filter, layout, ignoreExceptions);

		// start the messages expiration thread
		ThreadPools.statusPool.execute(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						break;
					}
					// if there is a message currently displayed and it's not an
					// error
					if (messageDisplayed != null
							&& !messageDisplayed.getLevel().isAtLeastAsSpecificAs(Level.ERROR))
					{
						// and it is expired
						if (lastMessageDisplayTime + LOG_MESSAGE_DURATION < new Date().getTime())
						{
							// clear the status bar
							ApplicationContext.queueCallback(new Runnable()
							{
								@Override
								public void run()
								{
									statusBarText.setText("");
								}
							}, true);
						}
					}
				}
			}
		});
	}

	@PluginFactory
	public static StatusBarAppender createAppender(@PluginAttribute("name") final String name,
			@PluginElement("Layout") Layout<? extends Serializable> layout,
			@PluginElement("Filter") Filter filter,
			@PluginAttribute("ignoreExceptions") final String ignore)
	{
		if (name == null)
		{
			LOGGER.error("No name provided for SMTPAppender");
			return null;
		}

		if (layout == null)
		{
			layout = HTMLLayout.createLayout(null, null, null, null, null, null);
		}
		final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);

		return new StatusBarAppender(name, filter, layout, ignoreExceptions);
	}

	@Override
	public void append(LogEvent event)
	{
		// stop if we didn't receive the UI information
		if (statusBarText == null || resources == null)
			return;

		display(event);
	}

	private String formatMessage(LogEvent event)
	{
		// retrieve the original message
		String key = event.getMessage().getFormattedMessage();

		// if no key, something bad happened
		if (key == null)
			key = "unknownMessage";

		// try to translate it
		String message = (String) resources.get(key);

		// if the translation failed, use the original message
		if (message == null)
			message = key;

		// return the shorten the message
		return ResourcesUtil.shorten(message, 80);
	}

	public boolean display(LogEvent event)
	{
		// if no message currently displayed
		if (messageDisplayed == null)
		{
			doDisplay(event);
			return true;
		}

		// if this is at least as important as what we currently display
		if (event.getLevel().isAtLeastAsSpecificAs(messageDisplayed.getLevel()))
		{
			doDisplay(event);
			return true;
		}

		// if current message expired
		if (lastMessageDisplayTime + LOG_MESSAGE_DURATION < new Date().getTime())
		{
			doDisplay(event);
			return true;
		}

		// the message is discarded
		return false;
	}

	public void doDisplay(final LogEvent event)
	{
		lastMessageDisplayTime = new Date().getTime();
		messageDisplayed = event;

		ApplicationContext.queueCallback(new Runnable()
		{
			@Override
			public void run()
			{
				String message = formatMessage(event);
				if (message != null)
				{
					statusBarText.setText(message);
					if (event.getLevel().isAtLeastAsSpecificAs(Level.ERROR))
						statusBarText.getStyles().put("color", "#ff0000");
					else
						statusBarText.getStyles().put("color", "#000000");
				}
			}
		}, false);
	}

	public static void setLabel(Label statusBarText)
	{
		StatusBarAppender.statusBarText = statusBarText;
	}

	public static void setResources(Resources resources)
	{
		StatusBarAppender.resources = resources;
	}
}
