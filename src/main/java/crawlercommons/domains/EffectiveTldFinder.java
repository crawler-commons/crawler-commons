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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.IDN;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To determine the actual domain name of a host name or URL requires knowledge
 * of the various domain registrars and their assignment policies. The best
 * publicly available knowledge base is the public suffix list maintained and
 * available at <a href="https://publicsuffix.org/">publicsuffix.org</a>. This
 * class implements the
 * <a href="https://publicsuffix.org/list/">publicsuffix.org ruleset</a> and
 * uses a copy of the public suffix list.
 * 
 * For more information, see
 * <ul>
 * <li><a href="https://www.publicsuffix.org/">publicsuffix.org</a></li>
 * <li><a href="https://en.wikipedia.org/wiki/Public_Suffix_List">Wikipedia
 * article about the public suffix list</a></li>
 * <li>Mozilla's
 * <a href="https://wiki.mozilla.org/Gecko:Effective_TLD_Service">Effective TLD
 * Service</a>: for historic reasons the class name stems from the term
 * &quot;effective top-level domain&quot; (eTLD)</li>
 * </ul>
 * 
 * EffectiveTldFinder loads the public suffix list as file
 * "effective_tld_names.dat" from the Java classpath. Make sure your classpath
 * does not contain any other file with the same name, eg. an outdated list
 * shipped with a third party library. To force EffectiveTldFinder to load an
 * updated or modified public suffix list, call
 * {@link EffectiveTldFinder#getInstance()
 * EffectiveTldFinder.getInstance()}{@link EffectiveTldFinder#initialize(InputStream)
 * .initialize(InputStream)}. Updates to the public suffix list can be found
 * here:
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
 * &quot;ICANN&quot; domains only, pass &quot;true&quot; as flag
 * <code>excludePrivate</code> to
 * {@link EffectiveTldFinder#getAssignedDomain(String, boolean, boolean)} resp.
 * {@link EffectiveTldFinder#getEffectiveTLD(String, boolean)}. This will
 * exclude the eTLDs from the PRIVATE domain section of the public suffix list
 * while a domain or eTLD is matched.
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

    /**
     * Max. length in ASCII characters of a dot-separated segment in host names
     * (applies to domain names as well), cf.
     * https://tools.ietf.org/html/rfc1034#section-3.1 and
     * https://en.wikipedia.org/wiki/Hostname#Restrictions_on_valid_hostnames
     * 
     * Note: We only have to validate domain names and not the host names passed
     * as input. For domain names a verification of the segment length also
     * implies that the entire domain names stays in the limit of 253
     * characters. Wildcard suffixes only allow two additional segments (2*63+1
     * = 127 chars) and all wildcard suffixes are far away from reaching the
     * critical length of 126 characters.
     */
    public static final int MAX_DOMAIN_LENGTH_PART = 63;

    private static EffectiveTldFinder instance = null;
    private Map<String, EffectiveTLD> domains = null;
    private SuffixTrie<EffectiveTLD> domainTrie = new SuffixTrie<>();
    private boolean configured = false;

    /**
     * A singleton loading the public suffix list from the Java class path.
     */
    private EffectiveTldFinder() {
        URL publicSuffixList = this.getClass().getResource(ETLD_DATA);
        LOGGER.info("Loading public suffix list from class path: {}", publicSuffixList);
        try (InputStream is = publicSuffixList.openStream()) {
            initialize(is);
        } catch (IOException e) {
            LOGGER.error("Failed to load public suffix list {} from class path: {}", publicSuffixList, e);
        }
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
     * (Re)initialize EffectiveTldFinder with custom public suffix list.
     * 
     * @param effectiveTldDataStream
     *            content of public suffix list as input stream
     * @return true if (re)initialization was successful
     */
    public boolean initialize(InputStream effectiveTldDataStream) {
        domains = new HashMap<>();
        domainTrie = new SuffixTrie<>();
        boolean inPrivateDomainSection = false;
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(effectiveTldDataStream, StandardCharsets.UTF_8));
            String line = null;
            while (null != (line = input.readLine())) {
                if (line.trim().isEmpty()) {
                    continue;
                } else if (line.startsWith(COMMENT)) {
                    if (line.contains("===BEGIN PRIVATE DOMAINS===")) {
                        inPrivateDomainSection = true;
                    } else if (line.contains("===END PRIVATE DOMAINS===")) {
                        inPrivateDomainSection = false;
                    }
                    continue;
                } else {
                    EffectiveTLD entry = new EffectiveTLD(line, inPrivateDomainSection);
                    for (String var : entry.getNameVariants()) {
                        domains.put(var, entry);
                        domainTrie.put(var, entry);
                    }
                }
            }
            configured = true;
        } catch (IOException e) {
            LOGGER.error("EffectiveTldFinder configuration failed: ", e);
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
        return getEffectiveTLD(hostname, false);
    }

    /**
     * Get EffectiveTLD for host name using the singleton instance of
     * EffectiveTldFinder.
     * 
     * @param hostname
     *            the hostname for which to find the {@link EffectiveTLD}
     * @param excludePrivate
     *            do not return an effective TLD from the PRIVATE section,
     *            instead return the shorter eTLD not in the PRIVATE section
     * @return the {@link EffectiveTLD}
     */
    public static EffectiveTLD getEffectiveTLD(String hostname, boolean excludePrivate) {
        SuffixTrie.LookupResult<EffectiveTLD> res = findEffectiveTLD(hostname, excludePrivate);
        if (res == null) {
            return null;
        }
        return res.value;
    }

    /**
     * Find EffectiveTLD and offset in host name using the singleton instance of
     * EffectiveTldFinder.
     * 
     * @param hostname
     *            the hostname for which to find the {@link EffectiveTLD}
     * @param excludePrivate
     *            do not return an effective TLD from the PRIVATE section,
     *            instead return the shorter eTLD not in the PRIVATE section
     * @return the {@link EffectiveTLD} or null if none is found
     */
    private static SuffixTrie.LookupResult<EffectiveTLD> findEffectiveTLD(String hostname, boolean excludePrivate) {
        List<SuffixTrie.LookupResult<EffectiveTLD>> suffixes = getInstance().domainTrie.getSuffixes(hostname);
        for (int i = suffixes.size() - 1; i >= 0; i--) {
            SuffixTrie.LookupResult<EffectiveTLD> res = suffixes.get(i);
            int offset = res.offset;
            if (offset == 0 || DOT == hostname.charAt(offset - 1)) {
                EffectiveTLD foundTld = res.value;
                if (excludePrivate && foundTld.isPrivate) {
                    continue;
                }
                if (offset == 0 || foundTld.isException() || !foundTld.isWild()) {
                    return res;
                }

                // wildcards create an open ETLD namespace
                int wildcardOffset = hostname.lastIndexOf(DOT, offset - 2);
                String retryTld;
                if (wildcardOffset == -1) {
                    // no further dot-separated element found, take full host
                    // name
                    retryTld = hostname;
                } else {
                    retryTld = hostname.substring(wildcardOffset + 1);
                }

                try {
                    foundTld = new EffectiveTLD(retryTld, foundTld.isPrivate);
                } catch (IllegalArgumentException e) {
                    // retryTld contains forbidden characters
                    return null;
                }

                return new SuffixTrie.LookupResult<EffectiveTLD>(wildcardOffset + 1, foundTld);
            }
        }
        return null;
    }

    /**
     * This method uses the effective TLD to determine which component of a FQDN
     * is the NIC-assigned domain name (aka "Paid Level Domain").
     * 
     * @param hostname
     *            a string for which to obtain a NIC-assigned domain name
     * @return the NIC-assigned domain name or as fall-back the hostname if no
     *         FQDN with valid TLD is found
     */
    public static String getAssignedDomain(String hostname) {
        return getAssignedDomain(hostname, false, false);
    }

    /**
     * This method uses the effective TLD to determine which component of a FQDN
     * is the NIC-assigned domain name (aka "Paid Level Domain").
     * 
     * @param hostname
     *            a string for which to obtain a NIC-assigned domain name
     * @param strict
     *            do not return the hostname as fall-back if a FQDN with valid
     *            TLD cannot be determined
     * @return the NIC-assigned domain name, null if strict and no FQDN with
     *         valid TLD is found
     */
    public static String getAssignedDomain(String hostname, boolean strict) {
        return getAssignedDomain(hostname, strict, false);
    }

    /**
     * This method uses the effective TLD to determine which component of a FQDN
     * is the NIC-assigned domain name.
     * 
     * @param hostname
     *            a string for which to obtain a NIC-assigned domain name
     * @param strict
     *            do not return the hostname as fall-back if a FQDN with valid
     *            TLD cannot be determined
     * @param excludePrivate
     *            do not return a domain which is below an eTLD from the PRIVATE
     *            section, return the shorter domain which is below the
     *            &quot;ICANN&quot; registry suffix
     * @return the NIC-assigned domain name, null if strict and no FQDN with
     *         valid TLD is found
     */
    public static String getAssignedDomain(String hostname, boolean strict, boolean excludePrivate) {
        hostname = hostname.toLowerCase(Locale.ROOT);
        SuffixTrie.LookupResult<EffectiveTLD> res = findEffectiveTLD(hostname, excludePrivate);
        if (res == null) {
            return (strict ? null : hostname);
        }
        EffectiveTLD etld = res.value;
        if (etld.isException()) {
            return etld.domain;
        }
        if (res.offset == 0) {
            // found eTLD covering entire hostname:
            // if strict: hostname cannot be an eTLD (except if it's an
            // exception which is already checked)
            return (strict ? null : hostname);
        }
        // clip hostname one dot-separated element before eTLD
        int etldStartPos = res.offset - 1;
        if (hostname.charAt(etldStartPos) != DOT) {
            // should not happen: no dot before TLD
            LOGGER.debug("No dot before eTLD {} in {}", hostname.substring(res.offset), hostname);
            return (strict ? null : hostname);
        }
        int start = 0;
        int pos;
        while ((pos = hostname.indexOf(DOT, start)) != -1) {
            if (pos == start) {
                // there must be at least one character between two dots
                LOGGER.debug("Two immediately consecutive dots in hostname: {}", hostname);
                return (strict ? null : hostname);
            }
            if (pos >= etldStartPos)
                break;
            start = pos + 1;
        }
        String domainSegment = hostname.substring(start, etldStartPos);
        if (!EffectiveTLD.isAscii(domainSegment)) {
            try {
                IDN.toASCII(domainSegment);
            } catch (IllegalArgumentException e) {
                // not a valid IDN segment,
                // includes check for max. length (63 chars)
                return (strict ? null : hostname);
            }
        } else if (strict) {
            // (strict mode) check for max. length of segment (63 chars)
            if (domainSegment.length() > MAX_DOMAIN_LENGTH_PART) {
                return null;
            }
        }
        return hostname.substring(start);
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

    public static void help() {
        LOGGER.error("EffectiveTldFinder [-etld] [-strict] [-excludePrivate]");
        LOGGER.error("  get domains or public suffixes for host names");
        LOGGER.error("Options:");
        LOGGER.error("  -etld");
        LOGGER.error("       change mode: return public suffix (eTLD)");
        LOGGER.error("  -strict");
        LOGGER.error("       return null if no valid suffix/TLD is found");
        LOGGER.error("  -excludePrivate");
        LOGGER.error("       do not match suffixes from the private section of the public suffix list");
        LOGGER.error("Input is read from stdin, output on stdout: host \\t domain/eTLD");
    }

    public static void main(String[] args) throws IOException {
        boolean modeEtld = false;
        boolean strict = false;
        boolean excludePrivate = false;
        for (String arg : args) {
            switch (arg) {
                case "-etld":
                modeEtld = true;
                    break;

                case "-strict":
                strict = true;
                    break;

                case "-excludePrivate":
                excludePrivate = true;
                    break;

                case "-h":
                case "-?":
                case "-help":
                case "--help":
                help();
                System.exit(0);

                default:
                LOGGER.error("Unknown argument: {}", arg);
                help();
                System.exit(1);
            }
        }

        String line, domain;
        EffectiveTLD etld;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, UTF_8));
        while ((line = in.readLine()) != null) {
            if (modeEtld) {
                etld = EffectiveTldFinder.getEffectiveTLD(line, excludePrivate);
                System.out.println(line + '\t' + etld);
            } else {
                domain = EffectiveTldFinder.getAssignedDomain(line, strict, excludePrivate);
                System.out.println(line + '\t' + domain);
            }
        }
    }

    /**
     * EffectiveTLD objects hold one line of the public suffix list:
     * <ul>
     * <li>the suffix (<code>com</code>, <code>co.uk</code>, etc.)</li>
     * <li>for IDN suffixes: both the ASCII and IDN variant
     * (<code>xn--p1ai</code> and <code>рф</code>)</li>
     * <li>and the properties required to parse host/domain names given in the
     * public suffix list (wildcard suffix, exception, in private domain
     * section)</li>
     * </ul>
     */
    public static class EffectiveTLD {

        private boolean exception = false;
        private boolean wild = false;
        private boolean isPrivate = false;
        private String domain = null;
        private String idn = null;

        /**
         * Parse one non-empty, non-comment line in the public suffix list and
         * hold the public suffix and its properties in the created object.
         * 
         * @param line
         *            non-empty, non-comment line in the public suffix list
         * @param isPrivateDomain
         *            whether line is in the section of &quot;PRIVATE
         *            DOMAINS&quot; of the public suffix list
         * @throws IllegalArgumentException
         *             if the input line contains non-ASCII Unicode characters
         *             prohibited in IDNs, cf. {@link IDN#toASCII(String)}
         */
        public EffectiveTLD(String line, boolean isPrivateDomain) throws IllegalArgumentException {
            if (line.startsWith(EXCEPTION)) {
                exception = true;
                domain = line.substring(EXCEPTION.length(), line.length());
            } else if (line.startsWith(WILD_CARD)) {
                wild = true;
                domain = line.substring(WILD_CARD.length(), line.length());
            } else {
                domain = line;
            }

            String norm = normalizeName(domain);
            if (!norm.equals(domain)) {
                idn = domain;
                domain = norm;
            }
            isPrivate = isPrivateDomain;
        }

        /**
         * Normalize a domain name: convert characters into to lowercase and
         * encode dot-separated segments containing non-ASCII characters. Cf.
         * {@link #asciiConvert(String)} and {@link IDN#toASCII(String)}
         * 
         * @param str
         *            domain name segment
         * @return normalized domain name containing only ASCII characters
         * @throws IllegalArgumentException
         *             if the input contains prohibited characters
         */
        private String normalizeName(String name) throws IllegalArgumentException {
            String[] parts = name.split(DOT_REGEX);
            String[] ary = new String[parts.length];
            for (int i = 0; i < parts.length; i++) {
                ary[i] = asciiConvert(parts[i]);
            }
            return join(ary);
        }

        /**
         * Generate name variants caused by Internationalized Domain Names:
         * every IDN part of a eTLD can be replaced by its punycoded ASCII
         * variant. For two-part IDN eTLDs this will generate 4 variants.
         * 
         * @return set of variant names
         */
        public Set<String> getNameVariants() {
            Set<String> res = new HashSet<>();
            if (idn == null) {
                res.add(domain);
                return res;
            }
            String[] parts = idn.split(DOT_REGEX);
            String[] var = new String[parts.length];
            for (int i = 0; i < parts.length; i++) {
                if (!isAscii(parts[i])) {
                    var[i] = IDN.toASCII(parts[i]);
                }
            }
            for (int i = 0; i < parts.length; i++) {
                Set<String> r = new HashSet<>();
                if (res.size() > 0) {
                    for (String p : res) {
                        r.add(p + DOT + parts[i]);
                    }
                } else {
                    r.add(parts[i]);
                }
                if (var[i] != null && !var[i].equals(parts[i])) {
                    if (res.size() > 0) {
                        for (String p : res) {
                            r.add(p + DOT + var[i]);
                        }
                    } else {
                        r.add(var[i]);
                    }
                }
                res = r;
            }
            return res;
        }

        /**
         * Converts a single domain name segment (separated by dots) to ASCII if
         * it contains non-ASCII character, cf. {@link IDN#toASCII(String)}.
         * 
         * @param str
         *            domain name segment
         * @return ASCII "Punycode" representation of the domain name segment
         * @throws IllegalArgumentException
         *             if the input contains prohibited characters
         */
        private static String asciiConvert(String str) throws IllegalArgumentException {
            if (isAscii(str)) {
                return str.toLowerCase(Locale.ROOT);
            }
            return IDN.toASCII(str);
        }

        private static boolean isAscii(String str) {
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
            sb.append("exception=").append(exception).append(",");
            sb.append("private=").append(isPrivate).append("]");
            return sb.toString();
        }
    }
}
