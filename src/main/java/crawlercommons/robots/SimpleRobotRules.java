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

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import crawlercommons.filters.basic.BasicURLNormalizer;

/**
 * {@inheritDoc}
 * 
 * <p>
 * Allow/disallow rules are matched following the <a
 * href="https://www.rfc-editor.org/rfc/rfc9309.html">Robots Exclusion Protocol
 * RFC 9309</a>. This includes <a href=
 * "https://developers.google.com/search/reference/robots_txt">Google's
 * robots.txt extensions</a> to the <a
 * href="http://www.robotstxt.org/robotstxt.html">original RFC draft</a>: the
 * <code>Allow</code> directive, <code>$</code>/<code>*</code> special
 * characters and precedence of longer (more specific) patterns.
 * </p>
 * 
 * <p>
 * See also: <a
 * href="https://en.wikipedia.org/wiki/Robots_exclusion_standard">Robots
 * Exclusion on Wikipedia</a>
 * </p>
 */

@SuppressWarnings("serial")
public class SimpleRobotRules extends BaseRobotRules {

    public enum RobotRulesMode {
        ALLOW_ALL, ALLOW_NONE, ALLOW_SOME
    }

    /**
     * Single rule that maps from a path prefix to an allow flag.
     */
    public static class RobotRule implements Comparable<RobotRule>, Serializable {

        String _prefix;
        boolean _allow;

        /**
         * A allow/disallow rule: a path prefix or pattern and whether it is
         * allowed or disallowed.
         */
        public RobotRule(String prefix, boolean allow) {
            _prefix = prefix;
            _allow = allow;
        }

        public boolean isAllow() {
            return this._allow;
        }

        public String getPrefix() {
            return this._prefix;
        }

