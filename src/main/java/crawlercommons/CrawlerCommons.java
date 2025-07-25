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

package crawlercommons;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Crawler-Commons is a set of reusable Java components that implement
 * functionality common to web crawlers: robots.txt and sitemap parsing, or URL
 * normalization.
 */
public class CrawlerCommons {

    public static String getVersion() {
        String path = "/version.prop";
        InputStream stream = CrawlerCommons.class.getResourceAsStream(path);
        if (stream == null) {
            return "Unknown Version";
        }

        Properties props = new Properties();
        try {
            props.load(stream);
            stream.close();
            return (String) props.get("version");
        } catch (IOException e) {
            return "Unknown Version";
        }
    }
}
