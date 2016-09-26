/**
 * Copyright 2016 Crawler-Commons
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package crawlercommons.fetcher.http;

import java.io.Serializable;
import java.util.Locale;

import crawlercommons.CrawlerCommons;

/**
 * User Agent enables us to describe characteristics of any Crawler Commons
 * agent. There are a number of constructor options to describe the following:
 * <ol>
 * <li><tt>_agentName</tt>: Primary agent name.</li>
 * <li><tt>_emailAddress</tt>: The agent owners email address.</li>
 * <li><tt>_webAddress</tt>: A web site/address representing the agent owner.</li>
 * <li><tt>_browserVersion</tt>: Broswer version used for compatibility.</li>
 * <li><tt>_crawlerVersion</tt>: Version of the user agents personal crawler. If
 * this is not set, it defaults to the crawler commons maven artifact version.</li>
 * </ol>
 * 
 * @deprecated As of release 0.6. We recommend directly using Apache HttpClient,
 *             async-http-client, or any other robust, industrial-strength HTTP
 *             clients.
 * 
 */
@Deprecated
@SuppressWarnings("serial")
public class UserAgent implements Serializable {

    public static final String DEFAULT_BROWSER_VERSION = "Mozilla/5.0";
    public static final String DEFAULT_CRAWLER_VERSION = CrawlerCommons.getVersion();

    private final String agentName;
    private final String emailAddress;
    private final String webAddress;
    private final String browserVersion;
    private final String crawlerConfiguration;

    /**
     * Set user agent characteristics
     * 
     * @param agentName an agent name string to associate with the crawler
     * @param emailAddress an agent email address string to associate with the crawler
     * @param webAddress a Web address string to associate with the crawler
     */
    public UserAgent(String agentName, String emailAddress, String webAddress) {
        this(agentName, emailAddress, webAddress, DEFAULT_BROWSER_VERSION);
    }

    /**
     * Set user agent characteristics
     * 
     * @param agentName an agent name string to associate with the crawler
     * @param emailAddress an agent email address string to associate with the crawler
     * @param webAddress a Web address string to associate with the crawler
     * @param browserVersion a browser version to mimic
     */
    public UserAgent(String agentName, String emailAddress, String webAddress, String browserVersion) {
        this(agentName, emailAddress, webAddress, browserVersion, DEFAULT_CRAWLER_VERSION);
    }

    /**
     * Set user agent characteristics
     * 
     * @param agentName an agent name string to associate with the crawler
     * @param emailAddress an agent email address string to associate with the crawler
     * @param webAddress a Web address string to associate with the crawler
     * @param browserVersion a browser version to mimic
     * @param crawlerVersion the version of your crawler/crawl agent
     */
    public UserAgent(String agentName, String emailAddress, String webAddress, String browserVersion, String crawlerVersion) {
        this.agentName = agentName;
        this.emailAddress = emailAddress;
        this.webAddress = webAddress;
        this.browserVersion = browserVersion;
        this.crawlerConfiguration = crawlerVersion == null ? "" : "/" + crawlerVersion;
    }

    /**
     * Obtain the just the user agent name
     * 
     * @return User Agent name (String)
     */
    public String getAgentName() {
        return agentName;
    }

    /**
     * Obtain a String representing the user agent characteristics.
     * 
     * @return User Agent String
     */
    public String getUserAgentString() {
        // Mozilla/5.0 (compatible; mycrawler/1.0; +http://www.mydomain.com; mycrawler@mydomain.com)
        return String.format(Locale.getDefault(), "%s (compatible; %s%s; +%s; %s)", browserVersion, getAgentName(), crawlerConfiguration, webAddress, emailAddress);
    }
}
