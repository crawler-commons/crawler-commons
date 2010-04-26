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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;

public class SiteMap extends AbstractSiteMap {

    /** Various Sitemap types */
    public enum SitemapType {
        XML, ATOM, RSS, TEXT
    };

    /** W3C date the Sitemap was last modified */
    private Date lastModified;

    /** Indicates if we have tried to process this Sitemap or not */
    private boolean processed;

    /** This Sitemap's type */
    private SitemapType type;

    /**
     * The base URL for the Sitemap is where the Sitemap was found If found at
     * http://foo.org/abc/sitemap.xml then baseUrl is http://foo.org/abc/
     * Sitemaps can only contain URLs that are under the base URL.
     */
    private String baseUrl;

    /** URLs found in this Sitemap */
    private Hashtable<String, SiteMapURL> urlList;

    public static DateFormat getFullDateFormat() {
        return dateFormats.get()[1];
    }

    /**
     * lastModified uses the W3C date format
     * (http://www.w3.org/TR/NOTE-datetime)
     */
    private static final ThreadLocal<DateFormat[]> dateFormats = new ThreadLocal<DateFormat[]>() {
        protected DateFormat[] initialValue() {
            return new DateFormat[] { new SimpleDateFormat("yyyy-MM-dd"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm+hh:00"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm-hh:00"),
                            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+hh:00"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-hh:00"), new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz") };
        }
    };

    public SiteMap() {
        urlList = new Hashtable<String, SiteMapURL>();
        lastModified = null;
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

    /**
     * @param lastModified
     *            - the lastModified to set
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @param lastModified
     *            - the lastModified to set
     */
    public void setLastModified(String lastModified) {
        this.lastModified = SiteMap.convertToDate(lastModified);
    }

    /**
     * @return the lastModified date of the Sitemap
     */
    public Date getLastModified() {
        return lastModified;
    }

    public String toString() {
        String s = "url=\"" + url + "\",lastMod=";
        s += (lastModified == null) ? "null" : SiteMap.getFullDateFormat().format(lastModified);
        s += ",type=" + type + ",processed=" + processed + ",urlListSize=" + urlList.size();
        return s;
    }

    /**
     * Convert the given date (given in an acceptable DateFormat), null if the
     * date is not in the correct format.
     * 
     * @param date
     *            - the date to be parsed
     * @return the Date equivalent
     */
    public static Date convertToDate(String date) {

        if (date != null) {
            for (DateFormat df : dateFormats.get()) {
                try {
                    return df.parse(date);
                } catch (ParseException e) {
                    // do nothing
                }
            }
        }

        // Not successful parsing any dates
        return null;
    }

    /**
     * @param processed
     *            - indicate if the Sitemap has been processed.
     */
    void setProcessed(boolean processed) {
        this.processed = processed;
    }

    /**
     * @return true if the Sitemap has been processed i.e it contains at least
     *         one SiteMapURL
     */
    public boolean isProcessed() {
        return processed;
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

    /**
     * @param type
     *            the Sitemap type to set
     */
    void setType(SitemapType type) {
        this.type = type;
    }

    /**
     * @return the Sitemap type
     */
    public SitemapType getType() {
        return type;
    }

    public boolean isIndex() {
        return false;
    }

}
