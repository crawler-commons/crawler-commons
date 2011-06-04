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

import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Result from parsing a single robots.txt file - which means we
 * get a set of rules, and a crawl-delay.
 */

public class SimpleRobotRules extends BaseRobotRules {

    public enum RobotRulesMode {
        ALLOW_ALL,
        ALLOW_NONE,
        ALLOW_SOME
    }

    /**
     * Single rule that maps from a path prefix to an allow flag.
     */
    protected class RobotRule {
        String _prefix;
        Pattern _pattern;
        boolean _allow;

        public RobotRule(String prefix, boolean allow) {
            _prefix = prefix;
            _pattern = null;
            _allow = allow;
        }

        public RobotRule(Pattern pattern, boolean allow) {
            _prefix = null;
            _pattern = pattern;
            _allow = allow;
        }
    }


    private ArrayList<RobotRule> _rules;
    private RobotRulesMode _mode;
    
    public SimpleRobotRules() {
        this(RobotRulesMode.ALLOW_SOME);
    }
    
    public SimpleRobotRules(RobotRulesMode mode) {
        super();
        
        _mode = mode;
        _rules = new ArrayList<RobotRule>();
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

    // TODO KKr - make sure paths are sorted from longest to shortest,
    // to implement longest match
    public boolean isAllowed(String url) {
        if (_mode == RobotRulesMode.ALLOW_NONE) {
            return false;
        } else if (_mode == RobotRulesMode.ALLOW_ALL) {
            return true;
        } else {
            String path = getPath(url);
            
            // Always allow robots.txt
            if (path.equals("/robots.txt")) {
                return true;
            }

            for (RobotRule rule : _rules) {
                if (path.startsWith(rule._prefix)) {
                    return rule._allow;
                }
            }

            return true;
        }
    }

    private String getPath(String url) {

        try {
            String path = new URL(url).getPath();
            
            if ((path == null) || (path.equals(""))) {
                return "/";
            } else {
                // We always lower-case the path, as anybody who sets up rules that differ only by case
                // is insane, but it's more likely that somebody will accidentally put in rules that don't
                // match their target paths because of case differences.
                return URLDecoder.decode(path, "UTF-8").toLowerCase();
            }
        } catch (Exception e) {
            // If the URL is invalid, we don't really care since the fetch
            // will fail, so return the root.
            return "/";
        }
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
}
