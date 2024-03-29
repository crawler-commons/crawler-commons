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
import java.util.Collection;

/** Robots.txt parser definition. */
@SuppressWarnings("serial")
public abstract class BaseRobotsParser implements Serializable {

    /**
     * Parse the robots.txt file in <i>content</i>, and return rules appropriate
     * for processing paths by <i>userAgent</i>. Note that multiple agent names
     * may be provided as comma-separated values. How agent names are matched
     * against user-agent lines in the robots.txt depends on the implementing
     * class.
     * 
     * Also note that names are lower-cased before comparison, and that any
     * robot name you pass shouldn't contain commas or spaces; if the name has
     * spaces, it will be split into multiple names, each of which will be
     * compared against agent names in the robots.txt file. An agent name is
     * considered a match if it's a prefix match on the provided robot name. For
     * example, if you pass in "Mozilla Crawlerbot-super 1.0", this would match
     * "crawlerbot" as the agent name, because of splitting on spaces,
     * lower-casing, and the prefix match rule.
     * 
     * @deprecated since 1.4 - replaced by {@link parseContent(String, byte[],
     *             String, Collection)}. Passing a collection of robot names
     *             gives users more control how user-agent and robot names are
     *             matched. Passing a list of names is also more efficient as it
     *             does not require to split the robot name string again and
     *             again on every robots.txt file to be parsed.
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
     *            name(s) of crawler, to be used when processing file contents
     *            (just the name portion, w/o version or other details)
     * @return robot rules.
     */
    @Deprecated
    public abstract BaseRobotRules parseContent(String url, byte[] content, String contentType, String robotNames);

    /**
     * Parse the robots.txt file in <i>content</i>, and return rules appropriate
     * for processing paths by <i>userAgent</i>. Multiple agent names can be
     * provided as collection. How agent names are matched against user-agent
     * lines in the robots.txt depends on the implementing class.
     * 
     * @param url
     *            URL that robots.txt content was fetched from. A complete and
     *            valid URL (e.g., https://example.com/robots.txt) is expected.
     *            Used to resolve relative sitemap URLs and for
     *            logging/reporting purposes.
     * @param content
     *            raw bytes from the site's robots.txt file
     * @param contentType
     *            content type (MIME type) from HTTP response header
     * @param robotNames
     *            name(s) of crawler, used to select rules from the robots.txt
     *            file by matching the names against the user-agent lines in the
     *            robots.txt file.
     * @return robot rules.
     */
    public abstract BaseRobotRules parseContent(String url, byte[] content, String contentType, Collection<String> robotNames);

    /**
     * The fetch of robots.txt failed, so return rules appropriate for the given
     * HTTP status code.
     * 
     * @param httpStatusCode
     *            a failure status code (NOT 2xx)
     * @return robot rules
     */
    public abstract BaseRobotRules failedFetch(int httpStatusCode);

}
