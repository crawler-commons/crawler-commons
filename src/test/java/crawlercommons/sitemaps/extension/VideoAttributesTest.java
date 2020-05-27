package crawlercommons.sitemaps.extension;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class VideoAttributesTest {

    @Test
    public void testVideoAttributes() throws MalformedURLException {
        VideoAttributes attributes = new VideoAttributes(new URL("http://www.example.com/thumbs/123.jpg"), "Grilling steaks for summer",
                "Alkis shows you how to get perfectly done steaks every time", new URL("http://www.example.com/video123.flv"), new URL("http://www.example.com/videoplayer.swf?video=123"));
        attributes.setDuration(600);
        ZonedDateTime dt = ZonedDateTime.parse("2009-11-05T19:20:30+08:00");
        attributes.setExpirationDate(dt);
        dt = ZonedDateTime.parse("2007-11-05T19:20:30+08:00");
        attributes.setPublicationDate(dt);
        attributes.setCategory("music");
        attributes.setRating(4.2f);
        attributes.setViewCount(12345);
        attributes.setFamilyFriendly(true);
        attributes.setTags(new String[] { "sample_tag1", "sample_tag2" });
        attributes.setAllowedCountries(new String[] { "IE", "GB", "US", "CA" });
        attributes.setGalleryLoc(new URL("http://cooking.example.com"));
        attributes.setGalleryTitle("Cooking Videos");
        attributes.setPrices(new VideoAttributes.VideoPrice[] { new VideoAttributes.VideoPrice("EUR", 1.99f, VideoAttributes.VideoPriceType.own) });
        attributes.setRequiresSubscription(true);
        attributes.setUploader("GrillyMcGrillerson");
        attributes.setUploaderInfo(new URL("http://www.example.com/users/grillymcgrillerson"));
        attributes.setLive(false);
        Map<String, String[]> map = attributes.asMap();

        assertEquals(attributes.getThumbnailLoc().toString(), map.get("thumbnail_loc")[0]);
        assertEquals(attributes.getTitle(), map.get("title")[0]);
        assertEquals(attributes.getDescription(), map.get("description")[0]);
        assertEquals(attributes.getContentLoc().toString(), map.get("content_loc")[0]);
        assertEquals(attributes.getPlayerLoc().toString(), map.get("player_loc")[0]);
        assertEquals(attributes.getExpirationDateTime().toString(), map.get("expiration_date")[0]);
        assertEquals(attributes.getRating().toString(), map.get("rating")[0]);
        assertEquals(attributes.getViewCount().toString(), map.get("view_count")[0]);
        assertEquals(attributes.getPublicationDateTime().toString(), map.get("publication_date")[0]);
        assertEquals(attributes.getFamilyFriendly().toString(), map.get("family_friendly")[0]);
        assertArrayEquals(attributes.getTags(), map.get("tags"));
        assertEquals(attributes.getCategory(), map.get("category")[0]);
        assertArrayEquals(attributes.getRestrictedCountries(), map.get("restricted_countries"));
        assertArrayEquals(attributes.getAllowedCountries(), map.get("allowed_countries"));
        assertEquals(attributes.getGalleryLoc().toString(), map.get("gallery_loc")[0]);
        assertEquals(attributes.getGalleryTitle(), map.get("gallery_title")[0]);
        assertArrayEquals(Arrays.stream(attributes.getPrices())
                .map(VideoAttributes.VideoPrice::toString)
                .toArray(String[]::new), map.get("prices"));
        assertEquals(attributes.getRequiresSubscription().toString(), map.get("requires_subscription")[0]);
        assertEquals(attributes.getUploader(), map.get("uploader")[0]);
        assertEquals(attributes.getUploaderInfo().toString(), map.get("uploader_info")[0]);
        assertArrayEquals(attributes.getAllowedPlatforms(), map.get("allowed_platforms"));
        assertArrayEquals(attributes.getRestrictedPlatforms(), map.get("restricted_platforms"));
        assertEquals(attributes.getLive().toString(), map.get("is_live")[0]);
    }

    @Test
    public void testNullVideoAttributes() {
        VideoAttributes attributes = new VideoAttributes(null, null, null, null, null);
        Map<String, String[]> map = attributes.asMap();

        assertNull(map.get("thumbnail_loc"));
        assertNull(map.get("title"));
        assertNull(map.get("description"));
        assertNull(map.get("content_loc"));
        assertNull(map.get("player_loc"));
        assertNull(map.get("expiration_date"));
        assertNull(map.get("rating"));
        assertNull(map.get("view_count"));
        assertNull(map.get("publication_date"));
        assertNull(map.get("family_friendly"));
        assertNull(map.get("tags"));
        assertNull(map.get("category"));
        assertNull(map.get("restricted_countries"));
        assertNull(map.get("allowed_countries"));
        assertNull(map.get("gallery_loc"));
        assertNull(map.get("gallery_title"));
        assertNull(map.get("prices"));
        assertNull(map.get("requires_subscription"));
        assertNull(map.get("uploader"));
        assertNull(map.get("uploader_info"));
        assertNull(map.get("allowed_platforms"));
        assertNull(map.get("restricted_platforms"));
        assertNull(map.get("is_live"));
    }

}
