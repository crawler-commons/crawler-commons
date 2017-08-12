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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.IDN;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To determine the actual domain name of a host name or URL requires knowledge
 * of the various domain registrars and their assignment policies. The best
 * publicly available knowledge base is the public suffix list maintained and
 * available at <a href="https://publicsuffix.org/">publicsuffix.org</a>. This
 * class implements the <a
 * href="https://publicsuffix.org/list/">publicsuffix.org ruleset</a> and uses a
 * copy of the public suffix list. data file format.
 * 
 * For more information, see
 * <ul>
 * <li><a href="http://www.publicsuffix.org">publicsuffix.org</a></li>
 * <li><a href="https://en.wikipedia.org/wiki/Public_Suffix_List">Wikipedia
 * article about the public suffix list</a></li>
 * <li>Mozilla's <a
 * href="http://wiki.mozilla.org/Gecko:Effective_TLD_Service">Effective TLD
 * Service</a>: for historic reasons the class name stems from the term
 * &quot;effective top-level domain&quot; (eTLD)</li>
 * </ul>
 * 
 * This class just needs "effective_tld_names.dat" in the classpath. If you want
 * to configure it with other data, call
 * {@link EffectiveTldFinder#getInstance() EffectiveTldFinder.getInstance()}
 * {@link EffectiveTldFinder#initialize(InputStream) .initialize(InputStream)}.
 * Updates to the public suffix list can be found here:
 * <ul>
 * <li><a href= "https://publicsuffix.org/list/public_suffix_list.dat"
 * >https://publicsuffix.org/list/public_suffix_list.dat</a></li>
 * <li><a href= "https://publicsuffix.org/list/effective_tld_names.dat"
 * >https://publicsuffix.org/list/effective_tld_names.dat</a> (same as
 * public_suffix_list.dat)</li>
 * <li><a href=
 * "https://raw.githubusercontent.com/publicsuffix/list/master/public_suffix_list.dat"
 * >https://raw.githubusercontent.com/publicsuffix/list/master/
 * public_suffix_list.dat</a></li>
 * </ul>
 * 
 * <h2>ICANN vs. Private Domains</h2>
 * 
 * The <a href="https://publicsuffix.org/list/">public suffix list (see section
 * &quot;divisions&quot;)</a> is subdivided into &quot;ICANN&quot; and
 * &quot;PRIVATE&quot; domains. To restrict the EffectiveTldFinder to
 * &quot;ICANN&quot; domains only, (re)initialize it by
 * {@link EffectiveTldFinder#getInstance() EffectiveTldFinder.getInstance()}
 * {@link EffectiveTldFinder#initialize(boolean) .initialize(true)} or
 * {@link EffectiveTldFinder#initialize(InputStream,boolean)
 * .initialize(InputStream, true)}. This will exclude the PRIVATE domain section
 * from the public suffix list.
 * 
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
        initialize(this.getClass().getResourceAsStream(ETLD_DATA));
    }

    /**
     * Get singleton instance of EffectiveTldFinder with default configuration.
     * 
     * @return singleton instance of EffectiveTldFinder
     */
    public static EffectiveTldFinder getInstance() {
        if (null == instance) {
            instance = new EffectiveTldFinder();
        }
        return instance;
    }

    /**
     * (Re)initialize EffectiveTldFinder with built-in public suffix list.
     * 
     * @param excludePrivateDomains
     *            whether to exclude the public suffixes listed in the PRIVATE
     *            domain section (opposed to &quot;ICANN&quot; domains)
     * @return true if (re)initialization was successful
     */
    public boolean initialize(boolean excludePrivateDomains) {
        return initialize(this.getClass().getResourceAsStream(ETLD_DATA), false);
    }

    /**
     * (Re)initialize EffectiveTldFinder with custom public suffix list.
     * 
     * @param effectiveTldDataStream
     *            content of public suffix list as input stream
     * @return true if (re)initialization was successful
     */
    public boolean initialize(InputStream effectiveTldDataStream) {
        return initialize(effectiveTldDataStream, false);
    }

    /**
     * (Re)initialize EffectiveTldFinder with custom public suffix list.
     * 
     * @param effectiveTldDataStream
     *            content of public suffix list as input stream
     * @param excludePrivateDomains
     *            whether to exclude the public suffixes listed in the PRIVATE
     *            domain section (opposed to &quot;ICANN&quot; domains)
     * @return true if (re)initialization was successful
     */
    public boolean initialize(InputStream effectiveTldDataStream, boolean excludePrivateDomains) {
        domains = new HashMap<>();
        boolean inPrivateDomainSection = false;
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(effectiveTldDataStream, StandardCharsets.UTF_8));
            String line = null;
            while (null != (line = input.readLine())) {
                if (line.length() == 0) {
                    continue;
                } else if (line.startsWith(COMMENT)) {
                    if (excludePrivateDomains) {
                        if (line.contains("===BEGIN PRIVATE DOMAINS===")) {
                            inPrivateDomainSection = true;
                        } else if (line.contains("===END PRIVATE DOMAINS===")) {
                            inPrivateDomainSection = false;
                        }
                    }
                    continue;
                } else if (excludePrivateDomains && inPrivateDomainSection) {
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
        return (Map<String, EffectiveTLD>) ((HashMap<String, EffectiveTLD>) getInstance().domains).clone();
    }

    /**
     * Get EffectiveTLD for host name using the singleton instance of
     * EffectiveTldFinder.
     * 
     * @param hostname
     *            the hostname for which to find the {@link EffectiveTLD}
     * @return the {@link EffectiveTLD}
     */
    public static EffectiveTLD getEffectiveTLD(String hostname) {
        if (getInstance().domains.containsKey(hostname)) {
            return getInstance().domains.get(hostname);
        }
        String[] parts = hostname.split(DOT_REGEX);
        for (int i = 1; i < parts.length; i++) {
            String[] slice = Arrays.copyOfRange(parts, i, parts.length);
            String tryTld = join(slice);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(hostname + " [" + i + ".." + parts.length + "]: " + Arrays.toString(slice) + " => " + tryTld);
            }
            if (getInstance().domains.containsKey(tryTld)) {
                EffectiveTLD foundTld = getInstance().domains.get(tryTld);
                if (foundTld.isException() || !foundTld.isWild()) {
                    return foundTld;
                }
                // wildcards create an open ETLD namespace
                slice = Arrays.copyOfRange(parts, i - 1, parts.length);
                String retryTld = join(slice);
                foundTld = new EffectiveTLD(retryTld);
                return foundTld;
            }
        }
        return null;
    }

    /**
     * This method uses the effective TLD to determine which component of a FQDN
     * is the NIC-assigned domain name.
     * 
     * @param hostname
     *            a string for which to obtain a NIC-assigned domain name
     * @return the NIC-assigned domain name
     */
    public static String getAssignedDomain(String hostname) {
        EffectiveTLD etld = getEffectiveTLD(hostname);
        if (null == etld || etld.getDomain().equalsIgnoreCase(hostname)) {
            return hostname.toLowerCase(Locale.ROOT);
        }
        if (etld.isException()) {
            return etld.domain;
        }
        return hostname.replaceFirst(".*?([^.]+\\.)" + etld.getDomain() + "$", "$1" + etld.getDomain());
    }

    public boolean isConfigured() {
        return configured;
    }

    private static String join(String[] ary) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ary.length; i++) {
            sb.append(ary[i]).append(DOT);
        }
        sb.deleteCharAt(sb.length() - 1);
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
            domain = normalizeName(domain);
        }

        private String normalizeName(String name) {
            String[] parts = name.split(DOT_REGEX);
            String[] ary = new String[parts.length];
            for (int i = 0; i < parts.length; i++) {
                ary[i] = asciiConvert(parts[i]);
            }
            return join(ary);
        }

        private String asciiConvert(String str) {
            if (isAscii(str)) {
                return str.toLowerCase(Locale.ROOT);
            }
            return IDN.toASCII(str);
        }

        private boolean isAscii(String str) {
            char[] chars = str.toCharArray();
            for (char c : chars) {
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
