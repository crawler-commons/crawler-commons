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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;

public class SiteMap extends AbstractSiteMap {


    /**
     * The base URL for the Sitemap is where the Sitemap was found If found at
     * http://foo.org/abc/sitemap.xml then baseUrl is http://foo.org/abc/
     * Sitemaps can only contain URLs that are under the base URL.
     */
    private String baseUrl;

    /** URLs found in this Sitemap */
    private Hashtable<String, SiteMapURL> urlList;

    public SiteMap() {
    	super();
        urlList = new Hashtable<String, SiteMapURL>();
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
        return urlList.values();
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
     *            - the URL of the Sitemap
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
        String s = "url=\"" + url + "\",lastMod=";
        s += (getLastModified() == null) ? "null" : SiteMap.getFullDateFormat().format(getLastModified());
        s += ",type=" + getType() + ",processed=" + isProcessed() + ",urlListSize=" + urlList.size();
        return s;
    }


    /**
     * This is private because only once we know the Sitemap's URL can we
     * determine the base URL.
     * 
     * @param sitemapUrl
     */
    private void setBaseUrl(URL sitemapUrl) {
        baseUrl = sitemapUrl.toString().toLowerCase();

        // baseUrl = "http://foo.org/abc/sitemap.xml";

        // Remove everything back to last slash.
        // So http://foo.org/abc/sitemap.xml becomes http://foo.org/abc/
        baseUrl = baseUrl.replaceFirst("/[^/]*$", "/");
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
        urlList.put(url.getUrl().toString(), url);
    }

    public boolean isIndex() {
        return false;
    }

}
