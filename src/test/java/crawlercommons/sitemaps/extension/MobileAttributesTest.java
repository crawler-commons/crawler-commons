package crawlercommons.sitemaps.extension;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MobileAttributesTest {

    @Test
    public void testMobileAttributesAsMap() throws MalformedURLException {
        MobileAttributes attributes = new MobileAttributes();
        Map<String, String[]> map = attributes.asMap();

        assertEquals(0, map.size());
    }
}
