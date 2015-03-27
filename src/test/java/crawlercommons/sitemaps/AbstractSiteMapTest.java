package crawlercommons.sitemaps;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.junit.Test;

public class AbstractSiteMapTest {

    @Test
    public void testDateParsing() {
        assertNull(AbstractSiteMap.convertToDate("blah"));
        assertNull(AbstractSiteMap.convertToDate(null));
        
        SimpleDateFormat isoFormatNoTimezone = new SimpleDateFormat("yyyyMMdd");

        // For formats where there's no time zone information, the time zone is undefined, so we can
        // only check on the year/month/day portion of the result.
        assertEquals("20140101", isoFormatNoTimezone.format(AbstractSiteMap.convertToDate("2014")));
        assertEquals("20140601", isoFormatNoTimezone.format(AbstractSiteMap.convertToDate("2014-06")));
        assertEquals("20140603", isoFormatNoTimezone.format(AbstractSiteMap.convertToDate("2014-06-03")));
        
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        // Complete date plus hours and minutes
        // yyyy-MM-dd'T'HH:mm+hh:00
        assertEquals("20140603T103000", isoFormat.format(AbstractSiteMap.convertToDate("2014-06-03T10:30+00:00")));

        // Complete date plus hours, minutes and seconds
        assertEquals("20140603T103045", isoFormat.format(AbstractSiteMap.convertToDate("2014-06-03T10:30:45+00:00")));
        
        // Negative time zone
        assertEquals("20140603T153045", isoFormat.format(AbstractSiteMap.convertToDate("2014-06-03T10:30:45-05:00")));
        
        // Complete date plus hours, minutes, seconds and a decimal fraction of a second
        SimpleDateFormat isoFormatWithFractionSeconds = new SimpleDateFormat("yyyyMMdd'T'HHmmss.S");
        isoFormatWithFractionSeconds.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("20140603T103045.820", isoFormatWithFractionSeconds.format(AbstractSiteMap.convertToDate("2014-06-03T10:30:45.82+00:00")));

    }

}
