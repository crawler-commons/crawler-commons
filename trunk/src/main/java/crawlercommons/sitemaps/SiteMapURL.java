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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * The SitemapUrl class represents a URL found in a Sitemap.
 * 
 * @author fmccown
 */
public class SiteMapURL {
    private static Logger LOG = LoggerFactory.getLogger(SiteMapURL.class);
    public static double defaultPriority = 0.5;

    /** Allowed change frequencies */
    public enum ChangeFrequency {
        ALWAYS, HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY, NEVER
    };

    /** URL found in Sitemap (required) */
    private URL url;

    /** When URL was last modified (optional) */
    private Date lastModified;

    /** How often the URL changes (optional) */
    private ChangeFrequency changeFreq;

    /** Value between [0.0 - 1.0] (optional) */
    private double priority = defaultPriority;

    /** could be false, if URL isn't found under base path **/
    private boolean valid;
    
    public SiteMapURL(String url, boolean valid) {
        setUrl(url);
        setValid(valid);
    }

    public SiteMapURL(URL url, boolean valid) {
        setUrl(url);
        setValid(valid);
    }

    public SiteMapURL(String url, String lastModified, String changeFreq, String priority, boolean valid) {
        this(url, valid);
        setLastModified(lastModified);
        setChangeFrequency(changeFreq);
        setPriority(priority);
    }

    public SiteMapURL(URL url, Date lastModified, ChangeFrequency changeFreq, double priority, boolean valid) {
        this(url, valid);
        setLastModified(lastModified);
        setChangeFrequency(changeFreq);
        setPriority(priority);
    }

    /**
     * Return the URL.
     * 
     * @return URL
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Set the URL.
     * 
     * @param url
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     * Set the URL.
     * 
     * @param url
     * In case of Malformed URL, the current url in this instance will be set to NULL
     */
    public void setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            LOG.error("Bad url: [{}], Exception: {}", url, e.toString());
            this.url = null;
        }
    }

    /**
     * Return when this URL was last modified.
     * 
     * @return last modified date
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Set when this URL was last modified.
     * 
     * @param lastModified
     */
    public void setLastModified(String lastModified) {
        this.lastModified = SiteMap.convertToDate(lastModified);
    }

    /**
     * Set when this URL was last modified.
     * 
     * @param lastModified
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Return this URL's priority (a value between [0.0 - 1.0]).
     * 
     * @return URL's priority (a value between [0.0 - 1.0])
     */
    public double getPriority() {
        return priority;
    }

    /**
     * Set the URL's priority to a value between [0.0 - 1.0] (Default Priority is used if the
     * given priority is out of range).
     * 
     * @param priority
     */
    public void setPriority(double priority) {

        // Ensure proper value
        if (priority < 0.0 || priority > 1.0) {
            this.priority = defaultPriority;
            LOG.warn("Can't set the priority to {}, Priority should be between 0 to 1, reverting to default priority value: {}", priority, defaultPriority);
        } else {
            this.priority = priority;
        }
    }

    /**
     * Set the URL's priority to a value between [0.0 - 1.0] (Default Priority is used if the
     * given priority missing or is out of range).
     * 
     * @param priorityStr
     */
    public void setPriority(String priorityStr) {
        try {
            if (priorityStr == null || priorityStr.isEmpty()) {
                LOG.debug("This item contains no priority (which is ok as text sitemaps don't have priority for example), defaulting priority value to: {}", defaultPriority);
                this.priority = defaultPriority;
            } else {
                setPriority(Double.parseDouble(priorityStr));
            }
        } catch (NumberFormatException nfe) {
            LOG.warn("Can't set the priority, because I can't understand this value: {}, Priority should be between 0 to 1, reverting to default priority value: {}", priorityStr, defaultPriority);
            this.priority = defaultPriority;
        }
    }

    /**
     * Return the URL's change frequency
     * 
     * @return the URL's change frequency
     */
    public ChangeFrequency getChangeFrequency() {
        return changeFreq;
    }

    /**
     * Set the URL's change frequency
     * 
     * @param changeFreq
     */
    public void setChangeFrequency(ChangeFrequency changeFreq) {
        this.changeFreq = changeFreq;
    }

    /**
     * Set the URL's change frequency
     * In case of a bad ChangeFrequency, the current frequency in this instance will be set to NULL
     * 
     * @param changeFreq
     */
    public void setChangeFrequency(String changeFreq) {

        if (changeFreq != null) {
            changeFreq = changeFreq.toUpperCase();

            if (changeFreq.contains("ALWAYS")) {
                this.changeFreq = ChangeFrequency.ALWAYS;
            } else if (changeFreq.contains("HOURLY")) {
                this.changeFreq = ChangeFrequency.HOURLY;
            } else if (changeFreq.contains("DAILY")) {
                this.changeFreq = ChangeFrequency.DAILY;
            } else if (changeFreq.contains("WEEKLY")) {
                this.changeFreq = ChangeFrequency.WEEKLY;
            } else if (changeFreq.contains("MONTHLY")) {
                this.changeFreq = ChangeFrequency.MONTHLY;
            } else if (changeFreq.contains("YEARLY")) {
                this.changeFreq = ChangeFrequency.YEARLY;
            } else if (changeFreq.contains("NEVER")) {
                this.changeFreq = ChangeFrequency.NEVER;
            } else {
                this.changeFreq = null;
            }
        }
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SiteMapURL that = (SiteMapURL) o;

        if (!url.equals(that.url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("url = \"").append(url).append("\"");
        sb.append(", lastMod = ").append((lastModified == null) ? "null" : SiteMap.getFullDateFormat().format(lastModified));
        sb.append(", changeFreq = ").append(changeFreq);
        sb.append(", priority = ").append(priority);

        return sb.toString();
    }
}