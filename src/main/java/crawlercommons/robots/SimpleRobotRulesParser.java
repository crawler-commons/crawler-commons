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

package crawlercommons.robots;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.robots.SimpleRobotRules.RobotRulesMode;

/**
 * <p>
 * This implementation of {@link BaseRobotsParser} retrieves a set of
 * {@link SimpleRobotRules rules} for an agent with the given name from the
 * <code>robots.txt</code> file of a given domain.
 * </p>
 * 
 * <p>
 * The class fulfills two tasks. The first one is the parsing of the
 * <code>robots.txt</code> file done in
 * {@link #parseContent(String, byte[], String, String)}. During the parsing
 * process the parser searches for the provided agent name(s). If the parser
 * finds a matching name, the set of rules for this name is parsed and returned
 * as the result. <b>Note</b> that if more than one agent name is given to the
 * parser, it parses the rules for the first matching agent name inside the file
 * and skips all following user agent groups. It doesn't matter which of the
 * other given agent names would match additional rules inside the file. Thus,
 * if more than one agent name is given to the parser, the result can be
 * influenced by the order of rule sets inside the <code>robots.txt</code> file.
 * </p>
 * 
 * <p>
 * Note that the parser always parses the entire file, even if a matching agent
 * name group has been found, as it needs to collect all of the sitemap
 * directives.
 * </p>
 * 
 * <p>
 * If no rule set matches any of the provided agent names, the rule set for the
 * <code>'*'</code> agent is returned. If there is no such rule set inside the
 * <code>robots.txt</code> file, a rule set allowing all resource to be crawled
 * is returned.
 * </p>
 * 
 * <p>
 * The crawl-delay is parsed and added to the rules. Note that if the crawl
 * delay inside the file exceeds a maximum value, the crawling of all resources
 * is prohibited. The default maximum value is defined with
 * {@link #DEFAULT_MAX_CRAWL_DELAY}={@value #DEFAULT_MAX_CRAWL_DELAY}
 * milliseconds. The default value can be changed using the constructor
 * ({@link #SimpleRobotRulesParser(long, int)} or via
 * {@link #setMaxCrawlDelay(long)}.
 * </p>
 * 
 * <p>
 * The second task of this class is to generate a set of rules if the fetching
 * of the <code>robots.txt</code> file fails. The {@link #failedFetch(int)}
 * method returns a predefined set of rules based on the given error code. If
 * the status code is indicating a client error (status code = 4xx) we can
 * assume that the <code>robots.txt</code> file is not there and crawling of all
 * resources is allowed. If the status code equals a different error code (3xx
 * or 5xx) the parser assumes a temporary error and a set of rules prohibiting
 * any crawling is returned.
 * </p>
 */
