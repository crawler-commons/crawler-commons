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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

/** SiteMap or SiteMapIndex**/
public abstract class AbstractSiteMap {
	
    /** Various Sitemap types */
    public enum SitemapType {
        INDEX, XML, ATOM, RSS, TEXT
    };

    // 1997-07-16T19:20+01:00
    private static final Pattern W3C_NO_SECONDS_PATTERN = Pattern.compile("(\\d\\d\\d\\d\\-\\d\\d\\-\\d\\dT\\d\\d:\\d\\d)(\\-|\\+)(\\d\\d):(\\d\\d)");
    private static final ThreadLocal<DateFormat> W3C_NO_SECONDS_FORMAT = new ThreadLocal<DateFormat>() {
        
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        }
    };
    
    private static final ThreadLocal<DateFormat> W3C_FULLDATE_FORMAT = new ThreadLocal<DateFormat>() {
        
        protected DateFormat initialValue() {
            SimpleDateFormat result = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            result.setTimeZone(TimeZone.getTimeZone("UTC"));
            return result;
        }
    };
    
    /** W3C date the Sitemap was last modified */
    private Date lastModified;
    
    /** This Sitemap's type */
    private SitemapType type;

    /** indicate if the Sitemap has been processed. */
    private boolean processed;
    
    protected URL url;
    
    public AbstractSiteMap() {
        lastModified = null;
    }
    
    public static DateFormat getFullDateFormat() {
        return W3C_FULLDATE_FORMAT.get();
    }

    public boolean isIndex() {
    	return (type == SitemapType.INDEX);
    };
    
    /**
     * @return the URL of the Sitemap
     */
    public URL getUrl() {
        return url;
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
    
    /**
     * @param processed
     *            - indicate if the Sitemap has been processed.
     */
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    /**
     * @return true if the Sitemap has been processed i.e it contains at least one SiteMapURL
     */
    public boolean isProcessed() {
        return processed;
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

    /**
     * Convert the given date (given in an acceptable DateFormat), null if the
     * date is not in the correct format.
     * 
     * @param date
     *            - the date to be parsed
     * @return the Date equivalent or NULL when encountering an unparsable date string argument
     */
    public static Date convertToDate(String date) {

        if (date != null) {
            try {
                return DatatypeConverter.parseDateTime(date).getTime();
            } catch (IllegalArgumentException e) {
                // See if it's the one W3C case that the javax.xml.bind implementation (incorrectly) doesn't handle.
                Matcher m = W3C_NO_SECONDS_PATTERN.matcher(date);
                if (m.matches()) {
                    try {
                        // Convert to a format that Java can parse, which means time zone has to be "-/+HHMM", not "+/-HH:MM"
                        StringBuffer mungedDate = new StringBuffer(m.group(1));
                        mungedDate.append(m.group(2));
                        mungedDate.append(m.group(3));
                        mungedDate.append(m.group(4));
                        return W3C_NO_SECONDS_FORMAT.get().parse(mungedDate.toString());
                    } catch (ParseException e2) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }
}