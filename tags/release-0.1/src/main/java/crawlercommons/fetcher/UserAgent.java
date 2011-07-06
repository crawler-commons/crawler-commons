/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
