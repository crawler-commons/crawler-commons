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

package crawlercommons.domains;

import java.net.URL;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routines to extract the PLD (paid-level domain, as per the IRLbot paper) from
 * a hostname or URL.
 * 
 */

public class PaidLevelDomain {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaidLevelDomain.class);

    private static final Pattern IPV4_ADDRESS_PATTERN = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");

    /**
     * Extract the PLD (paid-level domain) from the hostname. If the format
     * isn't recognized, the original hostname is returned.
     * 
     * @param hostname
     *            - hostname from URL, e.g. www.domain.com.it
     * @return - PLD, e.g. domain.com.it
     */

    public static String getPLD(String hostname) {
        // First, check for weird [HHHH:HH::H] IPv6 format.
        if (hostname.startsWith("[") && hostname.endsWith("]")) {
            return hostname;
        }

        // Check for ddd.ddd.ddd.ddd IPv4 format
        if (IPV4_ADDRESS_PATTERN.matcher(hostname).matches()) {
            return hostname;
        }

        // Now use support in EffectiveTldFinder
        String result = EffectiveTldFinder.getAssignedDomain(hostname, true, true);
        if (result == null) {
            LOGGER.debug("Hostname {} isn't a valid FQDN", hostname);
            return hostname;
        } else {
            return result;
        }
    }

    /**
     * Extract the PLD (paid-level domain) from the URL.
     * 
     * @param url
     *            - Valid URL, e.g. http://www.domain.com.it
     * @return - PLD e.g. domain.com.it
     */

    public static String getPLD(URL url) {
        return getPLD(url.getHost());
    } // getPLD
}
