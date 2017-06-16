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
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

public class AbstractSiteMapTest {

    @Test
    public void testDateParsing() {
        assertNull(AbstractSiteMap.convertToDate("blah"));
        assertNull(AbstractSiteMap.convertToDate(null));

        SimpleDateFormat isoFormatNoTimezone = new SimpleDateFormat("yyyyMMdd", Locale.ROOT);

        // For formats where there's no time zone information, the time zone is
        // undefined, so we can
        // only check on the year/month/day portion of the result.
        assertEquals("20140101", isoFormatNoTimezone.format(AbstractSiteMap.convertToDate("2014")));
        assertEquals("20140601", isoFormatNoTimezone.format(AbstractSiteMap.convertToDate("2014-06")));
        assertEquals("20140603", isoFormatNoTimezone.format(AbstractSiteMap.convertToDate("2014-06-03")));

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

    }

    @Test
    public void testRssDateNormalyzing() {
        assertNull(AbstractSiteMap.normalizeRSSTimestamp(null));
        assertEquals("incorrect", AbstractSiteMap.normalizeRSSTimestamp("incorrect"));

        assertEquals("2017-01-05T12:34:50+0000", AbstractSiteMap.normalizeRSSTimestamp("Thu, 05 Jan 2017 12:34:50 GMT"));
        assertEquals("2017-01-05T12:34:51+0000", AbstractSiteMap.normalizeRSSTimestamp("Thu, 05 Jan 2017 13:34:51 +0100"));
        assertEquals("2017-01-05T12:34:52+0000", AbstractSiteMap.normalizeRSSTimestamp("05 Jan 2017 11:34:52 -0100"));
        assertEquals("2017-01-05T12:34:53+0000", AbstractSiteMap.normalizeRSSTimestamp("05 Jan 17 12:34:53 GMT"));
        assertEquals("2017-01-05T12:34:54+0000", AbstractSiteMap.normalizeRSSTimestamp("Thu, 05 Jan 17 12:34:54 GMT"));
    }

}