@SuppressWarnings("serial")
public class SimpleRobotRulesParser extends BaseRobotsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRobotRulesParser.class);

    private enum RobotDirective {
        USER_AGENT, DISALLOW,

        ALLOW, CRAWL_DELAY, SITEMAP,

        // Russian-specific directive for mirror site?
        // Used by the zerkalschik robot?
        // See http://wataro.ur/en/web/robot.html
        HOST,

        // Google extension
        NO_INDEX,

        // ACAP directives all start with ACAP-
        ACAP_(true, false),

        // Extended standard
        REQUEST_RATE, VISIT_TIME, ROBOT_VERSION, COMMENT,

        // Line starts with http:, which we treat as sitemap directive.
        HTTP,

        // Line has no known directive on it.
        UNKNOWN(false, true),

        // Line has no directive on it
        MISSING(false, true);

        private boolean _prefix;
        private boolean _special;

        private RobotDirective() {
            _prefix = false;
            _special = false;
        }

        private RobotDirective(boolean isPrefix, boolean isSpecial) {
            _prefix = isPrefix;
            _special = isSpecial;
        }

        public boolean isSpecial() {
            return _special;
        }

        public boolean isPrefix() {
            return _prefix;
        }
    }

    private static class ParseState {
        /**
         * Flag indicating whether the given name of the agent matched.
         */
        private boolean _matchedRealName;
        /**
         * Flag that is true if not the given agent name but a wildcard matched.
         */
        private boolean _matchedWildcard;
        /**
         * Flag that is true as long as it is allowed to add rules for the given
         * agent.
         */
        private boolean _addingRules;
        /**
         * Flag indicating whether all consecutive agent fields has been seen.
         * It is set to false if an agent field is found and back to true if
         * something else has been found.
         */
        private boolean _finishedAgentFields;

        /*
         * Counter of warnings reporting invalid rules/lines in the robots.txt
         * file. The counter is used to limit the number of logged warnings.
         */
        private int _numWarnings;

        private String _url;
        private Collection<String> _targetNames;

        private SimpleRobotRules _curRules;

        public ParseState(String url, Collection<String> targetNames) {
            _url = url;
            _targetNames = targetNames;
            _curRules = new SimpleRobotRules();
        }

        public Collection<String> getTargetNames() {
            return _targetNames;
        }

        public boolean isMatchedRealName() {
            return _matchedRealName;
        }

        public void setMatchedRealName(boolean matchedRealName) {
            _matchedRealName = matchedRealName;
        }

        public boolean isMatchedWildcard() {
            return _matchedWildcard;
        }

        public void setMatchedWildcard(boolean matchedWildcard) {
            _matchedWildcard = matchedWildcard;
        }

        public boolean isAddingRules() {
            return _addingRules;
        }

        public void setAddingRules(boolean addingRules) {
            _addingRules = addingRules;
        }

        public boolean isFinishedAgentFields() {
            return _finishedAgentFields;
        }

        public void setFinishedAgentFields(boolean finishedAgentFields) {
            _finishedAgentFields = finishedAgentFields;
        }

        public void clearRules() {
            _curRules.clearRules();
        }

        public void addRule(String prefix, boolean allow) {
            _curRules.addRule(prefix, allow);
        }

        public void setCrawlDelay(long delay) {
            _curRules.setCrawlDelay(delay);
        }

        public SimpleRobotRules getRobotRules() {
            return _curRules;
        }

        public String getUrl() {
            return _url;
        }

        public void addSitemap(String sitemap) {
            _curRules.addSitemap(sitemap);
        }

    }

    private static class RobotToken {
        private RobotDirective _directive;
        private String _data;

        public RobotToken(RobotDirective directive, String data) {
            _directive = directive;
            _data = data;
        }

        public RobotDirective getDirective() {
            return _directive;
        }

        public String getData() {
            return _data;
        }
    }

    private static Map<String, RobotDirective> DIRECTIVE_PREFIX = new HashMap<String, RobotDirective>();

    static {
        for (RobotDirective directive : RobotDirective.values()) {
            if (!directive.isSpecial()) {
                String prefix = directive.name().toLowerCase(Locale.ROOT).replaceAll("_", "-");
                DIRECTIVE_PREFIX.put(prefix, directive);
            }
        }

        DIRECTIVE_PREFIX.put("useragent", RobotDirective.USER_AGENT);
        DIRECTIVE_PREFIX.put("useg-agent", RobotDirective.USER_AGENT);
        DIRECTIVE_PREFIX.put("ser-agent", RobotDirective.USER_AGENT);

        DIRECTIVE_PREFIX.put("desallow", RobotDirective.DISALLOW);
        DIRECTIVE_PREFIX.put("dissalow", RobotDirective.DISALLOW);
        DIRECTIVE_PREFIX.put("dssalow", RobotDirective.DISALLOW);
        DIRECTIVE_PREFIX.put("dsallow", RobotDirective.DISALLOW);

        DIRECTIVE_PREFIX.put("crawl delay", RobotDirective.CRAWL_DELAY);
    }

    // separator is either one or more spaces/tabs, or a colon
    private static final Pattern COLON_DIRECTIVE_DELIMITER = Pattern.compile("[ \t]*:[ \t]*(.*)");
    private static final Pattern BLANK_DIRECTIVE_DELIMITER = Pattern.compile("[ \t]+(.*)");

    // match the rest of the directive, up until whitespace or colon
    private static final Pattern DIRECTIVE_SUFFIX_PATTERN = Pattern.compile("[^: \t]+(.*)");

    // split pattern for robot names
    private static final Pattern ROBOT_NAMES_SPLIT = Pattern.compile("\\s*,\\s*|\\s+");

    /**
     * Figure out directive on line of text from robots.txt file. We assume the
     * line has been lower-cased
     * 
     * @param line
     * @return robot command found on line
     */
    private static RobotToken tokenize(String line) {
        String lowerLine = line.toLowerCase(Locale.ROOT);
        for (String prefix : DIRECTIVE_PREFIX.keySet()) {
            int prefixLength = prefix.length();
            if (lowerLine.startsWith(prefix)) {
                RobotDirective directive = DIRECTIVE_PREFIX.get(prefix);
                String dataPortion = line.substring(prefixLength);

                if (directive.isPrefix()) {
                    Matcher m = DIRECTIVE_SUFFIX_PATTERN.matcher(dataPortion);
                    if (m.matches()) {
                        dataPortion = m.group(1);
                    } else {
                        continue;
                    }
                }

                Matcher m = COLON_DIRECTIVE_DELIMITER.matcher(dataPortion);
                if (!m.matches()) {
                    m = BLANK_DIRECTIVE_DELIMITER.matcher(dataPortion);
                }

                if (m.matches()) {
                    return new RobotToken(directive, m.group(1).trim());
                }
            }
        }

        Matcher m = COLON_DIRECTIVE_DELIMITER.matcher(lowerLine);
        if (m.matches()) {
            return new RobotToken(RobotDirective.UNKNOWN, line);
        } else {
            return new RobotToken(RobotDirective.MISSING, line);
        }
    }

    private static final Pattern SIMPLE_HTML_PATTERN = Pattern.compile("(?is)<(html|head|body)\\s*>");
    private static final Pattern USER_AGENT_PATTERN = Pattern.compile("(?i)user-agent:");

    /**
     * Pattern to match a valid user-agent product tokens as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.2.1">RFC
     * 9309, section 2.2.1</a>
     */
    protected static final Pattern USER_AGENT_PRODUCT_TOKEN_MATCHER = Pattern.compile("[a-zA-Z_-]+");

    /**
     * Default max number of warnings logged during parse of any one robots.txt
     * file, see {@link #setMaxWarnings(int)}
     */
    public static final int DEFAULT_MAX_WARNINGS = 5;

    /**
     * Default max Crawl-Delay in milliseconds, see
     * {@link #setMaxCrawlDelay(long)}
     */
    public static final long DEFAULT_MAX_CRAWL_DELAY = 300000;

    // number of warnings found in the latest processed robots.txt file
    private ThreadLocal<Integer> _numWarningsDuringLastParse = new ThreadLocal<>();

    private int _maxWarnings;
    private long _maxCrawlDelay;
    private boolean _exactUserAgentMatching;

    public SimpleRobotRulesParser() {
        this(DEFAULT_MAX_CRAWL_DELAY, DEFAULT_MAX_WARNINGS);
    }

    /**
     * @param maxCrawlDelay
     *            see {@link #setMaxCrawlDelay(long)}
     * @param maxWarnings
     *            see {@link #setMaxWarnings(int)}
     */
    public SimpleRobotRulesParser(long maxCrawlDelay, int maxWarnings) {
        this._maxCrawlDelay = maxCrawlDelay;
        this._maxWarnings = maxWarnings;
        this._exactUserAgentMatching = true;
    }

    /**
     * Validate a user-agent product token as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.2.1">RFC
     * 9309, section 2.2.1</a>
     * 
     * @param userAgent
     *            user-agent token to verify
     * @return true if the product token is valid
     */
    protected static boolean isValidUserAgentToObey(String userAgent) {
        return userAgent != null && USER_AGENT_PRODUCT_TOKEN_MATCHER.matcher(userAgent).matches();
    }

    @Override
    public SimpleRobotRules failedFetch(int httpStatusCode) {
        SimpleRobotRules result;

        if ((httpStatusCode >= 200) && (httpStatusCode < 300)) {
            throw new IllegalStateException("Can't use status code constructor with 2xx response");
        } else if ((httpStatusCode >= 300) && (httpStatusCode < 400)) {
            // Should only happen if we're getting endless redirects (more than
            // our follow limit), so treat it as a temporary failure.
            result = new SimpleRobotRules(RobotRulesMode.ALLOW_NONE);
            result.setDeferVisits(true);
        } else if ((httpStatusCode >= 400) && (httpStatusCode < 500)) {
            // Some sites return 410 (gone) instead of 404 (not found), so treat
            // as the same. Actually treat all (including forbidden) as "no
            // robots.txt", as that's what Google and other search engines do.
            result = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
        } else {
            // Treat all other status codes as a temporary failure.
            result = new SimpleRobotRules(RobotRulesMode.ALLOW_NONE);
            result.setDeferVisits(true);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * crawlercommons.robots.BaseRobotsParser#parseContent(java.lang.String,
     * byte[], java.lang.String, java.lang.String)
     */
    @Deprecated
    @Override
    public SimpleRobotRules parseContent(String url, byte[] content, String contentType, String robotNames) {
        return parseContent(url, content, contentType, new LinkedHashSet<>(Arrays.asList(splitRobotNames(robotNames))), false);
    }

    /**
     * Split a string listing user-agent / robot names into tokens.
     * 
     * Splitting is done at comma and/or whitespace, the tokens are converted to
     * lower-case.
     * 
     * @param robotNames
     *            robot / user-agent string
     * @return array of user-agent / robot tokens
     */
    protected String[] splitRobotNames(String robotNames) {
        // lower case and trim
        robotNames = robotNames.toLowerCase(Locale.ROOT).trim();
        // split at commas and whitespace
        return ROBOT_NAMES_SPLIT.split(robotNames);
    }

    /**
     * Parse the robots.txt file in <i>content</i>, and return rules appropriate
     * for processing paths by <i>userAgent</i>. Multiple agent names are
     * provided as collection. See {@link #setExactUserAgentMatching(boolean)}
     * for details how agent names are matched.
     * 
     * @param url
     *            URL that robots.txt content was fetched from. A complete and
     *            valid URL (e.g., https://example.com/robots.txt) is expected.
     *            Used to resolve relative sitemap URLs and for
     *            logging/reporting purposes.
     * @param content
     *            raw bytes from the site's robots.txt file
     * @param contentType
     *            HTTP response header (mime-type)
     * @param robotNames
     *            name(s) of crawler, used to select rules from the robots.txt
     *            file by matching the names against the user-agent lines in the
     *            robots.txt file. Robot names should be single token names, w/o
     *            version or other parts. Names should be lower-case, as the
     *            user-agent line is also converted to lower-case for matching.
     * @return robot rules.
     */
    @Override
    public SimpleRobotRules parseContent(String url, byte[] content, String contentType, Collection<String> robotNames) {
        return parseContent(url, content, contentType, robotNames, isExactUserAgentMatching());
    }

    private SimpleRobotRules parseContent(String url, byte[] content, String contentType, Collection<String> robotNames, boolean exactUserAgentMatching) {

        // If there's nothing there, treat it like we have no restrictions.
        if ((content == null) || (content.length == 0)) {
            return new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
        }

        int bytesLen = content.length;
        int offset = 0;

        /*
         * RFC 9309 requires that is "UTF-8 encoded" (<a href=
         * "https://www.rfc-editor.org/rfc/rfc9309.html#name-access-method"> RFC
         * 9309, section 2.3 Access Method</a>), but
         * "Implementors MAY bridge encoding mismatches if they detect that the robots.txt file is not UTF-8 encoded."
         * (<a href=
         * "https://www.rfc-editor.org/rfc/rfc9309.html#name-the-allow-and-disallow-line"
         * > RFC 9309, section 2.2.2. The "Allow" and "Disallow" Lines</a>)
         */
        Charset encoding = StandardCharsets.UTF_8;

        // Check for a UTF-8 BOM at the beginning (EF BB BF)
        if ((bytesLen >= 3) && (content[0] == (byte) 0xEF) && (content[1] == (byte) 0xBB) && (content[2] == (byte) 0xBF)) {
            offset = 3;
            bytesLen -= 3;
            encoding = StandardCharsets.UTF_8;
        }
        // Check for UTF-16LE BOM at the beginning (FF FE)
        else if ((bytesLen >= 2) && (content[0] == (byte) 0xFF) && (content[1] == (byte) 0xFE)) {
            offset = 2;
            bytesLen -= 2;
            encoding = StandardCharsets.UTF_16LE;
        }
        // Check for UTF-16BE BOM at the beginning (FE FF)
        else if ((bytesLen >= 2) && (content[0] == (byte) 0xFE) && (content[1] == (byte) 0xFF)) {
            offset = 2;
            bytesLen -= 2;
            encoding = StandardCharsets.UTF_16BE;
        }

        String contentAsStr;
        contentAsStr = new String(content, offset, bytesLen, encoding);

        // Decide if we need to do special HTML processing.
        boolean isHtmlType = ((contentType != null) && contentType.toLowerCase(Locale.ROOT).startsWith("text/html"));

        // If it looks like it contains HTML, but doesn't have a user agent
        // field, then
        // assume somebody messed up and returned back to us a random HTML page
        // instead
        // of a robots.txt file.
        boolean hasHTML = false;
        if (isHtmlType || SIMPLE_HTML_PATTERN.matcher(contentAsStr).find()) {
            if (!USER_AGENT_PATTERN.matcher(contentAsStr).find()) {
                LOGGER.trace("Found non-robots.txt HTML file: " + url);
                return new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
            } else {
                // We'll try to strip out HTML tags below.
                if (isHtmlType) {
                    LOGGER.debug("HTML content type returned for robots.txt file: " + url);
                } else {
                    LOGGER.debug("Found HTML in robots.txt file: " + url);
                }

                hasHTML = true;
            }
        }

        // Break on anything that might be used as a line ending. Since
        // tokenizer doesn't return empty tokens, a \r\n sequence still
        // works since it looks like an empty string between the \r and \n.
        StringTokenizer lineParser = new StringTokenizer(contentAsStr, "\n\r\u0085\u2028\u2029");
        ParseState parseState = new ParseState(url, robotNames);

        while (lineParser.hasMoreTokens()) {
            String line = lineParser.nextToken();

            // Get rid of HTML markup, in case some brain-dead webmaster has
            // created an HTML
            // page for robots.txt. We could do more sophisticated processing
            // here to better
            // handle bad HTML, but that's a very tiny percentage of all
            // robots.txt files.
            if (hasHTML) {
                line = line.replaceAll("<[^>]+>", "");
            }

            // trim out comments and whitespace
            int hashPos = line.indexOf("#");
            if (hashPos >= 0) {
                line = line.substring(0, hashPos);
            }

            line = line.trim();
            if (line.length() == 0) {
                continue;
            }

            RobotToken token = tokenize(line);
            switch (token.getDirective()) {
                case USER_AGENT:
                handleUserAgent(parseState, token);
                    break;

                case DISALLOW:
                parseState.setFinishedAgentFields(true);
                handleDisallow(parseState, token);
                    break;

                case ALLOW:
                parseState.setFinishedAgentFields(true);
                handleAllow(parseState, token);
                    break;

                case CRAWL_DELAY:
                parseState.setFinishedAgentFields(true);
                handleCrawlDelay(parseState, token);
                    break;

                case SITEMAP:
                parseState.setFinishedAgentFields(true);
                handleSitemap(parseState, token);
                    break;

                case HTTP:
                parseState.setFinishedAgentFields(true);
                handleHttp(parseState, token);
                    break;

                case UNKNOWN:
                reportWarning(parseState, "Unknown directive in robots.txt file: {}", line);
                parseState.setFinishedAgentFields(true);
                    break;

                case MISSING:
                reportWarning(parseState, "Unknown line in robots.txt file (size {}): {}", content.length, line);
                parseState.setFinishedAgentFields(true);
                    break;

                default:
                    // All others we just ignore
                    // TODO KKr - which of these should be setting
                    // finishedAgentFields to true?
                    // TODO KKr - handle no-index
                    // TODO KKr - handle request-rate and visit-time
                    break;
            }
        }

        this._numWarningsDuringLastParse.set(parseState._numWarnings);
        SimpleRobotRules result = parseState.getRobotRules();
        if (result.getCrawlDelay() > _maxCrawlDelay) {
            // Some evil sites use a value like 3600 (seconds) for the crawl
            // delay, which would cause lots of problems for us.
            LOGGER.debug("Crawl delay exceeds max value - so disallowing all URLs: {}", url);
            return new SimpleRobotRules(RobotRulesMode.ALLOW_NONE);
        } else {
            result.sortRules();
            return result;
        }
    }

    private void reportWarning(ParseState state, String msg, Object... args) {
        state._numWarnings += 1;

        if (state._numWarnings == 1) {
            LOGGER.warn("Problem processing robots.txt for {}", state._url);
        }

        if (state._numWarnings < _maxWarnings) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof String && ((String) args[i]).length() > 1024) {
                    // clip overlong strings to prevent from overflows in log messages
                    args[i] = ((String) args[i]).substring(0, 1024) + " ...";
                }
            }
            LOGGER.warn("\t " + msg, args);
        }
    }

    /*
     * Check whether user-agent line starts with a valid user-agent product
     * token, but continues with additional characters to be ignored e.g. "foo"
     * matches "User-agent: foo/1.2" or "butterfly" matches
     * "User-agent: Butterfly/1.0"
     * 
     * See https://www.rfc-editor.org/rfc/rfc9309.html#section-2.3.1.5
     * "Crawlers MUST try to parse each line of the robots.txt file. Crawlers MUST use the parseable rules."
     * and https://github.com/google/robotstxt/issues/56
     * 
     * This method may be overridden to implement non-standard user-agent matching.
     * 
     * @param agentName user-agent, found in the <a href=
     * "https://www.rfc-editor.org/rfc/rfc9309.html#name-the-user-agent-line">
     * robots.txt user-agent line</a>
     * 
     * @param targetTokens collection of user-agent product tokens we're looking
     * for. Product tokens are expected to be lowercase.
     * 
     * @return true if the product token is match as token prefix
     */
    protected boolean userAgentProductTokenPartialMatch(String agentName, Collection<String> targetTokens) {
        Matcher m = USER_AGENT_PRODUCT_TOKEN_MATCHER.matcher(agentName);
        return m.lookingAt() && targetTokens.contains(m.group());
    }

    /**
     * Handle the user-agent: directive
     * 
     * @param state
     *            current parsing state
     * @param token
     *            data for directive
     */
    private void handleUserAgent(ParseState state, RobotToken token) {
        // If we are adding rules, and had already finished reading user agent
        // fields, then we are in a new section, hence stop adding rules
        if (state.isAddingRules() && state.isFinishedAgentFields()) {
            state.setAddingRules(false);
        }

        // Clearly we've encountered a new user agent directive, hence we need to start
        // processing until we are finished.
        state.setFinishedAgentFields(false);

        // Handle the case when multiple target names are passed.
        // We assume we should do case-insensitive comparison of every target name.
        Collection<String> targetNames = state.getTargetNames();

        if (isExactUserAgentMatching()) {
            String agentName = token.getData().trim().toLowerCase(Locale.ROOT);
            if (agentName.isEmpty()) {
                // Ignore empty names
            } else if (agentName.equals("*") && !state.isMatchedRealName()) {
                state.setMatchedWildcard(true);
                state.setAddingRules(true);
            } else if (targetNames.contains(agentName) || (!isValidUserAgentToObey(agentName) && userAgentProductTokenPartialMatch(agentName, targetNames))) {
                if (state.isMatchedWildcard()) {
                    // Clear rules of the wildcard user-agent found
                    // before the non-wildcard user-agent match.
                    state.clearRules();
                }
                state.setMatchedRealName(true);
                state.setAddingRules(true);
                state.setMatchedWildcard(false);
            }
        } else {
            /*
             * prefix matching on user-agent words - backward-compatibility with
             * the old and deprecated API if "User-Agent" HTTP request header
             * strings are passed as param instead of single-word/token
             * user-agent names, e.g. if the robot name is "WebCrawler/1.0" and is
             * expected match the robots.txt directive "User-agent: mybot"
             * or also "User-agent: my".
             */
            String agentNameFull = token.getData().trim().toLowerCase(Locale.ROOT);
            boolean matched = false;
            if (agentNameFull.equals("*") && !state.isMatchedRealName()) {
                state.setMatchedWildcard(true);
                state.setAddingRules(true);
            } else if (userAgentProductTokenPartialMatch(agentNameFull, targetNames)) {
                // match "butterfly" in the line "User-agent: Butterfly/1.0"
                matched = true;
            } else {
                String[] agentNames = ROBOT_NAMES_SPLIT.split(agentNameFull);
                if (agentNames.length > 1) {
                    LOGGER.debug("Multiple agent names in user-agent line: {}", token.getData());
                }
                for (String agentName : agentNames) {
                    for (String targetName : targetNames) {
                        LOGGER.debug(targetName);
                        if (targetName.startsWith(agentName)) {
                            matched = true;
                            break;
                        }
                    }
                }
            }
            if (matched) {
                if (state.isMatchedWildcard()) {
                    // Clear rules of the wildcard user-agent found
                    // before the non-wildcard user-agent match.
                    state.clearRules();
                }
                state.setMatchedRealName(true);
                state.setAddingRules(true);
                state.setMatchedWildcard(false);
            }
        }
    }

    /**
     * Add any uniform rules to clean up path directives
     * @param path
     * @return clean path
     */
    private String normalizePathDirective(String path) {
        path = path.trim();

        return path;
    }

    /**
     * Handle the disallow: directive
     * 
     * @param state
     *            current parsing state
     * @param token
     *            data for directive
     */
    private void handleDisallow(ParseState state, RobotToken token) {
        if (!state.isAddingRules()) {
            return;
        }

        String path = token.getData();

        try {
            path = URLDecoder.decode(path, "UTF-8");
            path = normalizePathDirective(path);
            if (path.length() == 0) {
                // Disallow: <nothing> => allow all.
                state.clearRules();
            } else {
                state.addRule(path, false);
            }
        } catch (Exception e) {
            reportWarning(state, "Error parsing robots rules - can't decode path: {}", path);
        }
    }

    /**
     * Handle the allow: directive
     * 
     * @param state
     *            current parsing state
     * @param token
     *            data for directive
     */
    private void handleAllow(ParseState state, RobotToken token) {
        if (!state.isAddingRules()) {
            return;
        }

        String path = token.getData();

        try {
            path = URLDecoder.decode(path, "UTF-8");
            path = normalizePathDirective(path);
        } catch (Exception e) {
            reportWarning(state, "Error parsing robots rules - can't decode path: {}", path);
        }

        if (path.length() == 0) {
            // Allow: <nothing> => allow all.
            state.clearRules();
        } else {
            state.addRule(path, true);
        }
    }

    /**
     * Handle the crawl-delay: directive
     * 
     * @param state
     *            current parsing state
     * @param token
     *            data for directive
     */
    private void handleCrawlDelay(ParseState state, RobotToken token) {
        if (!state.isAddingRules()) {
            return;
        }

        String delayString = token.getData();
        if (delayString.length() > 0) {
            try {
                // Some sites use values like 0.5 for the delay.
                if (delayString.indexOf('.') != -1) {
                    double delayValue = Double.parseDouble(delayString) * 1000.0;
                    state.setCrawlDelay(Math.round(delayValue));
                } else {
                    long delayValue = Integer.parseInt(delayString) * 1000L; // sec
                                                                             // to
                                                                             // millisec
                    state.setCrawlDelay(delayValue);
                }
            } catch (Exception e) {
                reportWarning(state, "Error parsing robots rules - can't decode crawl delay: {}", delayString);
            }
        }
    }

    /**
     * Handle the sitemap: directive
     * 
     * @param state
     *            current parsing state
     * @param token
     *            data for directive
     */
    private void handleSitemap(ParseState state, RobotToken token) {

        String sitemap = token.getData();
        try {
            URL sitemapUrl;
            URL base = null;
            try {
                base = new URL(state.getUrl());
            } catch (MalformedURLException e) {
                // must try without base URL
            }
            if (base != null) {
                sitemapUrl = new URL(base, sitemap);
            } else {
                sitemapUrl = new URL(sitemap);
            }
            String hostname = sitemapUrl.getHost();
            if ((hostname != null) && (hostname.length() > 0)) {
                state.addSitemap(sitemapUrl.toExternalForm());
            }
        } catch (Exception e) {
            reportWarning(state, "Invalid URL with sitemap directive:  {}", sitemap);
        }
    }

    /**
     * Handle a line that starts with http: and contains "sitemap", which we
     * treat as a missing sitemap: xxx directive.
     * 
     * @param state
     *            current parsing state
     * @param token
     *            data for directive
     */
    private void handleHttp(ParseState state, RobotToken token) {
        String urlFragment = token.getData();
        if (urlFragment.contains("sitemap")) {
            RobotToken fixedToken = new RobotToken(RobotDirective.SITEMAP, "http:" + token.getData());
            handleSitemap(state, fixedToken);
        } else {
            reportWarning(state, "Found raw non-sitemap URL: http:{}", urlFragment);
        }
    }

    /**
     * Get the number of warnings due to invalid rules/lines in the latest
     * processed robots.txt file (see
     * {@link #parseContent(String, byte[], String, String)}.
     * 
     * Note: an incorrect value may be returned if the processing of the
     * robots.txt happened in a different than the current thread.
     * 
     * @return number of warnings
     */
    public int getNumWarnings() {
        return _numWarningsDuringLastParse.get();
    }

    /** Get max number of logged warnings per robots.txt */
    public int getMaxWarnings() {
        return _maxWarnings;
    }

    /**
     * Set the max number of warnings about parse errors logged per robots.txt
     */
    public void setMaxWarnings(int maxWarnings) {
        _maxWarnings = maxWarnings;
    }

    /**
     * Get configured max crawl delay.
     *
     * @return the configured max. crawl delay, see
     *         {@link #setMaxCrawlDelay(long)}
     */
    public long getMaxCrawlDelay() {
        return _maxCrawlDelay;
    }

    /**
     * Set the max value in milliseconds accepted for the <a href=
     * "https://en.wikipedia.org/wiki/Robots_exclusion_standard#Crawl-delay_directive">Crawl-Delay</a>
     * directive. If the value in the robots.txt is greater than the max. value,
     * all pages are skipped to avoid that overtly long Crawl-Delays block fetch
     * queues and make the crawling slow. Note: the value is in milliseconds as
     * some sites use floating point numbers to define the delay.
     */
    public void setMaxCrawlDelay(long maxCrawlDelay) {
        _maxCrawlDelay = maxCrawlDelay;
    }

    /**
     * Set how the user-agent names in the robots.txt (<code>User-agent:</code>
     * lines) are matched with the provided robot names:
     * <ul>
     * <li>(with exact matching) follow the
     * <a href= "https://datatracker.ietf.org/doc/rfc9309/">Robots Exclusion
     * Protocol RFC 9309</a> and match user agent <b>literally but
     * case-insensitive over the full string length</b>:
     * 
     * <blockquote>Crawlers set their own name, which is called a product token,
     * to find relevant groups. The product token MUST contain only upper and
     * lowercase letters ("a-z" and "A-Z"), underscores ("_"), and hyphens
     * ("-"). [...] Crawlers MUST use case-insensitive matching to find the
     * group that matches the product token and then obey the rules of the
     * group.</blockquote></li>
     * 
     * <li>(without exact matching) split the user-agent and robot names at
     * whitespace into words and perform a prefix match (one of the user-agent
     * words must be a prefix of one of the robot words, eg. the robot name
     * <code>WebCrawler/3.0</code> matches the robots.txt directive
     * 
     * <pre>
     * User-agent: webcrawler
     * </pre>
     * 
     * This prefix matching on words allows that crawler developers lazily use
     * the HTTP User-Agent string also for the robots.txt parser. It does not
     * cover the case when the HTTP User-Agent string is used in the
     * robots.txt.</li>
     * </ul>
     * 
     * @param exactMatching
     *            if true, configure exact user-agent name matching. If false,
     *            disable exact matching and do prefix matching on user-agent
     *            words.
     */
    public void setExactUserAgentMatching(boolean exactMatching) {
        _exactUserAgentMatching = exactMatching;
    }

    /**
     * @return whether exact user-agent matching is configured, see
     *         {@link #setExactUserAgentMatching(boolean)}
     */
    public boolean isExactUserAgentMatching() {
        return _exactUserAgentMatching;
    }

    public static void main(String[] args) throws MalformedURLException, IOException {
        if (args.length < 1) {
            System.err.println("SimpleRobotRulesParser <robots.txt> [[<agentname>] <URL>...]");
            System.err.println();
            System.err.println("Parse a robots.txt file");
            System.err.println("  <robots.txt>\tURL pointing to robots.txt file.");
            System.err.println("              \tTo read a local file use a file:// URL");
            System.err.println("              \t(parsed as http://example.com/robots.txt)");
            System.err.println("  <agentname> \tuser agent name to check for exclusion rules.");
            System.err.println("              \tIf not defined check with '*'");
            System.err.println("  <URL>       \tcheck URL whether allowed or forbidden.");
            System.err.println("              \tIf no URL is given show robots.txt rules");
            System.exit(1);
        }

        String url = args[0];
        String agentName = "*";
        if (args.length >= 2) {
            agentName = args[1];
        }

        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        BaseRobotRules rules;
        URLConnection connection = new URL(url).openConnection();
        try {
            byte[] content = IOUtils.toByteArray(connection);
            if (!url.matches("^https?://")) {
                // use artificial URL to avoid problems resolving relative
                // sitemap paths for file:/ URLs
                url = "http://example.com/robots.txt";
            }
            rules = parser.parseContent(url, content, "text/plain", agentName);
        } catch (IOException e) {
            if (connection instanceof HttpURLConnection) {
                int code = ((HttpURLConnection) connection).getResponseCode();
                rules = parser.failedFetch(code);
                System.out.println("Fetch of " + url + " failed with HTTP status code " + code);
            } else {
                throw e;
            }
        }

        if (args.length < 3) {
            // no URL(s) given, print rules and exit
            System.out.println("Robot rules for user agentname '" + agentName + "':");
            System.out.println(rules.toString());
        } else {
            System.out.println("Checking URLs:");
            for (int i = 2; i < args.length; i++) {
                System.out.println((rules.isAllowed(args[i]) ? "allowed  " : "forbidden") + "\t" + args[i]);
            }
        }
    }

}
