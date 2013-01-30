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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.IDN;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Given a URL's hostname, there are determining the actual domain requires
 * knowledge of the various domain registrars and their assignment policies.
 * The best publicly available knowledge of this is maintained by the Mozilla
 * developers; this class uses their data file format. For more information, see
 * <ul>
 * <li><a href="http://wiki.mozilla.org/Gecko:Effective_TLD_Service">Effective TLD Service</a></li>
 * <li><a href="http://www.publicsuffix.org">Public Suffix</a></li>
 * </ul>
 *
 * This class just needs "effective_tld_names.dat" in the classpath. If you want
 * to configure it with other data, call EffectiveTldFinder.getInstance.initialize(is)
 * and have at it.
 */
public class EffectiveTldFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(EffectiveTldFinder.class);
    public static final String ETLD_DATA = "/effective_tld_names.dat";
    public static final String COMMENT = "//";
    public static final String DOT_REGEX = "\\.";
    public static final String EXCEPTION = "!";
    public static final String WILD_CARD = "*.";
    public static final char DOT = '.';
    private static EffectiveTldFinder instance = null;
    private Map<String, EffectiveTLD> domains = null;
    private boolean configured = false;

    /**
     * A singleton
     */
    private EffectiveTldFinder() {
        initialize(null);
    }
    
    public static EffectiveTldFinder getInstance() {
        if (null == instance) {
            instance = new EffectiveTldFinder();
        }
        return instance;
    }

    public boolean initialize(InputStream effective_tld_data_stream) {
        domains = new HashMap<String, EffectiveTLD>();
        try {
            if (null == effective_tld_data_stream && null != this.getClass().getResource(ETLD_DATA)) {
                effective_tld_data_stream = this.getClass().getResourceAsStream(ETLD_DATA);
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(effective_tld_data_stream));
            String line = null;
            while (null != (line = input.readLine())) {
                if (line.length() == 0 || (line.length() > 1  && line.startsWith(COMMENT))) {
                    continue;
                } else {
                    EffectiveTLD entry = new EffectiveTLD(line);
                    domains.put(entry.getDomain(), entry);
                }
            }
            configured = true;
        } catch (IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("EffectiveTldFinder configuration failed: ", e);
            }
            configured = false;
        }
        return configured;
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, EffectiveTLD> getEffectiveTLDs() {
        // The keys and values are immutables but we don't want the caller
        // changing the repertoire of our internal Map entries, so we clone
        return (Map<String, EffectiveTLD>) ((HashMap<String, EffectiveTLD>)getInstance().domains).clone();
    }
    
    /**
     * @param hostname
     * @return the Effective TLD
     */
    public static EffectiveTLD getEffectiveTLD(String hostname) {
        if (getInstance().domains.containsKey(hostname)) {
            return getInstance().domains.get(hostname);
        }
        String[] parts = hostname.split(DOT_REGEX);
        if (! getInstance().domains.containsKey(parts[parts.length-1])) {
            return null;
        }
        for (int i = 1; i < parts.length; i++) {
            String[] slice = Arrays.copyOfRange(parts, i, parts.length);
            String tryTld = join(slice);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(hostname + " [" + i + ".." + parts.length + "]: " + Arrays.toString(slice) + " => " + tryTld);
            }
            if (getInstance().domains.containsKey(tryTld)) {
                EffectiveTLD foundTld = getInstance().domains.get(tryTld);
                if (foundTld.isException() || ! foundTld.isWild()) {
                    return foundTld;
                }
                // wildcards create an open ETLD namespace
                slice = Arrays.copyOfRange(parts, i-1, parts.length);
                String retryTld = join(slice);
                foundTld = new EffectiveTLD(retryTld);
                return foundTld;
            }
        }
        return null;
    }
    
    /**
     * This method uses the effective TLD to determine which component of
     * a FQDN is the NIC-assigned domain name.
     * 
     * @param hostname
     * @return the NIC-assigned domain name
     */
    public static String getAssignedDomain(String hostname) {
        EffectiveTLD etld = getEffectiveTLD(hostname);
        if (null == etld || etld.getDomain() == hostname.toLowerCase()) {
            return hostname.toLowerCase();
        }
        String domain = hostname.replaceFirst(".*?([^.]+\\.)" + 
                etld.getDomain() + "$", "$1" + etld.getDomain());
        return domain;
    }
    
    public boolean isConfigured() {
        return configured;
    }

    private static String join(String[] ary) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ary.length; i++) {
            sb.append(ary[i]).append(DOT);
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public static class EffectiveTLD {

        private boolean exception = false;
        private boolean wild = false;
        private String domain = null;
        
        public EffectiveTLD(String line) {
            if (line.startsWith(EXCEPTION)) {
                exception = true;
                domain = line.substring(1, line.length());
            } else if (line.startsWith(WILD_CARD)) {
                wild = true;
                domain = line.substring(2, line.length());
            } else {
                domain = line;
            }
            domain = normalize_name(domain);
        }

        private String normalize_name(String name) {
            String[] parts = name.split(DOT_REGEX);
            if (parts.length < 2) {
                return name;
            }
            String[] ary = new String[parts.length];
            for (int i = 0; i < parts.length; i++) {
                ary[i] = asciiConvert(parts[i]);
            }
            return join(ary);
        }
        
        private String asciiConvert(String str) {
            if (isAscii(str)) {
                return str.toLowerCase();
            }
            return IDN.toASCII(str);
        }

        private boolean isAscii(String str) {
            char[] chars = str.toCharArray();
            for (char c: chars) {
                if (c > 127) {
                    return false;
                }
            }
            return true;
        }
        
        public String getDomain() {
            return domain;
        }

        public boolean isWild() {
            return wild;
        }
        
        public boolean isException() {
            return exception;
        }
        
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer("[");
            sb.append("domain=").append(domain).append(",");
            sb.append("wild=").append(wild).append(",");
            sb.append("exception=").append(exception).append("]");
            return sb.toString();
        }
    }
}
