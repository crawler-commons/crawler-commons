package crawlercommons.sitemaps.extension;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ImageAttributesTest {

    @Test
    public void testImageAttributesAsMap() throws MalformedURLException {
        ImageAttributes attributes = new ImageAttributes(new URL("http://example.com/image.jpg"));
        attributes.setCaption("caption");
        attributes.setGeoLocation("kalamazoo");
        attributes.setTitle("Title");
        attributes.setLicense(new URL("http://example.com/license"));
        Map<String, String[]> map = attributes.asMap();

        assertEquals(attributes.getLoc().toString(), map.get("loc")[0]);
        assertEquals(attributes.getCaption(), map.get("caption")[0]);
        assertEquals(attributes.getGeoLocation(), map.get("geo_location")[0]);
        assertEquals(attributes.getTitle(), map.get("title")[0]);
        assertEquals(attributes.getLicense().toString(), map.get("license")[0]);
    }

    @Test
    public void testNullImageAttributesAsMap() {
        ImageAttributes attributes = new ImageAttributes(null);
        Map<String, String[]> map = attributes.asMap();

        assertNull(map.get("loc"));
        assertNull(map.get("caption"));
        assertNull(map.get("geo_location"));
        assertNull(map.get("title"));
        assertNull(map.get("license"));
    }
}
