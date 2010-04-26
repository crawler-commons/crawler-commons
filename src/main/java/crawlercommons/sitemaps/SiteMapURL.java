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

// JDK imports
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * The SitemapUrl class represents a URL found in a Sitemap.
 * 
 * @author fmccown
 */
public class SiteMapURL {

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
    private double priority;

    public SiteMapURL(String url) {
        setUrl(url);
    }

    public SiteMapURL(URL url) {
        setUrl(url);
    }

    public SiteMapURL(String url, String lastModified, String changeFreq, String priority) {

        setUrl(url);
        setLastModified(lastModified);
        setChangeFrequency(changeFreq);
        setPriority(priority);
    }

    public SiteMapURL(URL url, Date lastModified, ChangeFrequency changeFreq, double priority) {

        setUrl(url);
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
     */
    public void setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            // e.printStackTrace();
            System.out.println("Bad url: [" + url + "]");
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
     * Set the URL's priority to a value between [0.0 - 1.0] (0.0 is used if the
     * given priority is out of range).
     * 
     * @param priority
     */
    public void setPriority(double priority) {

        // Ensure proper value
        if (priority < 0.0 || priority > 1.0) {
            this.priority = 0.0;
        } else {
            this.priority = priority;
        }
    }

    /**
     * Set the URL's priority to a value between [0.0 - 1.0] (0.0 is used if the
     * given priority is out of range).
     * 
     * @param priority
     */
    public void setPriority(String priority) {

        if (priority != null && priority.length() > 0) {
            try {
                setPriority(Double.parseDouble(priority));
            } catch (NumberFormatException e) {
                setPriority(0.0);
            }
        } else {
            setPriority(0.0);
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

    public String toString() {
        String s = "url=\"" + url + "\",";
        s += "lastMod=";
        s += (lastModified == null) ? "null" : SiteMap.getFullDateFormat().format(lastModified);
        s += ",changeFreq=" + changeFreq;
        s += ",priority=" + priority;
        return s;
    }

}
