package crawlercommons.fetcher;

import java.io.Serializable;

@SuppressWarnings("serial")
public class UserAgent implements Serializable {
	public static final String DEFAULT_BROWSER_VERSION = "Mozilla/5.0";
	
	private final String _agentName;
	private final String _emailAddress;
	private final String _webAddress;
	private final String _browserVersion;
	private final String _crawlerVersion;
	
	public UserAgent(String agentName, String emailAddress, String webAddress) {
		this(agentName, emailAddress, webAddress, DEFAULT_BROWSER_VERSION);
	}

	public UserAgent(String agentName, String emailAddress, String webAddress, String browserVersion) {
		this(agentName, emailAddress, webAddress, browserVersion, null);
	}
	
	public UserAgent(String agentName, String emailAddress, String webAddress,
			String browserVersion, String crawlerVersion) {
		_agentName = agentName;
		_emailAddress = emailAddress;
		_webAddress = webAddress;
		_browserVersion = browserVersion;
		_crawlerVersion = (crawlerVersion == null ? "" : "/" + crawlerVersion);
	}
	
	public String getAgentName() {
		return _agentName;
	}

	public String getUserAgentString() {
		// Mozilla/5.0 (compatible; mycrawler/1.0; +http://www.mydomain.com; mycrawler@mydomain.com)
		return String.format("%s (compatible; %s%s; +%s; %s)",
				_browserVersion, getAgentName(), _crawlerVersion, _webAddress, _emailAddress);
	}
}
