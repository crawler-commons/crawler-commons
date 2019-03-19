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

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

public class AbstractSiteMapTest {

    @Test
    public void testDateParsing() {
        assertNull(AbstractSiteMap.convertToDate("blah"));
        assertNull(AbstractSiteMap.convertToDate(null));

        SimpleDateFormat isoFormatShortDate = new SimpleDateFormat("yyyyMMdd", Locale.ROOT);
        isoFormatShortDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // For short dates we only check on the year/month/day portion of the result.
        // Time zone UTC is assumed because short dates do not contain a time zone.
        assertEquals("20140101", isoFormatShortDate.format(AbstractSiteMap.convertToDate("2014")));
        assertEquals("20140601", isoFormatShortDate.format(AbstractSiteMap.convertToDate("2014-06")));
        assertEquals("20140603", isoFormatShortDate.format(AbstractSiteMap.convertToDate("2014-06-03")));

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.ROOT);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Complete date plus hours and minutes
        // yyyy-MM-dd'T'HH:mm+hh:00
        assertEquals("20140603T103000", isoFormat.format(AbstractSiteMap.convertToDate("2014-06-03T10:30+00:00")));

        // Complete date plus hours, minutes and seconds
        assertEquals("20140603T103045", isoFormat.format(AbstractSiteMap.convertToDate("2014-06-03T10:30:45+00:00")));

        // Negative time zone
        assertEquals("20140603T153045", isoFormat.format(AbstractSiteMap.convertToDate("2014-06-03T10:30:45-05:00")));

        // Complete date plus hours, minutes, seconds and a decimal fraction of
        // a second
        SimpleDateFormat isoFormatWithFractionSeconds = new SimpleDateFormat("yyyyMMdd'T'HHmmss.S", Locale.ROOT);
        isoFormatWithFractionSeconds.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("20140603T103045.820", isoFormatWithFractionSeconds.format(AbstractSiteMap.convertToDate("2014-06-03T10:30:45.82+00:00")));

        // Date examples given in https://www.w3.org/TR/NOTE-datetime
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), AbstractSiteMap.TIME_ZONE_UTC);
        // YYYY (eg 1997) -- no time zone, see comment above
        zdt = zdt.withYear(1997);
        parseCompareDate(zdt, "1997", "yyyyMMdd");
        // YYYY-MM (eg 1997-07) -- no time zone, see comment above
        zdt = zdt.withMonth(7);
        parseCompareDate(zdt, "1997-07", "yyyyMMdd");
        // YYYY-MM-DD (eg 1997-07-16) -- no time zone, see comment above
        zdt = zdt.withDayOfMonth(16);
        parseCompareDate(zdt, "1997-07-16", "yyyyMMdd");
        // YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00)
        // one hour less in UTC because of time zone +01:00
        zdt = zdt.withHour(19).withMinute(20).minusHours(1);
        parseCompareDate(zdt, "1997-07-16T19:20+01:00");
        // YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00)
        zdt = zdt.withSecond(30);
        parseCompareDate(zdt, "1997-07-16T19:20:30+01:00");
        // YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00)
        zdt = zdt.withNano(450000000);
        parseCompareDate(zdt, "1997-07-16T19:20:30.45+01:00");
    }

    private void parseCompareDate(ZonedDateTime expected, String date) {
        parseCompareDate(expected, date, null);
    }

    private void parseCompareDate(ZonedDateTime expected, String date, String dateFormat) {
        ZonedDateTime parsed = AbstractSiteMap.convertToZonedDateTime(date);
        if (dateFormat != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(dateFormat, Locale.ROOT).withZone(ZoneId.systemDefault());
            assertEquals("Failed to parse W3C date format:", fmt.format(expected), fmt.format(parsed));
        } else {
            assertTrue("Failed to parse W3C date format: " + expected + " <> " + parsed, expected.isEqual(parsed));
        }
    }

    @Test
    public void testRssDateNormalyzing() {
        assertNull(AbstractSiteMap.normalizeRSSTimestamp(null));
        assertEquals("incorrect", AbstractSiteMap.normalizeRSSTimestamp("incorrect"));

        assertEquals("Full date-time with named timezone", "2017-01-05T12:34:50Z", AbstractSiteMap.normalizeRSSTimestamp("Thu, 05 Jan 2017 12:34:50 GMT"));
        assertEquals("Full date-time with time zone offset", "2017-01-05T12:34:51Z", AbstractSiteMap.normalizeRSSTimestamp("Thu, 05 Jan 2017 13:34:51 +0100"));
        assertEquals("Date-time without week day", "2017-01-05T12:34:52Z", AbstractSiteMap.normalizeRSSTimestamp("05 Jan 2017 11:34:52 -0100"));
        assertEquals("Date-time without week day and two-digit year", "2017-01-05T12:34:53Z", AbstractSiteMap.normalizeRSSTimestamp("05 Jan 17 12:34:53 GMT"));
        assertEquals("Date-time with two-digit year", "2017-01-05T12:34:54Z", AbstractSiteMap.normalizeRSSTimestamp("Thu, 05 Jan 17 12:34:54 GMT"));
    }

    @Test
    public void testFullDateFormat() {
        // test example date with time zone offset
        // from https://www.w3.org/TR/NOTE-datetime
        // the (re)formatted date should be identical
        ZonedDateTime date1 = SiteMap.convertToZonedDateTime("1994-11-05T13:15:30Z");
        ZonedDateTime date2 = SiteMap.convertToZonedDateTime("1994-11-05T08:15:30-05:00");
        assertTrue("Failed to parse date with time zone", date1.isEqual(date2));
        String datestr1 = SiteMap.W3C_FULLDATE_FORMATTER_UTC.format(date1);
        String datestr2 = SiteMap.W3C_FULLDATE_FORMATTER_UTC.format(date2);
        assertEquals("Failed to format date", datestr1, datestr2);
    }

}
