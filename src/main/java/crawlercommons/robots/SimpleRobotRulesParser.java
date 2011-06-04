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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.robots.SimpleRobotRules.RobotRulesMode;


@SuppressWarnings("serial")
public class SimpleRobotRulesParser extends BaseRobotsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRobotRulesParser.class);
    
    private enum RobotDirective {
        USER_AGENT,
        DISALLOW,
        
        ALLOW,
        CRAWL_DELAY,
        SITEMAP,
        
        // Russian-specific directive for mirror site?
        // Used by the zerkalschik robot?
        // See http://wataro.ur/en/web/robot.html
        HOST,
        
        // Google extension
        NO_INDEX,
        
        // ACAP directives all start with ACAP-
        ACAP_(true, false),
        
        // Extended standard
        REQUEST_RATE,
        VISIT_TIME,
        ROBOT_VERSION,
        COMMENT,
        
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
        private boolean _matchedRealName;
        private boolean _matchedWildcard;
        private boolean _addingRules;
        private boolean _finishedAgentFields;
        
        private String _url;
        private String _targetName;
        
        private SimpleRobotRules _curRules;
        
        public ParseState(String url, String targetName) {
            _url = url;
            _targetName = targetName;
            _curRules = new SimpleRobotRules();
        }
        
        public String getTargetName() {
            return _targetName;
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
                String prefix = directive.name().toLowerCase().replaceAll("_", "-");
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

    /**
     * Figure out directive on line of text from robots.txt file.
     * We assume the line has been lower-cased
     * 
     * @param line
     * @return robot command found on line
     */
    private static RobotToken tokenize(String line) {
        for (String prefix : DIRECTIVE_PREFIX.keySet()) {
            int prefixLength = prefix.length();
            if (line.startsWith(prefix)) {
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
        
        Matcher m = COLON_DIRECTIVE_DELIMITER.matcher(line);
        if (m.matches()) {
            return new RobotToken(RobotDirective.UNKNOWN, line);
        } else {
            return new RobotToken(RobotDirective.MISSING, line);
        }
    }
    
    private static final Pattern SIMPLE_HTML_PATTERN = Pattern.compile("(?is)<(html|head|body)\\s*>");
    private static final Pattern USER_AGENT_PATTERN = Pattern.compile("(?i)user-agent:");

    // Max # of warnings during parse of any one robots.txt file.
    private static final int MAX_WARNINGS = 5;
    
    // Max value for crawl delay we'll use from robots.txt file. If the value is greater
    // than this, we'll skip all pages.
    private static final long MAX_CRAWL_DELAY = 300000;

    private int _numWarnings;
    
    @Override
    public BaseRobotRules failedFetch(int httpStatusCode) {
        SimpleRobotRules result;
        
        if ((httpStatusCode >= 200) && (httpStatusCode < 300)) {
            throw new IllegalStateException("Can't use status code constructor with 2xx response");
        } else if ((httpStatusCode >= 300) && (httpStatusCode < 400)) {
            // Should only happen if we're getting endless redirects (more than our follow limit), so
            // treat it as a temporary failure.
            result = new SimpleRobotRules(RobotRulesMode.ALLOW_NONE);
            result.setDeferVisits(true);
        } else if ((httpStatusCode >= 400) && (httpStatusCode < 500)) {
            // Some sites return 410 (gone) instead of 404 (not found), so treat as the same.
            // Actually treat all (including forbidden) as "no robots.txt", as that's what Google
            // and other search engines do.
            result = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
        } else {
            // Treat all other status codes as a temporary failure.
            result = new SimpleRobotRules(RobotRulesMode.ALLOW_NONE);
            result.setDeferVisits(true);
        }
        
        return result;
    }

    @Override
    public BaseRobotRules parseContent(String url, byte[] content, String contentType, String robotName) {
        _numWarnings = 0;
        
        // If there's nothing there, treat it like we have no restrictions.
        if ((content == null) || (content.length == 0)) {
            return new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
        }

        int bytesLen = content.length;
        int offset = 0;
        String encoding = "us-ascii";
        
        // Check for a UTF-8 BOM at the beginning (EF BB BF)
        if ((bytesLen >= 3) && (content[0] == (byte)0xEF) && (content[1] == (byte)0xBB) && (content[2] == (byte)0xBF)) {
            offset = 3;
            bytesLen -= 3;
            encoding = "UTF-8";
        }
        
        String contentAsStr;
        try {
            contentAsStr = new String(content, offset, bytesLen, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Impossible unsupported encoding exception for " + encoding);
        }

        // Decide if we need to do special HTML processing.
        boolean isHtmlType = ((contentType != null) && contentType.toLowerCase().startsWith("text/html"));
        
        // If it looks like it contains HTML, but doesn't have a user agent field, then
        // assume somebody messed up and returned back to us a random HTML page instead
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
        
        // Break on anything that might be used as a line ending. Since tokenizer doesn't
        // return empty tokens, a \r\n sequence still works since it looks like an empty
        // string between the \r and \n.
        StringTokenizer lineParser = new StringTokenizer(contentAsStr, "\n\r\u0085\u2028\u2029");
        ParseState parseState = new ParseState(url, robotName.toLowerCase());
        boolean keepGoing = true;
        
        while (keepGoing && lineParser.hasMoreTokens()) {
            String line = lineParser.nextToken();

            // Get rid of HTML markup, in case some brain-dead webmaster has created an HTML
            // page for robots.txt. We could do more sophisticated processing here to better
            // handle bad HTML, but that's a very tiny percentage of all robots.txt files.
            if (hasHTML) {
                line = line.replaceAll("<[^>]+>","");
            }
            
            // trim out comments and whitespace
            int hashPos = line.indexOf("#");
            if (hashPos >= 0) {
                line = line.substring(0, hashPos);
            }
            
            line = line.trim().toLowerCase();
            if (line.length() == 0) {
                continue;
            }
         
            RobotToken token = tokenize(line);
            switch (token.getDirective()) {
                case USER_AGENT:
                    keepGoing = handleUserAgent(parseState, token);
                    break;
                    
                case DISALLOW:
                    keepGoing = handleDisallow(parseState, token);
                    break;
                    
                case ALLOW:
                    keepGoing = handleAllow(parseState, token);
                    break;
                    
                case CRAWL_DELAY:
                    keepGoing = handleCrawlDelay(parseState, token);
                    break;
                    
                case SITEMAP:
                    keepGoing = handleSitemap(parseState, token);
                    break;
                    
                case HTTP:
                    keepGoing = handleHttp(parseState, token);
                    break;

                case UNKNOWN:
                    reportWarning("Unknown directive in robots.txt file: " + line, url);
                    parseState.setFinishedAgentFields(true);
                    break;
                    
                case MISSING:
                    reportWarning(String.format("Unknown line in robots.txt file (size %d): %s", content.length, line), url);
                    parseState.setFinishedAgentFields(true);
                    break;
                    
                default:
                    // All others we just ignore
                    // TODO KKr - which of these should be setting finishedAgentFields to true?
                    // TODO KKr - handle no-index
                    // TODO KKr - handle request-rate and visit-time
                    break;
            }
        }

        SimpleRobotRules result = parseState.getRobotRules();
        if (result.getCrawlDelay() > MAX_CRAWL_DELAY) {
            // Some evil sites use a value like 3600 (seconds) for the crawl delay, which would
            // cause lots of problems for us.
            LOGGER.debug("Crawl delay exceeds max value - so disallowing all URLs: " + url);
            return new SimpleRobotRules(RobotRulesMode.ALLOW_NONE);
        } else {
            return result;
        }
    }

    private void reportWarning(String msg, String url) {
        _numWarnings += 1;
        
        if (_numWarnings == 1) {
            LOGGER.warn("Problem processing robots.txt for " + url);
        }
        
        if (_numWarnings < MAX_WARNINGS) {
            LOGGER.warn("\t" + msg);
        }
    }
    
    /**
     * Handle the user-agent: directive
     * 
     * @param state current parsing state
     * @param token data for directive
     * @return true to keep going, false if we're done
     */
    private boolean handleUserAgent(ParseState state, RobotToken token) {
        if (state.isMatchedRealName()) {
            if (state.isFinishedAgentFields()) {
                // We're all done.
                return false;
            } else {
                // Skip any more of these, once we have a real name match. We're waiting for some
                // allow/disallow/crawl delay fields.
                return true;
            }
        }

        if (state.isFinishedAgentFields()) {
            // We've got a user agent field, so we haven't yet seen anything that tells us
            // we're done with this set of agent names.
            state.setFinishedAgentFields(false);
            state.setAddingRules(false);
        }
        
        // Extract possible match names from our target agent name, since it appears
        // to be expected that "Mozilla botname 1.0" matches "botname"
        String[] targetNames = state.getTargetName().split(" ");
        
        // TODO KKr - catch case of multiple names, log as non-standard.
        String[] agentNames = token.getData().split("[ \t,]");
        for (String agentName : agentNames) {
            if (agentName.equals("*") && !state.isMatchedWildcard()) {
                state.setMatchedWildcard(true);
                state.setAddingRules(true);
            } else {
                for (String targetName : targetNames) {
                    if (targetName.startsWith(agentName)) {
                        state.setMatchedRealName(true);
                        state.setAddingRules(true);
                        state.clearRules();     // In case we previously hit a wildcard rule match
                        break;
                    }
                }
            }
        }
        
        // Keep going
        return true;
    }
    
    /**
     * Handle the disallow: directive
     * 
     * @param state current parsing state
     * @param token data for directive
     * @return true to keep going, false if we're done
     */
    private boolean handleDisallow(ParseState state, RobotToken token) {
        state.setFinishedAgentFields(true);
        
        if (!state.isAddingRules()) {
            return true;
        }
        
        String path = token.getData();
        
        try {
            path = URLDecoder.decode(path, "UTF-8");
            
            if (path.length() == 0) {
                // Disallow: <nothing> => allow all.
                state.clearRules();
            } else {
                state.addRule(path, false);
            }
        } catch (Exception e) {
            reportWarning("Error parsing robots rules - can't decode path: " + path, state.getUrl());
        }
        
        return true;
    }
    
    /**
     * Handle the allow: directive
     * 
     * @param state current parsing state
     * @param token data for directive
     * @return true to keep going, false if we're done
     */
    private boolean handleAllow(ParseState state, RobotToken token) {
        state.setFinishedAgentFields(true);
        
        if (!state.isAddingRules()) {
             return true;
         }
         
         String path = token.getData();
         
         try {
             path = URLDecoder.decode(path, "UTF-8");
         } catch (Exception e) {
             reportWarning("Error parsing robots rules - can't decode path: " + path, state.getUrl());
         }

         if (path.length() == 0) {
             // Allow: <nothing> => allow all.
             state.clearRules();
         } else {
             state.addRule(path, true);
         }
         
         return true;
     }

    /**
     * Handle the crawl-delay: directive
     * 
     * @param state current parsing state
     * @param token data for directive
     * @return true to keep going, false if we're done
     */
    private boolean handleCrawlDelay(ParseState state, RobotToken token) {
        state.setFinishedAgentFields(true);
        
        if (!state.isAddingRules()) {
             return true;
        }
        
        String delayString = token.getData();
        if (delayString.length() > 0) {
            try {
                // Some sites use values like 0.5 for the delay.
                if (delayString.indexOf('.') != -1) {
                    double delayValue = Double.parseDouble(delayString) * 1000.0;
                    state.setCrawlDelay(Math.round(delayValue));
                } else {
                    long delayValue = Integer.parseInt(delayString) * 1000L; // sec to millisec
                    state.setCrawlDelay(delayValue);
                }
            } catch (Exception e) {
                reportWarning("Error parsing robots rules - can't decode crawl delay: " + delayString, state.getUrl());
            }
        }
        
        return true;
    }
    
    /**
     * Handle the sitemap: directive
     * 
     * @param state current parsing state
     * @param token data for directive
     * @return true to keep going, false if we're done
     */
    private boolean handleSitemap(ParseState state, RobotToken token) {
        
        String sitemap = token.getData();
        try {
            String hostname = new URL(sitemap).getHost();
            if ((hostname != null) && (hostname.length() > 0)) {
                hostname = new URI(sitemap).getHost();
                if ((hostname != null) && (hostname.length() > 0)) {
                    state.addSitemap(sitemap);
                }
            }
        } catch (Exception e) {
            reportWarning("Invalid URL with sitemap directive: " + sitemap, state.getUrl());
        }
        
        return true;
    }
    
    /**
     * Handle a line that starts with http: and contains "sitemap", which we treat as a
     * missing sitemap: xxx directive.
     * 
     * @param state current parsing state
     * @param token data for directive
     * @return true to keep going, false if we're done
     */
    private boolean handleHttp(ParseState state, RobotToken token) {
        String urlFragment = token.getData();
        if (urlFragment.contains("sitemap")) {
            RobotToken fixedToken = new RobotToken(RobotDirective.SITEMAP, "http:" + token.getData());
            return handleSitemap(state, fixedToken);
        } else {
            reportWarning("Found raw non-sitemap URL: http:" + urlFragment, state.getUrl());
            return true;
        }
    }
    
    // For testing
    public int getNumWarnings() {
        return _numWarnings;
    }
    
}
