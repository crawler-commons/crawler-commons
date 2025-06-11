/**
 * Copyright 2018 Crawler-Commons
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package crawlercommons.sitemaps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import crawlercommons.sitemaps.extension.Extension;
import crawlercommons.sitemaps.extension.ExtensionMetadata;
import crawlercommons.sitemaps.extension.ImageAttributes;
import crawlercommons.sitemaps.extension.LinkAttributes;
import crawlercommons.sitemaps.extension.MobileAttributes;
import crawlercommons.sitemaps.extension.NewsAttributes;
import crawlercommons.sitemaps.extension.PageMap;
import crawlercommons.sitemaps.extension.PageMapDataObject;
import crawlercommons.sitemaps.extension.VideoAttributes;

public class SiteMapParserExtensionTest {

    private AbstractSiteMap parse(SiteMapParser parser, String resourcePath, URL url) throws IOException, UnknownFormatException {
        byte[] content = SiteMapParserTest.getResourceAsBytes(resourcePath);
        AbstractSiteMap asm = parser.parseSiteMap("text/xml", content, url);
        AbstractSiteMapTest.testSerializable(asm);
        return asm;
    }

    @Test
    public void testVideosSitemap() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.enableExtension(Extension.VIDEO);

        URL url = new URL("http://www.example.com/sitemap-video.xml");
        AbstractSiteMap asm = parse(parser, "src/test/resources/sitemaps/extension/sitemap-videos.xml", url);

        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(3, sm.getSiteMapUrls().size());
        Iterator<SiteMapURL> siter = sm.getSiteMapUrls().iterator();

        // first <loc> element: nearly all video attributes
        VideoAttributes expectedVideoAttributes = new VideoAttributes(new URL("http://www.example.com/thumbs/123.jpg"), "Grilling steaks for summer",
                        "Alkis shows you how to get perfectly done steaks every time", new URL("http://www.example.com/video123.flv"), new URL("http://www.example.com/videoplayer.swf?video=123"));
        expectedVideoAttributes.setDuration(600);
        ZonedDateTime dt = ZonedDateTime.parse("2009-11-05T19:20:30+08:00");
        expectedVideoAttributes.setExpirationDate(dt);
        dt = ZonedDateTime.parse("2007-11-05T19:20:30+08:00");
        expectedVideoAttributes.setPublicationDate(dt);
        expectedVideoAttributes.setRating(4.2f);
        expectedVideoAttributes.setViewCount(12345);
        expectedVideoAttributes.setFamilyFriendly(true);
        expectedVideoAttributes.setTags(new String[] { "sample_tag1", "sample_tag2" });
        expectedVideoAttributes.setAllowedCountries(new String[] { "IE", "GB", "US", "CA" });
        expectedVideoAttributes.setGalleryLoc(new URL("http://cooking.example.com"));
        expectedVideoAttributes.setGalleryTitle("Cooking Videos");
        expectedVideoAttributes.setPrices(new VideoAttributes.VideoPrice[] { new VideoAttributes.VideoPrice("EUR", 1.99f, VideoAttributes.VideoPriceType.own) });
        expectedVideoAttributes.setRequiresSubscription(true);
        expectedVideoAttributes.setUploader("GrillyMcGrillerson");
        expectedVideoAttributes.setUploaderInfo(new URL("http://www.example.com/users/grillymcgrillerson"));
        expectedVideoAttributes.setLive(false);
        assertTrue(expectedVideoAttributes.isValid());
        VideoAttributes attr = (VideoAttributes) siter.next().getAttributesForExtension(Extension.VIDEO)[0];
        assertNotNull(attr);
        assertTrue(attr.isValid());
        assertEquals(expectedVideoAttributes, attr);

        // locale-specific number format in <video:price>, test #220
        // The current expected behavior is to not handle non-US locale price
        // values and set the price value to null if parsing as float value
        // fails.
        expectedVideoAttributes = new VideoAttributes(new URL("http://www.example.com/thumbs/123-2.jpg"), "Grilling steaks for summer, episode 2",
                        "Alkis shows you how to get perfectly done steaks every time", new URL("http://www.example.com/video123-2.flv"), null);
        expectedVideoAttributes.setPrices(new VideoAttributes.VideoPrice[] { new VideoAttributes.VideoPrice("EUR", null, VideoAttributes.VideoPriceType.own) });
        attr = (VideoAttributes) siter.next().getAttributesForExtension(Extension.VIDEO)[0];
        assertNotNull(attr);
        assertEquals(expectedVideoAttributes, attr);

        // empty price, only type (purchase or rent) is indicated, see #221
        expectedVideoAttributes = new VideoAttributes(new URL("http://www.example.com/thumbs/123-3.jpg"), "Grilling steaks for summer, episode 3",
                        "Alkis shows you how to get perfectly done steaks every time", new URL("http://www.example.com/video123-3.flv"), null);
        expectedVideoAttributes.setPrices(new VideoAttributes.VideoPrice[] { new VideoAttributes.VideoPrice(null, null, VideoAttributes.VideoPriceType.rent) });
        attr = (VideoAttributes) siter.next().getAttributesForExtension(Extension.VIDEO)[0];
        assertNotNull(attr);
        assertEquals(expectedVideoAttributes, attr);
    }

    @Test
    public void testVideosSitemapTVShow() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.enableExtension(Extension.VIDEO);

        URL url = new URL("https://example.org/sitemap.xml");
        AbstractSiteMap asm = parse(parser, "src/test/resources/sitemaps/extension/sitemap-videos-tvshow.xml", url);

        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(1, sm.getSiteMapUrls().size());
        Iterator<SiteMapURL> siter = sm.getSiteMapUrls().iterator();
        ExtensionMetadata[] attrs = siter.next().getAttributesForExtension(Extension.VIDEO);
        assertEquals(1, attrs.length);
        VideoAttributes attr = (VideoAttributes) attrs[0];
        assertNotNull(attr);

        VideoAttributes expectedVideoAttributes = new VideoAttributes( //
                        new URL("https://example.org/my-tv-show.jpg"), //
                        "My TV Show! - Thu, Jun 05, 2025", //
                        "Dummy description", null, null);
        expectedVideoAttributes.setDuration(2459);
        ZonedDateTime dt = ZonedDateTime.parse("2025-06-13T09:00:00+00:00");
        expectedVideoAttributes.setExpirationDate(dt);
        dt = ZonedDateTime.parse("2025-06-06T09:00:00+00:00");
        expectedVideoAttributes.setPublicationDate(dt);
        expectedVideoAttributes.setTags(new String[] { "talkshow", "interview", "example" });
        expectedVideoAttributes.setCategory("My TV Show!");
        expectedVideoAttributes.setRequiresSubscription(true);
        expectedVideoAttributes.setContentLoc(new URL("https://example.org/video/video/23-13.mp4"));
        expectedVideoAttributes.setPlayerLoc(new URL("https://example.org/video/embed/23-13"));
        expectedVideoAttributes.addContentSegment(1287, new URL("https://example.org/video/video/23-13-1.mp4"));
        expectedVideoAttributes.addContentSegment(1172, new URL("https://example.org/video/video/23-13-2.mp4"));
        VideoAttributes.TVShow tvShow = new VideoAttributes.TVShow();
        tvShow.setShowTitle("My TV Show!");
        tvShow.setVideoType("full");
        tvShow.setEpisodeTitle("Thu, Jun 05, 2025");
        tvShow.setSeasonNumber(23);
        tvShow.setEpisodeNumber(13);
        dt = ZonedDateTime.parse("2025-06-05T21:00Z");
        tvShow.setPremierDate(dt);
        expectedVideoAttributes.setTVShow(tvShow);

        assertTrue(expectedVideoAttributes.isValid());
        assertTrue(attr.isValid());
        assertEquals(expectedVideoAttributes, attr);
        assertEquals(expectedVideoAttributes.toString(), attr.toString());
    }

    @Test
    public void testImageSitemap() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.enableExtension(Extension.IMAGE);

        URL url = new URL("http://www.example.com/sitemap-images.xml");
        AbstractSiteMap asm = parse(parser, "src/test/resources/sitemaps/extension/sitemap-images.xml", url);

        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(1, sm.getSiteMapUrls().size());
        ImageAttributes imageAttributes1 = new ImageAttributes(new URL("http://example.com/image.jpg"));
        ImageAttributes imageAttributes2 = new ImageAttributes(new URL("http://example.com/photo.jpg"));
        imageAttributes2.setCaption("This is the caption.");
        imageAttributes2.setGeoLocation("Limerick, Ireland");
        imageAttributes2.setTitle("Example photo shot in Limerick, Ireland");
        imageAttributes2.setLicense(new URL("https://creativecommons.org/licenses/by/4.0/legalcode"));

        for (SiteMapURL su : sm.getSiteMapUrls()) {
            assertNotNull(su.getAttributesForExtension(Extension.IMAGE));
            ExtensionMetadata[] attrs = su.getAttributesForExtension(Extension.IMAGE);
            ImageAttributes attr = (ImageAttributes) attrs[0];
            assertTrue(attr.isValid());
            assertEquals(imageAttributes1, attr);
            attr = (ImageAttributes) attrs[1];
            assertTrue(attr.isValid());
            assertEquals(imageAttributes2, attr);
        }
    }

    @SuppressWarnings("serial")
    @Test
    public void testXHTMLLinksSitemap() throws UnknownFormatException, IOException, MalformedURLException {
        SiteMapParser parser = new SiteMapParser();
        parser.enableExtension(Extension.LINKS);

        URL url = new URL("http://www.example.com/sitemap-links.xml");
        AbstractSiteMap asm = parse(parser, "src/test/resources/sitemaps/extension/sitemap-links.xml", url);

        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(3, sm.getSiteMapUrls().size());
        // all three pages share the same links attributes
        LinkAttributes[] linkAttributes = new LinkAttributes[] { new LinkAttributes(new URL("http://www.example.com/deutsch/")),
                        new LinkAttributes(new URL("http://www.example.com/schweiz-deutsch/")), new LinkAttributes(new URL("http://www.example.com/english/")) };
        linkAttributes[0].setParams(new HashMap<String, String>() {
            {
                put("rel", "alternate");
                put("hreflang", "de");
            }
        });
        linkAttributes[1].setParams(new HashMap<String, String>() {
            {
                put("rel", "alternate");
                put("hreflang", "de-ch");
            }
        });
        linkAttributes[2].setParams(new HashMap<String, String>() {
            {
                put("rel", "alternate");
                put("hreflang", "en");
            }
        });

        for (SiteMapURL su : sm.getSiteMapUrls()) {
            assertNotNull(su.getAttributesForExtension(Extension.LINKS));
            ExtensionMetadata[] attrs = su.getAttributesForExtension(Extension.LINKS);
            assertEquals(linkAttributes.length, attrs.length);
            for (int i = 0; i < linkAttributes.length; i++) {
                LinkAttributes attr = (LinkAttributes) attrs[i];
                assertEquals(linkAttributes[i], attr);
                assertTrue(linkAttributes[i].isValid());
            }
        }
    }

    @Test
    public void testNewsSitemap() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.enableExtension(Extension.NEWS);

        URL url = new URL("http://www.example.org/sitemap-news.xml");
        AbstractSiteMap asm = parse(parser, "src/test/resources/sitemaps/extension/sitemap-news.xml", url);

        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(2, sm.getSiteMapUrls().size());
        ZonedDateTime dt = ZonedDateTime.parse("2008-12-23T00:00:00+00:00");
        NewsAttributes expectedNewsAttributes = new NewsAttributes("The Example Times", "en", dt, "Companies A, B in Merger Talks");
        expectedNewsAttributes.setKeywords(new String[] { "business", "merger", "acquisition", "A", "B" });
        expectedNewsAttributes.setGenres(new NewsAttributes.NewsGenre[] { NewsAttributes.NewsGenre.PressRelease, NewsAttributes.NewsGenre.Blog });
        expectedNewsAttributes.setStockTickers(new String[] { "NASDAQ:A", "NASDAQ:B" });
        expectedNewsAttributes.setAccess("Subscription");
        Iterator<SiteMapURL> it = sm.getSiteMapUrls().iterator();
        SiteMapURL su = it.next();
        assertNotNull(su.getAttributesForExtension(Extension.NEWS));
        assertEquals(1, su.getAttributesForExtension(Extension.NEWS).length);
        NewsAttributes attr = (NewsAttributes) su.getAttributesForExtension(Extension.NEWS)[0];
        assertEquals(expectedNewsAttributes, attr);
        assertNotNull(su.getAttributesForExtension(Extension.NEWS));
        assertEquals("Subscription", attr.getAccess().toString());
        assertEquals(expectedNewsAttributes.toString(), attr.toString());

        // second sitemap record with missing / empty attributes
        su = it.next();
        dt = ZonedDateTime.parse("2025-05-27T00:00:00+00:00");
        expectedNewsAttributes = new NewsAttributes(null, "en", dt, "Test empty news attributes");
        assertFalse(expectedNewsAttributes.isValid());
        assertNotNull(su.getAttributesForExtension(Extension.NEWS));
        assertEquals(1, su.getAttributesForExtension(Extension.NEWS).length);

        attr = (NewsAttributes) su.getAttributesForExtension(Extension.NEWS)[0];
        assertFalse(attr.isValid());
        assertEquals(expectedNewsAttributes, attr);
        assertEquals(expectedNewsAttributes.toString(), attr.toString());
    }

    @Test
    public void testMobileSitemap() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.enableExtension(Extension.MOBILE);

        URL url = new URL("http://www.example.org/sitemap-mobile.xml");
        AbstractSiteMap asm = parse(parser, "src/test/resources/sitemaps/extension/sitemap-mobile.xml", url);

        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        for (SiteMapURL su : sm.getSiteMapUrls()) {
            URL u = su.getUrl();
            ExtensionMetadata[] attrs = su.getAttributesForExtension(Extension.MOBILE);
            if (u.getPath().contains("mobile-friendly")) {
                assertNotNull(attrs);
                MobileAttributes attr = (MobileAttributes) attrs[0];
                assertNotNull(attr);
                assertTrue(attr.isValid());
            } else {
                assertTrue(attrs == null || attrs.length == 0);
            }
        }
    }

    @Test
    public void testShinpaideshuNewsSitemap() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.setStrictNamespace(true);
        parser.enableExtension(Extension.NEWS);

        URL url = new URL("https://shinpaideshou.wordpress.com/news-sitemap.xml");
        AbstractSiteMap asm = parse(parser, "src/test/resources/sitemaps/extension/shinpaideshou-news-sitemap.xml", url);

        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(3, sm.getSiteMapUrls().size());
        for (SiteMapURL su : sm.getSiteMapUrls()) {
            assertNotNull(su.getAttributesForExtension(Extension.NEWS));
            NewsAttributes attr = (NewsAttributes) su.getAttributesForExtension(Extension.NEWS)[0];
            assertNotNull(attr.getName());
            assertNotNull(attr.getPublicationDateTime());
            assertEquals(2017, attr.getPublicationDateTime().getYear());
        }
    }

    @Test
    public void testHebdenbridgetimesArticlesSitemap() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.setStrictNamespace(true);
        parser.enableExtension(Extension.NEWS);
        parser.enableExtension(Extension.IMAGE);
        parser.enableExtension(Extension.VIDEO);
        parser.enableExtension(Extension.MOBILE);

        URL url = new URL("http://www.hebdenbridgetimes.co.uk/sitemap-article-2015-18.xml");
        AbstractSiteMap asm = parse(parser, "src/test/resources/sitemaps/extension/hebdenbridgetimes-articles-sitemap.xml", url);

        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(74, sm.getSiteMapUrls().size());
    }

    @Test
    public void testPageMapSitemap() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.setStrictNamespace(true);
        parser.enableExtension(Extension.PAGEMAPS);

        String urlStr = "http://www.example.com/pagemaps-sitemap.xml";
        URL url = new URL(urlStr);
        AbstractSiteMap asm = parse(parser, "src/test/resources/sitemaps/extension/pagemaps-sitemap.xml", url);

        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(2, sm.getSiteMapUrls().size());
        assertEquals(urlStr, sm.getUrl().toString());
        // System.out.println(sm.toString());
        for (SiteMapURL u : sm.getSiteMapUrls()) {
            for (Entry<Extension, ExtensionMetadata[]> x : u.getAttributes().entrySet()) {
                assertEquals(Extension.PAGEMAPS, x.getKey());
                PageMap pageMap = (PageMap) x.getValue()[0];
                List<PageMapDataObject> dataObjects = pageMap.getPageMapDataObjects();
                PageMapDataObject dataObject;
                switch (u.getUrl().toString()) {
                    case "http://www.example.com/foo":
                    assertEquals(2, dataObjects.size());
                    dataObject = dataObjects.get(0);
                    assertEquals("document", dataObject.getType());
                    assertEquals("one", dataObject.getId());
                    assertEquals("Doc One", dataObject.getAttribute("name"));
                    assertEquals("3.5", dataObject.getAttribute("review"));
                    dataObject = dataObjects.get(1);
                    assertEquals("image", dataObject.getType());
                    assertNull(dataObject.getId());
                    assertEquals("http://www.example.com/foo.gif", dataObject.getAttribute("image_src"));
                        break;
                    case "http://www.example.com/bar":
                    assertEquals(1, dataObjects.size());
                    dataObject = dataObjects.get(0);
                    assertEquals("document", dataObject.getType());
                    assertEquals("two", dataObject.getId());
                    assertEquals("Doc Two", dataObject.getAttribute("name"));
                    assertEquals("4.0", dataObject.getAttribute("review"));
                        break;
                }
            }
        }
    }
}
