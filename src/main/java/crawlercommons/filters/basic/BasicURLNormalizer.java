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

package crawlercommons.filters.basic;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.filters.URLFilter;

/**
 * Converts URLs to a
 * <a href="https://en.wikipedia.org/wiki/URI_normalization">normal form</a>.
 * 
 * <ul>
 * <li>remove dot segments in path: <code>/./</code> or <code>/../</code></li>
 * <li>remove default ports, e.g. 80 for protocol <code>http://</code></li>
 * <li>normalize <a href=
 * "https://en.wikipedia.org/wiki/Percent-encoding#Percent-encoding_in_a_URI">
 * percent-encoding</a> in URL paths</li>
 * <li>sort URL query parameters</li>
 * <li>optionally:
 * <ul>
 * <li>remove a configured set of URL query parameters, see
 * {@link Builder#queryParamsToRemove}
 * <li>normalize internationalized domain names (IDNs), see
 * {@link Builder#idnNormalization(IdnNormalization)}</li>
 * </ul>
 * </li>
 * </ul>
 */
public class BasicURLNormalizer extends URLFilter {
    public static final Logger LOG = LoggerFactory.getLogger(BasicURLNormalizer.class);

    /**
     * Pattern to detect whether a URL path could be normalized. Contains one of
     * /. or ./ /.. or ../ //
     */
    private final static Pattern hasNormalizablePathPattern = Pattern.compile("/[./]|[.]/");

    /**
     * find URL encoded parts of the URL
     */
    private final static Pattern unescapeRulePattern = Pattern.compile("%([0-9A-Fa-f]{2})");

    /**
     * Match URLs starting with a valid scheme, see
     * https://tools.ietf.org/html/rfc2396#section-3.1
     */
    private final static Pattern hasSchemePattern = Pattern.compile("^[A-Za-z][A-Za-z0-9+.-]*:/");

    /** look-up table for characters which should not be escaped in URL paths */
    private final static boolean[] unescapedCharacters = new boolean[128];

    static {
        for (int c = 0; c < 128; c++) {
            /*
             * https://tools.ietf.org/html/rfc3986#section-2.2 For consistency,
             * percent-encoded octets in the ranges of ALPHA (%41-%5A and
             * %61-%7A), DIGIT (%30-%39), hyphen (%2D), period (%2E), underscore
             * (%5F), or tilde (%7E) should not be created by URI producers and,
             * when found in a URI, should be decoded to their corresponding
             * unreserved characters by URI normalizers.
             */
            if (isAlphaNumeric(c) || c == 0x2D || c == 0x2E || c == 0x5F || c == 0x7E) {
                unescapedCharacters[c] = true;
            } else {
                unescapedCharacters[c] = false;
            }
        }
    }

    /**
     * look-up table for characters which should always be escaped in URL path
     * and query, cf. https://url.spec.whatwg.org/#percent-encoded-bytes and
     * https://en.wikipedia.org/wiki/Percent-encoding
     */
    private final static boolean[] escapedCharacters = new boolean[128];
    static {
        for (int c = 0; c < 128; c++) {
            if (unescapedCharacters[c]) {
                escapedCharacters[c] = false;
            } else if (c <= 0x1F // control characters
                            || c == 0x20 // space
                            || c == 0x22 // "
                            || c == 0x23 // #
                            || c == 0x3C // <
                            || c == 0x3E // >
                            || c == 0x5B // [
                            || c == 0x5D // ]
                            || c == 0x5E // ^
                            || c == 0x60 // `
                            || c == 0x7B // {
                            || c == 0x7C // |
                            || c == 0x7D // }
                            || c == 0x7F // DEL
            ) {
                escapedCharacters[c] = true;
            } else {
                LOG.debug("Character {} ({}) not handled as escaped or unescaped", c, (char) c);
            }
        }
    }

    private static boolean isAlphaNumeric(int c) {
        return (0x41 <= c && c <= 0x5A) || (0x61 <= c && c <= 0x7A) || (0x30 <= c && c <= 0x39);
    }

