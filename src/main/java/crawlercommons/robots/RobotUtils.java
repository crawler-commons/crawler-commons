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

package crawlercommons.robots;

import java.net.URL;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.fetcher.BaseFetcher;
import crawlercommons.fetcher.FetchedResult;
import crawlercommons.fetcher.HttpFetchException;
import crawlercommons.fetcher.IOFetchException;
import crawlercommons.fetcher.RedirectFetchException;
import crawlercommons.fetcher.SimpleHttpFetcher;
import crawlercommons.fetcher.UserAgent;


public class RobotUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RobotUtils.class);
    
    // Some robots.txt files are > 64K, amazingly enough.
    private static final int MAX_ROBOTS_SIZE = 128 * 1024;

    // subdomain.domain.com can direct to domain.com, so if we're simultaneously fetching
    // a bunch of robots from subdomains that redirect, we'll exceed the default limit.
    private static final int MAX_CONNECTIONS_PER_HOST = 20;
    
    // Crank down default values when fetching robots.txt, as this should be super
    // fast to get back.
    private static final int ROBOTS_CONNECTION_TIMEOUT = 10 * 1000;
    private static final int ROBOTS_SOCKET_TIMEOUT = 10 * 1000;
    private static final int ROBOTS_RETRY_COUNT = 2;

    // TODO KKr - set up min response rate, use it with max size to calc max
    // time for valid download, use it for COMMAND_TIMEOUT
    
    // Amount of time we'll wait for pending tasks to finish up. This is roughly equal
    // to the max amount of time it might take to fetch a robots.txt file (excluding
    // download time, which we could add).
    // FUTURE KKr - add in time to do the download.
    private static final long MAX_FETCH_TIME = (ROBOTS_CONNECTION_TIMEOUT + ROBOTS_SOCKET_TIMEOUT) * ROBOTS_RETRY_COUNT;

    public static BaseFetcher createFetcher(BaseFetcher fetcher) {
        return createFetcher(fetcher.getUserAgent(), fetcher.getMaxThreads());
    }

    public static BaseFetcher createFetcher(UserAgent userAgent, int maxThreads) {
        SimpleHttpFetcher fetcher = new SimpleHttpFetcher(maxThreads, userAgent);
        fetcher.setDefaultMaxContentSize(MAX_ROBOTS_SIZE);
        fetcher.setMaxConnectionsPerHost(MAX_CONNECTIONS_PER_HOST);
        fetcher.setMaxRetryCount(ROBOTS_RETRY_COUNT);
        fetcher.setConnectionTimeout(ROBOTS_CONNECTION_TIMEOUT);
        fetcher.setSocketTimeout(ROBOTS_SOCKET_TIMEOUT);
        
        return fetcher;
    }
    
    public static long getMaxFetchTime() {
        return MAX_FETCH_TIME;
    }

    /**
     * Externally visible, static method for use in tools and for testing.
     * Fetch the indicated robots.txt file, parse it, and generate rules.
     * 
     * @param fetcher Fetcher for downloading robots.txt file
     * @param robotsUrl URL to robots.txt file
     * @return Robot rules
     */
    public static BaseRobotRules getRobotRules(BaseFetcher fetcher, BaseRobotsParser parser, URL robotsUrl) {
        
        try {
            String urlToFetch = robotsUrl.toExternalForm();
            FetchedResult result = fetcher.get(urlToFetch);

            // HACK! DANGER! Some sites will redirect the request to the top-level domain
            // page, without returning a 404. So look for a response which has a redirect,
            // and the fetched content is not plain text, and assume it's one of these...
            // which is the same as not having a robots.txt file.
            
            String contentType = result.getContentType();
            boolean isPlainText = (contentType != null) && (contentType.startsWith("text/plain"));
            if ((result.getNumRedirects() > 0) && !isPlainText) {
                return parser.failedFetch(HttpStatus.SC_GONE);
            }
            
            return parser.parseContent(urlToFetch, result.getContent(), result.getContentType(), 
                            fetcher.getUserAgent().getAgentName());
        } catch (HttpFetchException e) {
            return parser.failedFetch(e.getHttpStatus());
        } catch (IOFetchException e) {
            return parser.failedFetch(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } catch (RedirectFetchException e) {
            // Other sites will have circular redirects, so treat this as a missing robots.txt
            return parser.failedFetch(HttpStatus.SC_GONE);
        } catch (Exception e) {
            LOGGER.error("Unexpected exception fetching robots.txt: " + robotsUrl, e);
            return parser.failedFetch(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
