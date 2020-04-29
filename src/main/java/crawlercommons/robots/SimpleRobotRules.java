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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result from parsing a single robots.txt file - which means we get a set of
 * rules, and an optional crawl-delay, and an optional sitemap URL. Note that we
 * support Google's extensions (Allow directive and '$'/'*' special chars) plus
 * the more widely used Sitemap directive.
 * 
 * See https://en.wikipedia.org/wiki/Robots_exclusion_standard See
 * https://developers.google.com/search/reference/robots_txt
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

        // Sort from longest to shortest rules.
        @Override
        public int compareTo(RobotRule o) {
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

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (_allow ? 1231 : 1237);
            result = prime * result + ((_prefix == null) ? 0 : _prefix.hashCode());
            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
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

    public List<RobotRule> getRobotRules() {
        return this._rules;
    }

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

            for (RobotRule rule : _rules) {
                if (ruleMatches(pathWithQuery, rule._prefix)) {
                    return rule._allow;
                }
            }

            return true;
        }
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

            // We used to lower-case the path, but Google says we need to do
            // case-sensitive matching.
            return URLDecoder.decode(path, "UTF-8");
        } catch (Exception e) {
            // If the URL is invalid, we don't really care since the fetch
            // will fail, so return the root.
            return "/";
        }
    }

    private boolean ruleMatches(String text, String pattern) {
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
                    return true;
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
                    return false;
                }
            } else {
                // See if the pattern from patternPos to wildcardPos matches the
                // text starting at textPos
                while ((patternPos < wildcardPos) && (textPos < textEnd)) {
                    if (text.charAt(textPos++) != pattern.charAt(patternPos++)) {
                        return false;
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
        return (patternPos == patternEnd) && ((textPos == textEnd) || !containsEndChar);
    }

    /**
     * In order to match up with Google's convention, we want to match rules
     * from longest to shortest. So sort the rules.
     */
    public void sortRules() {
        Collections.sort(_rules);
    }

    /**
     * Is our ruleset set up to allow all access?
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
     * @return true if no URLs are allowed.
     */
    @Override
    public boolean isAllowNone() {
        return _mode == RobotRulesMode.ALLOW_NONE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((_mode == null) ? 0 : _mode.hashCode());
        result = prime * result + ((_rules == null) ? 0 : _rules.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
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
