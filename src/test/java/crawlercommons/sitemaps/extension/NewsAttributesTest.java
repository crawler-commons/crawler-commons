package crawlercommons.sitemaps.extension;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NewsAttributesTest {

    @Test
    public void testNewsAttributesAsMap() {
        ZonedDateTime dt = ZonedDateTime.parse("2008-11-23T00:00:00+00:00");
        NewsAttributes attributes = new NewsAttributes("The Example Times", "en", dt, "Companies A, B in Merger Talks");
        attributes.setKeywords(new String[] { "business", "merger", "acquisition", "A", "B" });
        attributes.setGenres(new NewsAttributes.NewsGenre[] { NewsAttributes.NewsGenre.PressRelease, NewsAttributes.NewsGenre.Blog });
        attributes.setStockTickers(new String[] { "NASDAQ:A", "NASDAQ:B" });
        Map<String, String[]> map = attributes.asMap();

        assertEquals(attributes.getName(), map.get("name")[0]);
        assertEquals(attributes.getTitle(), map.get("title")[0]);
        assertEquals(attributes.getLanguage(), map.get("language")[0]);
        assertEquals(attributes.getPublicationDateTime().toString(), map.get("publication_date")[0]);
        assertArrayEquals(attributes.getKeywords(), map.get("keywords"));
        assertArrayEquals(attributes.getStockTickers(), map.get("stock_tickers"));
        assertArrayEquals(Arrays.stream(attributes.getGenres())
                .map(NewsAttributes.NewsGenre::toString)
                .toArray(String[]::new), map.get("genres"));
    }

    @Test
    public void testNullNewsAttributesAsMap() {
        NewsAttributes attributes = new NewsAttributes(null, null, null, null);
        Map<String, String[]> map = attributes.asMap();

        assertNull(map.get("name"));
        assertNull(map.get("title"));
        assertNull(map.get("language"));
        assertNull(map.get("publication_date"));
        assertNull(map.get("keywords"));
        assertNull(map.get("stock_tickers"));
        assertNull(map.get("genres"));
    }

}
