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

        assertEquals(attributes.getName(), map.get(NewsAttributes.NAME)[0]);
        assertEquals(attributes.getTitle(), map.get(NewsAttributes.TITLE)[0]);
        assertEquals(attributes.getLanguage(), map.get(NewsAttributes.LANGUAGE)[0]);
        assertEquals(attributes.getPublicationDateTime().toString(), map.get(NewsAttributes.PUBLICATION_DATE)[0]);
        assertArrayEquals(attributes.getKeywords(), map.get(NewsAttributes.KEYWORDS));
        assertArrayEquals(attributes.getStockTickers(), map.get(NewsAttributes.STOCK_TICKERS));
        assertArrayEquals(Arrays.stream(attributes.getGenres())
                .map(NewsAttributes.NewsGenre::toString)
                .toArray(String[]::new), map.get(NewsAttributes.GENRES));
    }

    @Test
    public void testNullNewsAttributesAsMap() {
        NewsAttributes attributes = new NewsAttributes(null, null, null, null);
        Map<String, String[]> map = attributes.asMap();

        assertNull(map.get(NewsAttributes.NAME));
        assertNull(map.get(NewsAttributes.TITLE));
        assertNull(map.get(NewsAttributes.LANGUAGE));
        assertNull(map.get(NewsAttributes.PUBLICATION_DATE));
        assertNull(map.get(NewsAttributes.KEYWORDS));
        assertNull(map.get(NewsAttributes.STOCK_TICKERS));
        assertNull(map.get(NewsAttributes.GENRES));
    }

}
