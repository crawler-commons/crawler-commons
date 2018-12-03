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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import crawlercommons.sitemaps.extension.Extension;
import crawlercommons.sitemaps.extension.ExtensionMetadata;
import crawlercommons.sitemaps.extension.ImageAttributes;
import crawlercommons.sitemaps.extension.LinkAttributes;
import crawlercommons.sitemaps.extension.MobileAttributes;
import crawlercommons.sitemaps.extension.NewsAttributes;
import crawlercommons.sitemaps.extension.VideoAttributes;

@RunWith(JUnit4.class)
public class SiteMapParserExtensionTest {

    @Test
    public void testVideosSitemap() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.enableExtension(Extension.VIDEO);

        String contentType = "text/xml";
        byte[] content = SiteMapParserTest.getResourceAsBytes("src/test/resources/sitemaps/extension/sitemap-videos.xml");

        URL url = new URL("http://www.example.com/sitemap-video.xml");
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(1, sm.getSiteMapUrls().size());
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

        for (SiteMapURL su : sm.getSiteMapUrls()) {
            assertNotNull(su.getAttributesForExtension(Extension.VIDEO));
            VideoAttributes attr = (VideoAttributes) su.getAttributesForExtension(Extension.VIDEO)[0];
            assertEquals(expectedVideoAttributes, attr);
        }
    }

    @Test
    public void testImageSitemap() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.enableExtension(Extension.IMAGE);

        String contentType = "text/xml";
        byte[] content = SiteMapParserTest.getResourceAsBytes("src/test/resources/sitemaps/extension/sitemap-images.xml");

        URL url = new URL("http://www.example.com/sitemap-images.xml");
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
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
            assertEquals(imageAttributes1, attr);
            attr = (ImageAttributes) attrs[1];
            assertEquals(imageAttributes2, attr);
        }
    }

    @SuppressWarnings("serial")
    @Test
    public void testXHTMLLinksSitemap() throws UnknownFormatException, IOException, MalformedURLException {
        SiteMapParser parser = new SiteMapParser();
        parser.enableExtension(Extension.LINKS);

        String contentType = "text/xml";
        byte[] content = SiteMapParserTest.getResourceAsBytes("src/test/resources/sitemaps/extension/sitemap-links.xml");

        URL url = new URL("http://www.example.com/sitemap-links.xml");
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
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
            }
        }
    }

    @Test
    public void testNewsSitemap() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.enableExtension(Extension.NEWS);

        String contentType = "text/xml";
        byte[] content = SiteMapParserTest.getResourceAsBytes("src/test/resources/sitemaps/extension/sitemap-news.xml");

        URL url = new URL("http://www.example.org/sitemap-news.xml");
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(1, sm.getSiteMapUrls().size());
        ZonedDateTime dt = ZonedDateTime.parse("2008-11-23T00:00:00+00:00");
        NewsAttributes expectedNewsAttributes = new NewsAttributes("The Example Times", "en", dt, "Companies A, B in Merger Talks");
        expectedNewsAttributes.setKeywords(new String[] { "business", "merger", "acquisition", "A", "B" });
        expectedNewsAttributes.setGenres(new NewsAttributes.NewsGenre[] { NewsAttributes.NewsGenre.PressRelease, NewsAttributes.NewsGenre.Blog });
        expectedNewsAttributes.setStockTickers(new String[] { "NASDAQ:A", "NASDAQ:B" });
        for (SiteMapURL su : sm.getSiteMapUrls()) {
            assertNotNull(su.getAttributesForExtension(Extension.NEWS));
            NewsAttributes attr = (NewsAttributes) su.getAttributesForExtension(Extension.NEWS)[0];
            assertEquals(expectedNewsAttributes, attr);
        }
    }

    @Test
    public void testMobileSitemap() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.enableExtension(Extension.MOBILE);

        String contentType = "text/xml";
        byte[] content = SiteMapParserTest.getResourceAsBytes("src/test/resources/sitemaps/extension/sitemap-mobile.xml");

        URL url = new URL("http://www.example.org/sitemap-mobile.xml");
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
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

        String contentType = "text/xml";
        byte[] content = SiteMapParserTest.getResourceAsBytes("src/test/resources/sitemaps/extension/shinpaideshou-news-sitemap.xml");

        URL url = new URL("https://shinpaideshou.wordpress.com/news-sitemap.xml");
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
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

        String contentType = "text/xml";
        byte[] content = SiteMapParserTest.getResourceAsBytes("src/test/resources/sitemaps/extension/hebdenbridgetimes-articles-sitemap.xml");

        URL url = new URL("http://www.hebdenbridgetimes.co.uk/sitemap-article-2015-18.xml");
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(74, sm.getSiteMapUrls().size());
    }


    @Test
    public void testVideosSitemapWithError() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.enableExtension(Extension.VIDEO);

        String contentType = "text/xml";
        byte[] content = SiteMapParserTest.getResourceAsBytes("src/test/resources/sitemaps/extension/sitemap-videos-error.xml");

        URL url = new URL("http://www.example.com/sitemap-video.xml");
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(1, sm.getSiteMapUrls().size());
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
        expectedVideoAttributes.setPrices(new VideoAttributes.VideoPrice[] { new VideoAttributes.VideoPrice("EUR", null, VideoAttributes.VideoPriceType.own) });
        expectedVideoAttributes.setRequiresSubscription(true);
        expectedVideoAttributes.setUploader("GrillyMcGrillerson");
        expectedVideoAttributes.setUploaderInfo(new URL("http://www.example.com/users/grillymcgrillerson"));
        expectedVideoAttributes.setLive(false);

        for (SiteMapURL su : sm.getSiteMapUrls()) {
            assertNotNull(su.getAttributesForExtension(Extension.VIDEO));
            VideoAttributes attr = (VideoAttributes) su.getAttributesForExtension(Extension.VIDEO)[0];
            assertEquals(expectedVideoAttributes, attr);
        }
    }

}
