package info.bonjean.beluga.gui.pivot;

import info.bonjean.beluga.configuration.BelugaConfiguration;
import info.bonjean.beluga.configuration.DNSProxy;

import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferencesUI extends TablePane implements Bindable
{
	private static final Logger log = LoggerFactory.getLogger(PreferencesUI.class);
	private final BelugaConfiguration configuration = BelugaConfiguration.getInstance();

	@BXML
	TextInput emailAddressInput;
	@BXML
	TextInput passwordInput;
	@BXML
	TextInput httpProxyHostInput;
	@BXML
	TextInput httpProxyPortInput;
	@BXML
	ListButton dnsProxyInput;
	@BXML
	PushButton submitButton;
	@BXML
	Label errorArea;

	public PreferencesUI()
	{
		Action.getNamedActions().put("submit", new AsyncAction(MainWindow.getInstance())
		{
			@Override
			public void asyncPerform(final Component source)
			{
				configuration.setUserName(emailAddressInput.getText());
				configuration.setPassword(passwordInput.getText());
				configuration.setProxyHost(httpProxyHostInput.getText());
				configuration.setProxyPort(httpProxyPortInput.getText());
				configuration.setDNSProxy(((DNSProxy) dnsProxyInput.getSelectedItem()).getId());
				configuration.store();

				log.info("Configuration successfully updated");
			}
		});
	}

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
		emailAddressInput.setText(configuration.getUserName());
		passwordInput.setText(configuration.getPassword());
		httpProxyHostInput.setText(configuration.getProxyHost());
		httpProxyPortInput.setText(configuration.getProxyPortStr());
		@SuppressWarnings("unchecked")
		List<Object> listData = (List<Object>) dnsProxyInput.getListData();
		for (DNSProxy dnsProxy : DNSProxy.values())
			listData.add(dnsProxy);
		dnsProxyInput.setSelectedItem(DNSProxy.get(configuration.getDNSProxy()));
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		if (enabled)
			submitButton.setAction("submit");
		else
			submitButton.setAction((Action) null);
		submitButton.setEnabled(enabled);
		emailAddressInput.setEnabled(enabled);
		passwordInput.setEnabled(enabled);
		httpProxyHostInput.setEnabled(enabled);
		httpProxyPortInput.setEnabled(enabled);
		dnsProxyInput.setEnabled(enabled);
	}
}
