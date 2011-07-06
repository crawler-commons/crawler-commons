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

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class BaseRobotsParser implements Serializable {

    /**
     * Parse the robots.txt file in <content>, and return rules appropriate for
     * processing paths by <userAgent>
     * 
     * @param url URL that content was fetched from (for reporting purposes)
     * @param content raw bytes from the site's robots.txt file
     * @param contentType HTTP response header (mime-type)
     * @param robotName name of crawler, to be used when processing file contents
     *        (just the name portion, w/o version or other details)
     * @return robot rules.
     */
    
    public abstract BaseRobotRules parseContent(String url, byte[] content, String contentType, String robotName);
    
    
    /**
     * The fetch of robots.txt failed, so return rules appropriate give the
     * HTTP status code.
     * 
     * @param httpStatusCode a failure status code (NOT 2xx)
     * @return robot rules
     */
    public abstract BaseRobotRules failedFetch(int httpStatusCode);
}
