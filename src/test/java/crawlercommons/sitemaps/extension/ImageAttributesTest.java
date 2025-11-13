package crawlercommons.sitemaps.extension;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ImageAttributesTest {

    @Test
    public void testImageAttributesAsMap() throws MalformedURLException, URISyntaxException {
        ImageAttributes attributes = new ImageAttributes(new URI("http://example.com/image.jpg").toURL());
        attributes.setCaption("caption");
        attributes.setGeoLocation("kalamazoo");
        attributes.setTitle("Title");
        attributes.setLicense(new URI("http://example.com/license").toURL());
        Map<String, String[]> map = attributes.asMap();

        assertEquals(attributes.getLoc().toString(), map.get(ImageAttributes.LOC)[0]);
        assertEquals(attributes.getCaption(), map.get(ImageAttributes.CAPTION)[0]);
        assertEquals(attributes.getGeoLocation(), map.get(ImageAttributes.GEO_LOCATION)[0]);
        assertEquals(attributes.getTitle(), map.get(ImageAttributes.TITLE)[0]);
        assertEquals(attributes.getLicense().toString(), map.get(ImageAttributes.LICENSE)[0]);
    }

    @Test
    public void testNullImageAttributesAsMap() {
        ImageAttributes attributes = new ImageAttributes(null);
        Map<String, String[]> map = attributes.asMap();

        assertNull(map.get(ImageAttributes.LOC));
        assertNull(map.get(ImageAttributes.CAPTION));
        assertNull(map.get(ImageAttributes.GEO_LOCATION));
        assertNull(map.get(ImageAttributes.TITLE));
        assertNull(map.get(ImageAttributes.LICENSE));
    }
}
