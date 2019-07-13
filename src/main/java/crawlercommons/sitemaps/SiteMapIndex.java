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

package crawlercommons.sitemaps;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SiteMapIndex extends AbstractSiteMap {

    /** URLs found in this Sitemap Index */
    private List<AbstractSiteMap> sitemaps;

    public SiteMapIndex() {
        sitemaps = new ArrayList<AbstractSiteMap>();
    }

    public SiteMapIndex(URL url) {
        this();
        this.url = url;
    }

    /**
     * @return a Collection of Sitemaps in this Sitemap Index.
     */
    public Collection<AbstractSiteMap> getSitemaps() {
        return sitemaps;
    }

    /**
     * Add this Sitemap to the list of Sitemaps,
     * 
     * @param sitemap
     *            - Sitemap to be added to the list of Sitemaps
     */
    public void addSitemap(AbstractSiteMap sitemap) {
        sitemaps.add(sitemap);
    }

    /**
     * Returns the Sitemap that has the given URL. Returns null if the URL
     * cannot be found.
     * 
     * @param url
     *            - The Sitemap's URL
     * @return SiteMap corresponding to the URL or null
     */
    public AbstractSiteMap getSitemap(URL url) {
        for (AbstractSiteMap asm : sitemaps) {
            if (asm.getUrl().equals(url)) {
                return asm;
            }
        }

        return null;
    }

    /**
     * @return true if there are Sitemaps in this index that have not been
     *         processed yet, false otherwise.
     */
    public boolean hasUnprocessedSitemap() {

        // Check existence of an unprocessed Sitemap
        return (nextUnprocessedSitemap() != null);
    }

    /**
     * @return an unprocessed Sitemap or null if no unprocessed Sitemaps could
     *         be found.
     */
    public AbstractSiteMap nextUnprocessedSitemap() {
        for (AbstractSiteMap asm : sitemaps) {
            if (!asm.isProcessed()) {
                return asm;
            }
        }

        return null;
    }

    @Override
    public boolean isIndex() {
        return true;
    }

    @Override
    public String toString() {
        return "url = \"" + url + "\", sitemapListSize = " + sitemaps.size();
    }
}