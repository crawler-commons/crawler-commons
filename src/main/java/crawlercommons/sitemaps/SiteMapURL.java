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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import crawlercommons.sitemaps.extension.Extension;
import crawlercommons.sitemaps.extension.ExtensionMetadata;

/**
 * The SitemapUrl class represents a URL found in a Sitemap.
 * 
 * @author fmccown
 */
public class SiteMapURL {
    private static final Logger LOG = LoggerFactory.getLogger(SiteMapURL.class);
    public static final double DEFAULT_PRIORITY = 0.5;

    /**
     * Allowed change frequencies
     */
    public enum ChangeFrequency {
        ALWAYS, HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY, NEVER
    }

    ;

    /**
     * URL found in Sitemap (required)
     */
    private URL url;

    /**
     * When URL was last modified (optional)
     */
    private Date lastModified;

    /**
     * How often the URL changes (optional)
     */
    private ChangeFrequency changeFreq;

    /**
     * Value between [0.0 - 1.0] (optional)
     */
    private double priority = DEFAULT_PRIORITY;

    /**
     * could be false, if URL isn't found under base path as indicated here:
     * http://www.sitemaps.org/protocol.html#location *
     */
    private boolean valid;

    /**
     * attributes from sitemap extensions (news, image, video sitemaps, etc.)
     */
    private Map<Extension, ExtensionMetadata[]> attributes;

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

    public SiteMapURL(URL url, ZonedDateTime lastModified, ChangeFrequency changeFreq, double priority, boolean valid) {
        this(url, Date.from(lastModified.toInstant()), changeFreq, priority, valid);
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
     *            of the sitemap
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     * Set the URL.
     * 
     * @param url
     *            In case of Malformed URL, the current url in this instance
     *            will be set to NULL
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
     *            lastmod specified for the URL
     */
    public void setLastModified(String lastModified) {
        this.lastModified = SiteMap.convertToDate(lastModified);
    }

    /**
     * Set when this URL was last modified.
     * 
     * @param lastModified
     *            lastmod specified for the URL
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Set when this URL was last modified.
     * 
     * @param lastModified
     *            lastmod specified for the URL
     */
    public void setLastModified(ZonedDateTime lastModified) {
        if (lastModified != null) {
            this.lastModified = Date.from(lastModified.toInstant());
        }
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
     * Set the URL's priority to a value between [0.0 - 1.0] (Default Priority
     * is used if the given priority is out of range).
     * 
     * @param priority
     *            a value between [0.0 - 1.0]
     */
    public void setPriority(double priority) {

        // Ensure proper value
        if (priority < 0.0 || priority > 1.0) {
            this.priority = DEFAULT_PRIORITY;
            LOG.warn("Can't set the priority to {}, Priority should be between 0 to 1, reverting to default priority value: {}", priority, DEFAULT_PRIORITY);
        } else {
            this.priority = priority;
        }
    }

    /**
     * Set the URL's priority to a value between [0.0 - 1.0] (Default Priority
     * is used if the given priority missing or is out of range).
     * 
     * @param priorityStr
     *            a value between [0.0 - 1.0]
     */
    public void setPriority(String priorityStr) {
        try {
            if (priorityStr == null || priorityStr.isEmpty()) {
                LOG.debug("This item contains no priority (which is ok as text sitemaps don't have priority for example), defaulting priority value to: {}", DEFAULT_PRIORITY);
                this.priority = DEFAULT_PRIORITY;
            } else {
                setPriority(Double.parseDouble(priorityStr));
            }
        } catch (NumberFormatException nfe) {
            LOG.warn("Can't set the priority, because I can't understand this value: {}, Priority should be between 0 to 1, reverting to default priority value: {}", priorityStr, DEFAULT_PRIORITY);
            this.priority = DEFAULT_PRIORITY;
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
     *            a {@link crawlercommons.sitemaps.SiteMapURL.ChangeFrequency}
     *            for this sitemap
     */
    public void setChangeFrequency(ChangeFrequency changeFreq) {
        this.changeFreq = changeFreq;
    }

    /**
     * Set the URL's change frequency In case of a bad ChangeFrequency, the
     * current frequency in this instance will be set to NULL
     * 
     * @param changeFreq
     *            a string representing a
     *            {@link crawlercommons.sitemaps.SiteMapURL.ChangeFrequency} for
     *            this sitemap
     */
    public void setChangeFrequency(String changeFreq) {

        if (changeFreq != null) {
            changeFreq = changeFreq.toUpperCase(Locale.ROOT);

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

    /**
     * Valid means that it follows the official guidelines that the siteMapURL
     * must be under the base url
     * 
     * @param valid
     *            whether the Sitemap is valid syntax or not
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * Is the siteMapURL under the base url ?
     * 
     * @return true if the syntax is valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Add attributes of a specific sitemap extension
     * 
     * @param extension
     *            sitemap extension (news, images, videos, etc.)
     * @param attributes
     *            array of attributes
     */
    public void addAttributesForExtension(Extension extension, ExtensionMetadata[] attributes) {
        if (this.attributes == null) {
            this.attributes = new TreeMap<>();
        }
        this.attributes.put(extension, attributes);
    }

    /**
     * Get attributes of sitemap extensions (news, images, videos, etc.)
     * 
     * @return attribute map or null if no extensions are used
     */
    public Map<Extension, ExtensionMetadata[]> getAttributes() {
        return attributes;
    }

    /**
     * Get attributes of a specific sitemap extension
     * 
     * @param extension
     *            sitemap extension (news, images, videos, etc.)
     * @return array of attributes or null if there are no attributes for the
     *         given extension
     */
    public ExtensionMetadata[] getAttributesForExtension(Extension extension) {
        if (attributes == null) {
            return null;
        }
        return attributes.get(extension);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SiteMapURL that = (SiteMapURL) o;

        if (!url.equals(that.url))
            return false;

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
        sb.append(", lastMod = ").append((lastModified == null) ? "null" : SiteMap.W3C_FULLDATE_FORMATTER_UTC.format(lastModified.toInstant()));
        sb.append(", changeFreq = ").append(changeFreq);
        sb.append(", priority = ").append(priority);
        if (attributes != null) {
            for (Entry<Extension, ExtensionMetadata[]> e : attributes.entrySet()) {
                for (ExtensionMetadata m : e.getValue()) {
                    sb.append(", ").append(m.toString());
                }
            }
        }

        return sb.toString();
    }
}