    private static boolean isHexCharacter(int c) {
        return (0x41 <= c && c <= 0x46) || (0x61 <= c && c <= 0x66) || (0x30 <= c && c <= 0x39);
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

    private final Set<String> queryParamsToRemove;
    private final IdnNormalization idnNormalization;


    public BasicURLNormalizer() {
        this(new Builder());
    }

    public BasicURLNormalizer(Builder builder) {
        this.queryParamsToRemove = builder.queryParamsToRemove;
        this.idnNormalization = builder.idnNormalization;
    }

    @Override
    public String filter(String urlString) {

        if ("".equals(urlString)) // permit empty
            return urlString;

        urlString = urlString.trim(); // remove extra spaces

        // remove fragment before escaping, so # sign is not escaped
        int fragmentPos = urlString.indexOf('#');
        if (urlString.indexOf('#') >= 0) {
            urlString = urlString.substring(0, fragmentPos);
        }

        // escape to ensure URL does not contain illegal characters
        urlString = escapePath(urlString);

        URL url = parseStringToURL(urlString);
        if (url == null) {
            LOG.debug("Malformed URL {}", urlString);
            return null;
        }

        String protocol = url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();
        String file = url.getFile();

        boolean changed = false;
        boolean normalizePath = false;

        if (!urlString.startsWith(protocol)) // protocol was lowercased
            changed = true;

        if ("http".equals(protocol) || "https".equals(protocol) || "ftp".equals(protocol)) {

            if (host != null && url.getAuthority() != null) {
                String newHost;
                try {
                    newHost = normalizeHostName(host);
                } catch (IllegalArgumentException | IndexOutOfBoundsException | UnsupportedEncodingException e) {
                    LOG.info("Invalid hostname: {}", host, e);
                    return null;
                }
                if (!host.equals(newHost)) {
                    host = newHost;
                    changed = true;
                } else if (!url.getAuthority().equals(newHost)) {
                    // authority (http://<...>/) contains other elements (port,
                    // user, etc.) which will likely cause a change if left away
                    changed = true;
                }
            } else {
                // no host or authority: recompose the URL from components
                changed = true;
            }

            if (port == url.getDefaultPort()) { // uses default port
                port = -1; // so don't specify it
                changed = true;
            }

            normalizePath = true;
            if (file == null || "".equals(file)) { // add a slash
                file = "/";
                changed = true;
                normalizePath = false; // no further path normalization required
            } else if (!file.startsWith("/")) {
                file = "/" + file;
                changed = true;
                normalizePath = false; // no further path normalization required
            }

            if (url.getRef() != null) { // remove the ref
                changed = true;
            }
        } else if (protocol.equals("file")) {
            normalizePath = true;
        }

        // properly encode characters in path/file using percent-encoding
        String file2 = normalizeUrlFile(file);

        if (!file.equals(file2)) {
            changed = true;
            file = file2;
        }

        if (normalizePath) {
            // check for unnecessary use of "/../", "/./", and "//"
            try {
                if (changed) {
                    String tempUrl = protocol + "://" + (host == null ? "" : host) + (port == -1 ? "" : ":" + port) + file;
                    url = new URI(tempUrl).toURL();
                }
                file2 = getFileWithNormalizedPath(url);
                if (!file.equals(file2)) {
                    changed = true;
                    file = file2;
                }
            } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
                LOG.info("Malformed URL {}://{}{}{}", protocol, host, (port == -1 ? "" : ":" + port), file);
                return null;
            }
        }

        if (changed)
            try {
                String tempUrl = protocol + "://" + (host == null ? "" : host) + (port == -1 ? "" : ":" + port) + file;
                urlString = new URI(tempUrl).toURL().toString();
            } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
                LOG.info("Malformed URL {}://{}{}{}", protocol, host, (port == -1 ? "" : ":" + port), file);
                return null;
            }

