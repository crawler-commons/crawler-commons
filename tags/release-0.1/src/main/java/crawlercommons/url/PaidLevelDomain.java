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

package crawlercommons.url;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routines to extract the PLD (paid-level domain, as per the IRLbot paper) from a hostname or URL.
 *
 */

public class PaidLevelDomain {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaidLevelDomain.class);
    
    private static final String CC_TLDS =
        "ac ad ae af ag ai al am an ao aq ar as at au aw ax az ba bb bd be bf bg bh bi " +
        "bj bl bm bn bo br bs bt bv bw by bz ca cc cd cf cg ch ci ck cl cm cn co cr cu " +
        "cv cx cy cz de dj dk dm do dz ec ee eg eh er es et eu fi fj fk fm fo fr ga gb " +
        "gd ge gf gg gh gi gl gm gn gp gq gr gs gt gu gw gy hk hm hn hr ht hu id ie il " +
        "im in io iq ir is it je jm jo jp ke kg kh ki km kn kp kr kw ky kz la lb lc li " +
        "lk lr ls lt lu lv ly ma mc md me mf mg mh mk ml mm mn mo mp mq mr ms mt mu mv " +
        "mw mx my mz na nc ne nf ng ni nl no np nr nu nz om pa pe pf pg ph pk pl pm pn " +
        "pr ps pt pw py qa re ro rs ru rw sa sb sc sd se sg sh si sj sk sl sm sn so sr " +
        "st su sv sy sz tc td tf tg th tj tk tl tm tn to tp tr tt tv tw tz ua ug uk um " +
        "us uy uz va vc ve vg vi vn vu wf ws ye yt yu za zm zw";

    private static final String G_TLDS = 
        "aero arpa asia biz cat com coop edu gov info int jobs mil mobi museum name net " +
        "org pro tel";

    private static final Set<String> ccTLDs = new HashSet<String>(Arrays.asList(CC_TLDS.split(" ")));
    private static final Set<String> gTLDs = new HashSet<String>(Arrays.asList(G_TLDS.split(" ")));

    private static final Pattern IPV4_ADDRESS_PATTERN = Pattern.compile("(?:\\d{1,3}\\.){3}\\d{1,3}");

    /**
     * Extract the PLD (paid-level domain) from the hostname. If the format isn't recognized,
     * the original hostname is returned.
     * 
     * @param hostname - hostname from URL, e.g. www.domain.com.it
     * @return - PLD, e.g. domain.com.it
     */

    public static String getPLD(String hostname) {
        // First, check for weird [HHHH:HH::H] IPv6 format.
        if (hostname.startsWith("[") && hostname.endsWith("]")) {
            return hostname;
        }

        String[] subNames = hostname.split("\\.");
        int numPieces = subNames.length;
        if (numPieces <= 2) {
            return hostname;
        }

        // Check for ddd.ddd.ddd.ddd IPv4 format
        if ((numPieces == 4) && (IPV4_ADDRESS_PATTERN.matcher(hostname).matches())) {
            return hostname;
        }

        int firstHostPiece = 0;
        if (ccTLDs.contains(subNames[numPieces - 1].toLowerCase())) {
            // We have a country code at the end. See if the preceding piece is either
            // a two-letter name (country code or funky short gTLD), or one of the
            // "well-known" gTLDs.
            if (subNames[numPieces - 2].length() <= 2) {
                // Must be xxx.co.jp format
                firstHostPiece = numPieces - 3;
            } else if (gTLDs.contains(subNames[numPieces - 2].toLowerCase())) {
                // Must be xxx.com.mx format
                firstHostPiece = numPieces - 3;
            } else {
                // Must be xxx.it format
                firstHostPiece = numPieces - 2;
            }
        } else if (gTLDs.contains(subNames[numPieces - 1].toLowerCase())) {
            if (ccTLDs.contains(subNames[numPieces - 2].toLowerCase())) {
                // Must be xxx.de.com format
                firstHostPiece = numPieces - 3;
            } else {
                // Must be xxx.com format
                firstHostPiece = numPieces - 2;
            }
        } else {
            LOGGER.debug("Unknown format for hostname: " + hostname);
        }

        if (firstHostPiece == 0) {
            return hostname;
        } else {
            // Build the result from the firstHostPiece to numPices pieces.
            StringBuilder result = new StringBuilder();
            for (int i = firstHostPiece; i < numPieces; i++) {
                result.append(subNames[i]);
                result.append('.');
            }

            // Trim off final '.'
            return result.deleteCharAt(result.length() - 1).toString();
        }
    } // getPLD


    /**
     * Extract the PLD (paid-level domain) from the URL.
     * 
     * @param url - Valid URL, e.g. http://www.domain.com.it
     * @return - PLD e.g. domain.com.it
     */

    public static String getPLD(URL url) {
        return getPLD(url.getHost());
    } // getPLD
}
