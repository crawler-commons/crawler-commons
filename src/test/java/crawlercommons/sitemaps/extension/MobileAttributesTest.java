package crawlercommons.sitemaps.extension;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MobileAttributesTest {

    @Test
    public void testMobileAttributesAsMap() {
        MobileAttributes attributes = new MobileAttributes();
        Map<String, String[]> map = attributes.asMap();

        assertEquals(0, map.size());
    }
}
