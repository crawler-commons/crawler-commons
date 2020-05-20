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
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;

/** SiteMap or SiteMapIndex **/
public abstract class AbstractSiteMap {

    /** Various Sitemap types */
    public enum SitemapType {
        INDEX, XML, ATOM, RSS, TEXT
    };

    protected static final ZoneId TIME_ZONE_UTC = ZoneId.of(ZoneOffset.UTC.toString());

    /**
     * DateTimeFormatter for parsing dates in ISO-8601 format
     */
    public static final DateTimeFormatter W3C_FULLDATE_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * DateTimeFormatter to format dates in ISO-8601 format (UTC time zone 'Z')
     */
    public static final DateTimeFormatter W3C_FULLDATE_FORMATTER_UTC = DateTimeFormatter.ISO_INSTANT;

    /**
     * DateTimeFormatter for parsing short dates ('1997', '1997-07',
     * '1997-07-16') without daytime and time zone
     */
    public static final DateTimeFormatter W3C_SHORTDATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy[-MM[-dd]]", Locale.ROOT).withZone(TIME_ZONE_UTC);

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
    public void setType(SitemapType type) {
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
     * @return true if the Sitemap has been processed i.e it contains at least
     *         one SiteMapURL
     */
    public boolean isProcessed() {
        return processed;
    }

    /**
     * @param lastModified
     *            the last-modified date
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @param lastModified
     *            the last-modified date and time
     */
    public void setLastModified(ZonedDateTime lastModified) {
        this.lastModified = Date.from(lastModified.toInstant());
    }

    /**
     * @param lastModified
     *            the last-modified date time. If parsing of the given date time
     *            fails, the last-modified field is set to null.
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
     * Convert the given date (given in an acceptable DateFormat), return null
     * if the date is not in the correct format.
     * 
     * <p>
     * Dates must follow the <a href="https://www.w3.org/TR/NOTE-datetime">W3C
     * Datetime format</a> which is similar to <a
     * href="https://en.wikipedia.org/wiki/ISO_8601">ISO-8601</a> but allows
     * dates with different precisions:
     * </p>
     * 
     * <pre>
     *   Year:
     *      YYYY (eg 1997)
     *   Year and month:
     *      YYYY-MM (eg 1997-07)
     *   Complete date:
     *      YYYY-MM-DD (eg 1997-07-16)
     *   Complete date plus hours and minutes:
     *      YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00)
     *   Complete date plus hours, minutes and seconds:
     *      YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00)
     *   Complete date plus hours, minutes, seconds and a decimal fraction of a second
     *      YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00)
     * </pre>
     * 
     * Note: Greenwich time (UTC) is assumed if the date string does not specify
     * a time zone.
     * 
     * @param date
     *            - the date to be parsed
     * @return the zoned date time equivalent to the date string or NULL if
     *         parsing failed
     */
    public static ZonedDateTime convertToZonedDateTime(String date) {

        if (date == null) {
            return null;
        }

        // full date including daytime and optional time zone
        try {
            return W3C_FULLDATE_FORMATTER.parse(date, ZonedDateTime::from);
        } catch (DateTimeParseException e) {
            // fall-through and try date without daytime
        }

        // dates without daytime
        try {
            TemporalAccessor ta = W3C_SHORTDATE_FORMATTER.parse(date);
            LocalDate ldt = null;
            if (ta.isSupported(ChronoField.DAY_OF_MONTH)) {
                ldt = LocalDate.from(ta);
            } else if (ta.isSupported(ChronoField.MONTH_OF_YEAR)) {
                ldt =  YearMonth.from(ta).atDay(1);
            } else if (ta.isSupported(ChronoField.YEAR)) {
                ldt = Year.from(ta).atDay(1);
            }
            if (ldt != null) {
                return ldt.atStartOfDay(TIME_ZONE_UTC);
            }
        } catch (DateTimeParseException e) {
        }

        return null;
    }

    /**
     * See {@link #convertToZonedDateTime(String)}.
     * 
     * @param date
     *            the date string to convert
     * @return returns the date or null if parsing of the date string fails
     */
    public static Date convertToDate(String date) {
        ZonedDateTime zdt = convertToZonedDateTime(date);
        if (zdt == null) {
            return null;
        }
        return Date.from(zdt.toInstant());
    }

    /**
     * Converts pubDate of RSS to the ISO-8601 instant format, e.g.,
     * '2017-01-05T12:34:54Z' in UTC / GMT time zone, see
     * {@link DateTimeFormatter#ISO_INSTANT}.
     * 
     * @param pubDate
     *            - date time of pubDate in RFC822
     * @return converted to &quot;yyyy-MM-dd'T'HH:mm:ssZ&quot; format or
     *         original value if it doesn't follow the RFC822
     */
    public static String normalizeRSSTimestamp(String pubDate) {
        if (pubDate == null) {
            return null;
        }
        ZonedDateTime zdt = parseRSSTimestamp(pubDate);
        if (zdt == null) {
            return pubDate;
        }
        return W3C_FULLDATE_FORMATTER_UTC.format(zdt);
    }

    /**
     * Parse pubDate of RSS feeds.
     * 
     * @param pubDate
     *            - date time of pubDate in RFC822
     * @return date time or null if parsing failed
     */
    public static ZonedDateTime parseRSSTimestamp(String pubDate) {
        ZonedDateTime zdt = null;
        try {
            zdt = DateTimeFormatter.RFC_1123_DATE_TIME.parse(pubDate, ZonedDateTime::from);
        } catch (DateTimeParseException ex) {
            return null;
        }
        if (zdt.getYear() <= 99 && zdt.getYear() >= 0) {
            // adjust two-digit years: RFC 1123 requires a fully-specified year,
            // while RFC 822 allows two digits
            if (zdt.getYear() >= 80) {
                // assume 19yy - RFC 822 has been publish in 1982
                zdt = zdt.plusYears(1900);
            } else {
                zdt = zdt.plusYears(2000);
            }
        }
        return zdt;
    }
}