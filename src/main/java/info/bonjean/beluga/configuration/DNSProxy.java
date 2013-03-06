package info.bonjean.beluga.configuration;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum DNSProxy
{
	NONE("", "None", "", ""), PROXY_DNS("proxydns", "Proxy DNS", "74.207.242.213", "50.116.28.138"), TUNLR("tunlr", "Tunlr", "142.54.177.158",
			"198.147.22.212");
	private String id;
	private String name;
	private String primaryServer;
	private String secondaryServer;
	private static final Map<String, DNSProxy> lookup = new HashMap<String, DNSProxy>();

	static
	{
		for (DNSProxy s : EnumSet.allOf(DNSProxy.class))
			lookup.put(s.getId(), s);
	}

	public static DNSProxy get(String id)
	{
		return lookup.get(id);
	}

	private DNSProxy(String id, String name, String primaryServer, String secondaryServer)
	{
		this.id = id;
		this.name = name;
		this.primaryServer = primaryServer;
		this.secondaryServer = secondaryServer;
	}

	public String getId()
	{
		return id;
	}

	public String getPrimaryServer()
	{
		return primaryServer;
	}

	public String getSecondaryServer()
	{
		return secondaryServer;
	}

	public String toString()
	{
		return name;
	}
}
