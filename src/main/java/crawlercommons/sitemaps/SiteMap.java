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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class SiteMap extends AbstractSiteMap {

    /**
     * The base URL for the Sitemap is where the Sitemap was found If found at
     * http://foo.org/abc/sitemap.xml then baseUrl is http://foo.org/abc/
     * Sitemaps can only contain URLs that are under the base URL.
     */
    private String baseUrl;

    /** URLs found in this Sitemap */
    private List<SiteMapURL> urlList;

    public SiteMap() {
        super();

        urlList = new ArrayList<SiteMapURL>();
        setProcessed(false);
    }

    public SiteMap(URL url) {
        this();
        setUrl(url);
    }

    public SiteMap(String url) {
        this();
        setUrl(url);
    }

    public SiteMap(URL url, Date lastModified) {
        this(url);
        setLastModified(lastModified);
    }

    public SiteMap(String url, String lastModified) {
        this(url);
        setLastModified(lastModified);
    }

    /**
     * @return the Collection of SitemapUrls in this Sitemap.
     */
    public Collection<SiteMapURL> getSiteMapUrls() {
        return urlList;
    }

    /**
     * @param url
     *            - the URL of the Sitemap
     */
    private void setUrl(URL url) {
        this.url = url;
        setBaseUrl(url);
    }

    /**
     * @param url
     *            - the URL of the Sitemap In the case of a Malformed URL the
     *            URL member is set to null
     */
    private void setUrl(String url) {
        try {
            this.url = new URL(url);

            setBaseUrl(this.url);
        } catch (MalformedURLException e) {
            this.url = null;
        }
    }

    public String toString() {
        // Leaving here blank comments so the formatter won't put these into one
        // ugly line
        StringBuilder sb = new StringBuilder().append("url = \"") //
                        .append(url).append("\", lastMod = ") //
                        .append((getLastModified() == null) ? "null" : SiteMap.W3C_FULLDATE_FORMATTER_UTC.format(getLastModified().toInstant())) //
                        .append(", type = ").append(getType()) //
                        .append(", processed = ") //
                        .append(isProcessed()) //
                        .append(", urlListSize = ") //
                        .append(urlList.size());

        return sb.toString();
    }

    /**
     * This is private because only once we know the Sitemap's URL can we
     * determine the base URL.
     * 
     * @param sitemapUrl
     */
    private void setBaseUrl(URL sitemapUrl) {
        // Remove everything back to last slash on the file path:
        // - http://example.com/abc/sitemap.xml
        // becomes
        // - http://example.com/abc/
        // But ignore slashes in the query part:
        // - http://example.com/index.php?route=feed/imagemap
        // becomes
        // - http://example.com/
        String path = sitemapUrl.getPath();
        int lastPathDelimPos = path.lastIndexOf('/');
        if (lastPathDelimPos < 0) {
            path = "/";
        } else {
            path = path.substring(0, lastPathDelimPos + 1);
        }
        try {
            baseUrl = new URL(sitemapUrl.getProtocol(), sitemapUrl.getHost(), sitemapUrl.getPort(), path).toString();
        } catch (MalformedURLException e) {
            baseUrl = sitemapUrl.toString();
        }
    }

    /**
     * @return the baseUrl for this Sitemap.
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @param url
     *            The SitemapUrl to be added to the Sitemap.
     */
    public void addSiteMapUrl(SiteMapURL url) {
        urlList.add(url);
    }

    public boolean isIndex() {
        return false;
    }
}