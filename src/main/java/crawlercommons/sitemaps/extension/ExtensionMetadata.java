/**
 * Copyright 2018 Crawler-Commons
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

package crawlercommons.sitemaps.extension;

import crawlercommons.sitemaps.SiteMapURL;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;

/**
 * Container for attributes of a {@link SiteMapURL} defined by a sitemap
 * extension.
 */
@SuppressWarnings("serial")
public abstract class ExtensionMetadata implements Serializable {

    public abstract boolean equals(Object other);

    public abstract Map<String, String[]> asMap();

    public boolean isValid() {
        return true;
    }

    /**
     * Compare URLs by their string representation because calling
     * {@link URL#equals(Object)} may trigger an unwanted and potentially slow
     * DNS lookup to resolve the host part
     */
    protected static boolean urlEquals(URL a, URL b) {
        return (a == b) || (a != null && a.toString().equals(b.toString()));
    }

}