        return urlString;
    }

    /**
     * Tries to parse the given string into a java.net.URL object.
     *
     * @param urlString a string which possibly contains a URL
     * @return a URL object or null if an exception occurs.
     */
    private static URL parseStringToURL(String urlString) {
        URL url = null;
        try {
            url = new URI(urlString).toURL();
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            if (!hasSchemePattern.matcher(urlString).find()) {
                // no protocol/scheme : try to prefix http://
                try {
                    url = new URI("http://" + urlString).toURL();
                } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e1) {
                }
            }
        }
        return url;
    }

    /**
     * Parses the URL file and applies normalizations to the path and query components.
     *
     * @param file the URL file (as in java.net.URL.getFile()).
     * @return a normalized URL file
     */
    private String normalizeUrlFile(String file) {
        // find the beginning of the query parameters
        int endPathIdx = file.indexOf('?');
        if (endPathIdx == -1) {
            // no query parameters, just properly normalize the path
            return unescapePath(file);
        }

        int queryStartIdx = endPathIdx + 1;
        if (queryStartIdx >= file.length()) {
            // question mark was the last char in the file, so the query parameters
            // string is empty. we can just remove the question mark and properly
            // normalize the path.
            final String path = file.substring(0, file.length() - 1);
            return unescapePath(path);
        }

        file = unescapePath(file);

        List<NameValuePair> pairs =
                parseQueryParameters(file, queryStartIdx, queryParamsToRemove);

        StringBuilder normalizedFile = new StringBuilder();
        String path = file.substring(0, endPathIdx);
        if (!path.isBlank()) {
            normalizedFile.append(path);
        }

        // reconstruct query parameters in sorted order
        if (!pairs.isEmpty()) {
            pairs.sort(NameValuePair.NAME_COMPARATOR);
            normalizedFile
                    .append('?')
                    .append(formatQueryParameters(pairs));
        }

        return normalizedFile.toString();
    }

    /**
     * Receives the URL query string and parses it into a list of name-value pairs. Optionally,
     * allows to remove query parameters.
     *
     * @param s a String containing the URL file (as per java.net.URL.getFile(), i.e., the path + query +
     * fragment)
     * @param queryStartIdx the index position of the query part in the string {@code s}.
     * @param queryElementsToRemove a set of query parameter names to be ignored while parsing the
     * query parameters.
     */
    public static List<NameValuePair> parseQueryParameters(final String s, final int queryStartIdx,
                                                           final Set<String> queryElementsToRemove) {

        if (s == null || s.isEmpty()) {
            return Collections.emptyList();
        }

        final List<NameValuePair> list = new ArrayList<>();

        int nameBeginIdx;
        String name;
        int valueBeginIdx;
        String value;

        char c = s.charAt(queryStartIdx);
        for (int i = queryStartIdx, len = s.length(); i < len; i++) {

            // parse query parameter name
            nameBeginIdx = i;
            while (i < len) {
                c = s.charAt(i);
                if (isNameEnd(c)) {
                    break;
                }
                i++;
            }
            name = s.substring(nameBeginIdx, i);

            // parse query parameter value
            value = null;
            if (i < len && c == '=') {
                i++;
                valueBeginIdx = i;
                while (i < len) {
                    c = s.charAt(i);
                    if (isValueEnd(c)) {
                        break;
                    }
                    i++;
                }
                if (valueBeginIdx < i) {
                    value = s.substring(valueBeginIdx, i);
                }
            }

            if (!name.isEmpty()) {
                if (queryElementsToRemove != null && !queryElementsToRemove.contains(name)) {
                    list.add(new NameValuePair(name, value));
                }
            }
        }
        return list;
    }

    /**
     * Checks if the given char is a delimiter of a query parameter value.
     *
     * @param c the char to be checked
     * @return true if the char is a delimiter, false otherwise.
     */
    private static boolean isValueEnd(final char c) {
        return c == '&';
    }

    /**
     * Checks if the given char is a delimiter of a query parameter name.
     *
     * @param c the char to be checked
     * @return true if the char is a delimiter, false otherwise.
     */
    private static boolean isNameEnd(final char c) {
        return c == '=' || c == '&';
    }

    /**
     * Formats a list of query parameter name-value pairs into a query parameter string.
     *
     * @param parameters the query parameter name-value pairs
     * @return a URL query string
     */
    public static String formatQueryParameters(final List<NameValuePair> parameters) {
        final StringBuilder result = new StringBuilder();
        for (final NameValuePair parameter : parameters) {
            if (result.length() > 0) {
                result.append('&');
            }
            result.append(parameter.getName());
            final String value = parameter.getValue();
            if (value != null) {
                result.append('=');
                result.append(value);
            }
        }
        return result.toString();
    }

    /**
     * Represents the name-value pairs of each URL query parameter.
     */
    private static class NameValuePair {

        protected final String name;
        protected final String value;

        public NameValuePair(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public final static Comparator<NameValuePair> NAME_COMPARATOR =
            Comparator.comparing(NameValuePair::getName);

    }

    private String getFileWithNormalizedPath(URL url) throws MalformedURLException {
        String file;

        if (hasNormalizablePathPattern.matcher(url.getPath()).find()) {
            // only normalize the path if there is something to normalize
            // to avoid needless work
            try {
                file = url.toURI().normalize().toURL().getFile();
                // URI.normalize() does not normalize leading dot segments,
                // see also http://tools.ietf.org/html/rfc3986#section-5.2.4
                int start = 0;
                while (file.startsWith("/..", start) && ((start + 3) == file.length() || file.charAt(3) == '/')) {
                    start += 3;
                }
                if (start > 0) {
                    file = file.substring(start);
                }
            } catch (URISyntaxException e) {
                file = url.getFile();
            }
        } else {
            file = url.getFile();
        }

        // if path is empty return a single slash
        if (file.isEmpty()) {
            file = "/";
        } else if (!file.startsWith("/")) {
            file = "/" + file;
        }

        return file;
    }

    /**
     * Remove % encoding from path segment in URL for characters which should be
     * unescaped according to <a
     * href="https://tools.ietf.org/html/rfc3986#section-2.2">RFC3986</a>.
     */
    public static String unescapePath(String path) {
        StringBuilder sb = new StringBuilder();

        Matcher matcher = unescapeRulePattern.matcher(path);

        int end = -1;
        int letter;

        // Traverse over all encoded groups
        while (matcher.find()) {
            // Append everything up to this group
            sb.append(path, end + 1, matcher.start());

            // Get the integer representation of this hexadecimal encoded
            // character
            letter = Integer.valueOf(matcher.group().substring(1), 16);

            if (letter < 128 && unescapedCharacters[letter]) {
                // character should be unescaped in URLs
                sb.append(Character.valueOf((char) letter));
            } else {
                // Append the encoded character as uppercase
                sb.append(matcher.group().toUpperCase(Locale.ROOT));
            }

            end = matcher.start() + 2;
        }

        letter = path.length();

        // Append the rest if there's anything
        if (end <= letter - 1) {
            sb.append(path, end + 1, letter);
        }

        return sb.toString();
    }

    /**
     * Convert path segment of URL from Unicode to UTF-8 and escape all
     * characters which should be escaped according to <a
     * href="https://tools.ietf.org/html/rfc3986#section-2.2">RFC3986</a>.
     */
    public static String escapePath(String path) {
        return escapePath(path, null);
    }

    public static String escapePath(String path, boolean[] extraEscapedBytes) {
        StringBuilder sb = new StringBuilder(path.length());

        // Traverse over all bytes in this URL
        byte[] bytes = path.getBytes(UTF_8);
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            // Is this a control character?
            if (b < 0 || escapedCharacters[b] || (extraEscapedBytes != null && extraEscapedBytes[b])) {
                // Start escape sequence
                sb.append('%');

                // Get this byte's hexadecimal representation
                String hex = Integer.toHexString(b & 0xFF).toUpperCase(Locale.ROOT);

                // Do we need to prepend a zero?
                if (hex.length() % 2 != 0) {
                    sb.append('0');
                    sb.append(hex);
                } else {
                    // No, append this hexadecimal representation
                    sb.append(hex);
                }
            } else if (b == 0x25) {
                // percent sign (%): read-ahead to check whether a valid escape
                // sequence
                if ((i + 2) >= bytes.length) {
                    // need at least two more characters
                    sb.append("%25");
                } else {
                    byte e1 = bytes[i + 1];
                    byte e2 = bytes[i + 2];
                    if (isHexCharacter(e1) && isHexCharacter(e2)) {
                        // valid percent encoding, output and fast-forward
                        i += 2;
                        sb.append((char) b);
                        sb.append((char) e1);
                        sb.append((char) e2);
                    } else {
                        sb.append("%25");
                    }
                }
            } else {
                // No, just append this character as-is
                sb.append((char) b);
            }
        }

        return sb.toString();
    }

    private String normalizeHostName(String host) throws IllegalArgumentException, IndexOutOfBoundsException, UnsupportedEncodingException {

        /* 1. unescape percent-encoded characters in host name */
        if (host.indexOf('%') != -1) {
            /*
             * throws IllegalArgumentException on illegal percent-encoded
             * sequences
             */
            host = URLDecoder.decode(host, UTF_8.toString());
        }

        /* 2. lowercase host name */
        host = host.toLowerCase(Locale.ROOT);

        /*
         * 3. convert between Unicode and ASCII forms for Internationalized
         * Domain Names (IDNs)
         */
        if (this.idnNormalization == IdnNormalization.PUNYCODE && !isAscii(host)) {
            /*
             * IllegalArgumentException: thrown if the input string contains
             * non-convertible Unicode codepoints
             * 
             * IndexOutOfBoundsException: thrown (undocumented) if one "label"
             * (non-ASCII dot-separated segment) is longer than 256 characters,
             * cf. https://bugs.openjdk.java.net/browse/JDK-6806873
             */
            host = IDN.toASCII(host);
        } else if (this.idnNormalization == IdnNormalization.UNICODE && host.contains("xn--")) {
            host = IDN.toUnicode(host);
        }

        /* 4. trim a trailing dot */
        if (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }

        return host;
    }

    /**
     * Create a new builder object for creating a customized {@link BasicURLNormalizer} object.
     *
     * @return a {@link Builder} ready to use
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    public enum IdnNormalization {
        NONE,
        PUNYCODE,
        UNICODE
    }

    /**
     * A builder class for the {@link BasicURLNormalizer}.
     */
    public static class Builder {

        public IdnNormalization idnNormalization = IdnNormalization.PUNYCODE;
        Set<String> queryParamsToRemove = new TreeSet<>();

        private Builder() {
        }

        /**
         * A collection of names of query parameters that should be removed from the URL query.
         *
         * @param queryParamsToRemove
         * @return this builder
         */
        public Builder queryParamsToRemove(Collection<String> queryParamsToRemove) {
            this.queryParamsToRemove = new TreeSet<>(queryParamsToRemove);
            return this;
        }

        /**
         * Configures whether <a href=
         * "https://en.wikipedia.org/wiki/Internationalized_domain_name">internationalized
         * domain names (IDNs)</a> should be converted to ASCII/Punycode or
         * Unicode.
         *
         * @param idnNormalization
         * @return this builder
         */
        public Builder idnNormalization(IdnNormalization idnNormalization) {
            this.idnNormalization = idnNormalization;
            return this;
        }

        /**
         * Constructs the custom URL normalizer instance.
         *
         * @return the constructed URL normalizer
         */
        public BasicURLNormalizer build() {
            return new BasicURLNormalizer(this);
        }
    }

    public static void main(String args[]) throws IOException {
        BasicURLNormalizer normalizer = new BasicURLNormalizer();
        String line, normUrl;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, UTF_8));
        while ((line = in.readLine()) != null) {
            normUrl = normalizer.filter(line);
            LOG.info("{} => {}", line, normUrl);
        }
        System.exit(0);
    }

}
