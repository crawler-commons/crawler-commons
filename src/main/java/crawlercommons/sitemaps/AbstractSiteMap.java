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
import java.util.Date;

/** SiteMap or SiteMapIndex**/
public abstract class AbstractSiteMap {
	
    /** Various Sitemap types */
    public enum SitemapType {
        INDEX, XML, ATOM, RSS, TEXT
    };

    /** W3C date the Sitemap was last modified */
    private Date lastModified;
    
    /** This Sitemap's type */
    private SitemapType type;

    /** Indicates if we have tried to process this Sitemap or not */
    private boolean processed;
        
    URL url;
    
    public AbstractSiteMap() {
    	lastModified = null;
    }
    
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
    
    
    public boolean isIndex() {
    	if(type == SitemapType.INDEX) {
    		return true;
    	}
    	return false;    	
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
}
