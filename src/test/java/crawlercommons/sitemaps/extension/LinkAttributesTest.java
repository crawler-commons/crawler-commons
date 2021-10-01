package crawlercommons.sitemaps.extension;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LinkAttributesTest {

    @Test
    public void testLinkAttributesAsMap() throws MalformedURLException {
        LinkAttributes attributes = new LinkAttributes(new URL("http://www.example.com/deutsch/"));
        attributes.setParams(new HashMap<String, String>() {
            {
                put("rel", "alternate");
                put("hreflang", "de");
            }
        });
        Map<String, String[]> map = attributes.asMap();

        assertEquals(attributes.getHref().toString(), map.get(LinkAttributes.HREF)[0]);
        assertEquals(attributes.getParams().get("rel"), map.get("params.rel")[0]);
        assertEquals(attributes.getParams().get("hreflang"), map.get("params.hreflang")[0]);
    }

    @Test
    public void testNullLinkAttributesAsMap() {
        LinkAttributes attributes = new LinkAttributes(null);
        Map<String, String[]> map = attributes.asMap();

        assertNull(map.get(LinkAttributes.HREF));
    }
}