        @Override
        public int compareTo(RobotRule o) {
            // order from longest to shortest path prefixes/patterns
            if (_prefix.length() < o._prefix.length()) {
                return 1;
            } else if (_prefix.length() > o._prefix.length()) {
                return -1;
            } else if (_allow == o._allow) {
                return 0;
            } else if (_allow) {
                // Allow comes before disallow
                return -1;
            } else {
                return 1;
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (_allow ? 1231 : 1237);
            result = prime * result + ((_prefix == null) ? 0 : _prefix.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            RobotRule other = (RobotRule) obj;
            if (_allow != other._allow)
                return false;
            if (_prefix == null) {
                if (other._prefix != null)
                    return false;
            } else if (!_prefix.equals(other._prefix))
                return false;
            return true;
        }

    }

    protected ArrayList<RobotRule> _rules;
    protected RobotRulesMode _mode;

    /** Special characters which require percent-encoding for path matching */
    protected final static boolean[] specialCharactersPathMatching = new boolean[128];
    static {
        specialCharactersPathMatching['*'] = true;
        specialCharactersPathMatching['$'] = true;
    }

    public SimpleRobotRules() {
        this(RobotRulesMode.ALLOW_SOME);
    }

    public SimpleRobotRules(RobotRulesMode mode) {
        super();

        _mode = mode;
        _rules = new ArrayList<>();
    }

    public void clearRules() {
        _rules.clear();
    }

    public void addRule(String prefix, boolean allow) {
        // Convert old-style case of disallow: <nothing>
        // into new allow: <nothing>.
        if (!allow && (prefix.length() == 0)) {
            allow = true;
        }

        _rules.add(new RobotRule(prefix, allow));
    }

    /**
     * @return the list of allow/disallow rules
     */
    public List<RobotRule> getRobotRules() {
        return this._rules;
    }

    @Override
    public boolean isAllowed(String url) {
        if (_mode == RobotRulesMode.ALLOW_NONE) {
            return false;
        } else if (_mode == RobotRulesMode.ALLOW_ALL) {
            return true;
        } else {
            String pathWithQuery = getPath(url, true);

            // Always allow robots.txt
            if (pathWithQuery.equals("/robots.txt")) {
                return true;
            }

            boolean isAllowed = true;
            int longestRuleMatch = Integer.MIN_VALUE;
            for (RobotRule rule : _rules) {
                int matchLength = ruleMatches(pathWithQuery, rule._prefix);
                if (matchLength == -1) {
                    // See precedence-of-rules test case for an example
                    // Some webmasters expect behavior close to google's, and
                    // this block is equivalent to:
                    // https://github.com/google/robotstxt/blob/02bc6cdfa32db50d42563180c42aeb47042b4f0c/robots.cc#L605-L618
                    // There are example robots.txt in the wild that benefit
                    // from this.
                    // As of 2/7/2022, https://venmo.com/robots.txt for
                    // instance.
                    if (rule._prefix.endsWith("index.htm") || rule._prefix.endsWith("index.html")) {
                        matchLength = ruleMatches(pathWithQuery, rule._prefix.substring(0, rule._prefix.indexOf("index.htm")) + "$");
                        if (matchLength == -1) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }

                if (longestRuleMatch < matchLength) {
                    longestRuleMatch = matchLength;
                    isAllowed = rule.isAllow();
                } else if (longestRuleMatch == matchLength) {
                    isAllowed |= rule.isAllow();
                }
                // else we've already got a more specific rule, and this match
                // doesn't matter
            }

            return isAllowed;
        }
    }

    /**
     * Encode/decode (using percent-encoding) all characters where necessary:
     * encode Unicode/non-ASCII characters) and decode printable ASCII
     * characters without special semantics.
     * 
     * @param urlPathQuery
     *            path and query component of the URL
     * @param additionalEncodedBytes
     *            boolean array to request bytes (ASCII characters) to be
     *            percent-encoded in addition to other characters requiring
     *            encoding (Unicode/non-ASCII and characters not allowed in
     *            URLs).
     * @return properly percent-encoded URL path and query
     */
    public static String escapePath(String urlPathQuery, boolean[] additionalEncodedBytes) {
        return BasicURLNormalizer.escapePath(BasicURLNormalizer.unescapePath(urlPathQuery), additionalEncodedBytes);
    }

    private String getPath(String url, boolean getWithQuery) {

        try {
            URL urlObj = new URL(url);
            String path = urlObj.getPath();
            if ((path == null) || (path.equals(""))) {
                path = "/";
            }

            String query = urlObj.getQuery();
            if (getWithQuery && query != null) {
                path += "?" + query;
            }

            /*
             * We used to lower-case the path, but Google and RFC 9309 require
             * case-sensitive matching.
             * 
             * However, we need to properly decode percent-encoded characters,
             * but preserve those escaped characters which have special
             * semantics in path matching, e.g. slash `/`. However, for the
             * implementation of the path matching requires that asterisk `*`
             * and dollar `$` are exceptionally percent-encoded.
             */
            return escapePath(path, specialCharactersPathMatching);
        } catch (Exception e) {
            // If the URL is invalid, we don't really care since the fetch
            // will fail, so return the root.
            return "/";
        }
    }

    private int ruleMatches(String text, String pattern) {
        int patternPos = 0;
        int textPos = 0;

        int patternEnd = pattern.length();
        int textEnd = text.length();

        boolean containsEndChar = pattern.endsWith("$");
        if (containsEndChar) {
            patternEnd -= 1;
        }

        while ((patternPos < patternEnd) && (textPos < textEnd)) {
            // Find next wildcard in the pattern.
            int wildcardPos = pattern.indexOf('*', patternPos);
            if (wildcardPos == -1) {
                wildcardPos = patternEnd;
            }

            // If we're at a wildcard in the pattern, find the place in the text
            // where the character(s) after the wildcard match up with what's in
            // the text.
            if (wildcardPos == patternPos) {
                patternPos += 1;
                if (patternPos >= patternEnd) {
                    // Pattern ends with '*', we're all good.
                    return pattern.length();
                }

                // TODO - don't worry about having two '*' in a row?

                // Find the end of the pattern piece we need to match.
                int patternPieceEnd = pattern.indexOf('*', patternPos);
                if (patternPieceEnd == -1) {
                    patternPieceEnd = patternEnd;
                }

                boolean matched = false;
                int patternPieceLen = patternPieceEnd - patternPos;
                while ((textPos + patternPieceLen <= textEnd) && !matched) {
                    // See if patternPieceLen chars from text at textPos match
                    // chars from pattern at patternPos
                    matched = true;
                    for (int i = 0; i < patternPieceLen && matched; i++) {
                        if (text.charAt(textPos + i) != pattern.charAt(patternPos + i)) {
                            matched = false;
                        }
                    }

                    // If we matched, we're all set, otherwise we have to
                    // advance textPos
                    if (!matched) {
                        textPos += 1;
                    }
                }

                // If we matched, we're all set, otherwise we failed
                if (!matched) {
                    return -1;
                }
            } else {
                // See if the pattern from patternPos to wildcardPos matches the
                // text starting at textPos
                while ((patternPos < wildcardPos) && (textPos < textEnd)) {
                    if (text.charAt(textPos++) != pattern.charAt(patternPos++)) {
                        return -1;
                    }
                }
            }
        }

        // If we didn't reach the end of the pattern, make sure we're not at a
        // wildcard, that's a 0 or more match, so then we're still OK.
        while ((patternPos < patternEnd) && (pattern.charAt(patternPos) == '*')) {
            patternPos += 1;
        }

        // We're at the end, so we have a match if the pattern was completely
        // consumed, and either we consumed all the text or we didn't have to
        // match it all (no '$' at end of the pattern)
        if ((patternPos == patternEnd) && ((textPos == textEnd) || !containsEndChar)) {
            return pattern.length();
        } else {
            return -1;
        }
    }

    /**
     * Sort and deduplicate robot rules. This method must be called after the
     * robots.txt has been processed and before rule matching.
     * 
     * The ordering is implemented in {@link RobotRule#compareTo(RobotRule)} and
     * defined by <a
     * href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.2.2">RFC
     * 9309, section 2.2.2</a>:
     * 
     * <blockquote>The most specific match found MUST be used. The most specific
     * match is the match that has the most octets. Duplicate rules in a group
     * MAY be deduplicated.</blockquote>
     */
    public void sortRules() {
        if (_rules.size() > 1) {
            _rules = new ArrayList<>(_rules.stream().sorted().distinct().collect(Collectors.toList()));
        }
    }

    /**
     * Is our ruleset set up to allow all access?
     * 
     * <p>
     * Note: This is decided only based on the {@link RobotRulesMode} without
     * inspecting the set of allow/disallow rules.
     * </p>
     * 
     * @return true if all URLs are allowed.
     */
    @Override
    public boolean isAllowAll() {
        return _mode == RobotRulesMode.ALLOW_ALL;
    }

    /**
     * Is our ruleset set up to disallow all access?
     * 
     * <p>
     * Note: This is decided only based on the {@link RobotRulesMode} without
     * inspecting the set of allow/disallow rules.
     * </p>
     * 
     * @return true if no URLs are allowed.
     */
    @Override
    public boolean isAllowNone() {
        return _mode == RobotRulesMode.ALLOW_NONE;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((_mode == null) ? 0 : _mode.hashCode());
        result = prime * result + ((_rules == null) ? 0 : _rules.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleRobotRules other = (SimpleRobotRules) obj;
        if (_mode != other._mode)
            return false;
        if (_rules == null) {
            if (other._rules != null)
                return false;
        } else if (!_rules.equals(other._rules))
            return false;
        return true;
    }

    /*
     * {@inheritDoc}
     * 
     * In addition, the number of allow/disallow rules and the most specific
     * rules by pattern length are shown (ten rules, at maximum).
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        int nRules = _rules.size();
        if (nRules == 0) {
            sb.append(" - no rules");
            if (isAllowNone()) {
                sb.append(" (allow none)");
            } else if (isAllowAll()) {
                sb.append(" (allow all)");
            }
            sb.append('\n');
        } else {
            sb.append(" - number of rules: ").append(nRules).append('\n');
            int numOfRulesToShow = Math.min(nRules, 10);
            for (int i = 0; i < numOfRulesToShow; i++) {
                RobotRule r = _rules.get(i);
                sb.append(r._allow ? "   A" : "   Disa").append("llow: ").append(r._prefix).append('\n');
            }
        }
        return sb.toString();
    }

}
