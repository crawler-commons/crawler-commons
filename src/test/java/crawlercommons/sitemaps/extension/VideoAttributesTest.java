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
    public void testVideoAttributesAsMap() throws MalformedURLException {
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

        assertEquals(attributes.getThumbnailLoc().toString(), map.get(VideoAttributes.THUMBNAIL_LOC)[0]);
        assertEquals(attributes.getTitle(), map.get(VideoAttributes.TITLE)[0]);
        assertEquals(attributes.getDescription(), map.get(VideoAttributes.DESCRIPTION)[0]);
        assertEquals(attributes.getContentLoc().toString(), map.get(VideoAttributes.CONTENT_LOC)[0]);
        assertEquals(attributes.getPlayerLoc().toString(), map.get(VideoAttributes.PLAYER_LOC)[0]);
        assertEquals(attributes.getExpirationDateTime().toString(), map.get(VideoAttributes.EXPIRATION_DATE)[0]);
        assertEquals(attributes.getRating().toString(), map.get(VideoAttributes.RATING)[0]);
        assertEquals(attributes.getViewCount().toString(), map.get(VideoAttributes.VIEW_COUNT)[0]);
        assertEquals(attributes.getPublicationDateTime().toString(), map.get(VideoAttributes.PUBLICATION_DATE)[0]);
        assertEquals(attributes.getFamilyFriendly().toString(), map.get(VideoAttributes.FAMILY_FRIENDLY)[0]);
        assertArrayEquals(attributes.getTags(), map.get(VideoAttributes.TAGS));
        assertEquals(attributes.getCategory(), map.get(VideoAttributes.CATEGORY)[0]);
        assertArrayEquals(attributes.getRestrictedCountries(), map.get(VideoAttributes.RESTRICTED_COUNTRIES));
        assertArrayEquals(attributes.getAllowedCountries(), map.get(VideoAttributes.ALLOWED_COUNTRIES));
        assertEquals(attributes.getGalleryLoc().toString(), map.get(VideoAttributes.GALLERY_LOC)[0]);
        assertEquals(attributes.getGalleryTitle(), map.get(VideoAttributes.GALLERY_TITLE)[0]);
        assertArrayEquals(Arrays.stream(attributes.getPrices())
                .map(VideoAttributes.VideoPrice::toString)
                .toArray(String[]::new), map.get(VideoAttributes.PRICES));
        assertEquals(attributes.getRequiresSubscription().toString(), map.get(VideoAttributes.REQUIRES_SUBSCRIPTION)[0]);
        assertEquals(attributes.getUploader(), map.get(VideoAttributes.UPLOADER)[0]);
        assertEquals(attributes.getUploaderInfo().toString(), map.get(VideoAttributes.UPLOADER_INFO)[0]);
        assertArrayEquals(attributes.getAllowedPlatforms(), map.get(VideoAttributes.ALLOWED_PLATFORMS));
        assertArrayEquals(attributes.getRestrictedPlatforms(), map.get(VideoAttributes.RESTRICTED_PLATFORMS));
        assertEquals(attributes.getLive().toString(), map.get(VideoAttributes.IS_LIVE)[0]);
    }

    @Test
    public void testNullVideoAttributesAsMap() {
        VideoAttributes attributes = new VideoAttributes(null, null, null, null, null);
        Map<String, String[]> map = attributes.asMap();

        assertNull(map.get(VideoAttributes.THUMBNAIL_LOC));
        assertNull(map.get(VideoAttributes.TITLE));
        assertNull(map.get(VideoAttributes.DESCRIPTION));
        assertNull(map.get(VideoAttributes.CONTENT_LOC));
        assertNull(map.get(VideoAttributes.PLAYER_LOC));
        assertNull(map.get(VideoAttributes.EXPIRATION_DATE));
        assertNull(map.get(VideoAttributes.RATING));
        assertNull(map.get(VideoAttributes.VIEW_COUNT));
        assertNull(map.get(VideoAttributes.PUBLICATION_DATE));
        assertNull(map.get(VideoAttributes.FAMILY_FRIENDLY));
        assertNull(map.get(VideoAttributes.TAGS));
        assertNull(map.get(VideoAttributes.CATEGORY));
        assertNull(map.get(VideoAttributes.RESTRICTED_COUNTRIES));
        assertNull(map.get(VideoAttributes.ALLOWED_COUNTRIES));
        assertNull(map.get(VideoAttributes.GALLERY_LOC));
        assertNull(map.get(VideoAttributes.GALLERY_TITLE));
        assertNull(map.get(VideoAttributes.PRICES));
        assertNull(map.get(VideoAttributes.REQUIRES_SUBSCRIPTION));
        assertNull(map.get(VideoAttributes.UPLOADER));
        assertNull(map.get(VideoAttributes.UPLOADER_INFO));
        assertNull(map.get(VideoAttributes.ALLOWED_PLATFORMS));
        assertNull(map.get(VideoAttributes.RESTRICTED_PLATFORMS));
        assertNull(map.get(VideoAttributes.IS_LIVE));
    }
}