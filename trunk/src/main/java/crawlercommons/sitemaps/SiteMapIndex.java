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

package crawlercommons.sitemaps;

import java.net.URL;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map.Entry;

public class SiteMapIndex extends AbstractSiteMap {

    /** URLs found in this Sitemap Index */
    private Hashtable<String, SiteMap> sitemaps;

    public SiteMapIndex() {
        sitemaps = new Hashtable<String, SiteMap>();
    }

    public SiteMapIndex(URL url) {
        this();
        this.url = url;
    }

    /**
     * @return a Collection of Sitemaps in this Sitemap Index.
     */
    public Collection<SiteMap> getSitemaps() {
        return sitemaps.values();
    }

    /**
     * Add this Sitemap to the list of Sitemaps,
     * 
     * @param sitemap
     *            - Sitemap to be added to the list of Sitemaps
     */
    void addSitemap(SiteMap sitemap) {
        sitemaps.put(sitemap.getUrl().toString(), sitemap);
    }

    /**
     * Returns the Sitemap that has the given URL. Returns null if the URL
     * cannot be found.
     * 
     * @param url
     *            - The Sitemap's URL
     * @return SiteMap corresponding to the URL or null
     */
    public SiteMap getSitemap(URL url) {
        return sitemaps.get(url.toString());
    }

    /**
     * @return true if there are Sitemaps in this index that have not been
     *         processed yet, false otherwise.
     */
    public boolean hasUnprocessedSitemap() {

        // Find an unprocessed Sitemap
        for (Entry<String, SiteMap> sitemap : sitemaps.entrySet()) {
            SiteMap s = sitemap.getValue();
            if (!s.isProcessed()) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return an unprocessed Sitemap or null if no unprocessed Sitemaps could
     *         be found.
     */
    public SiteMap nextUnprocessedSitemap() {
        for (Entry<String, SiteMap> sitemap : sitemaps.entrySet()) {
            SiteMap s = sitemap.getValue();
            if (!s.isProcessed()) {
                return s;
            }
        }

        return null;
    }

    public String toString() {
        return "url=\"" + url + "\",sitemapListSize=" + sitemaps.size();
    }

    public boolean isIndex() {
        return true;
    }

}
