/**
 * Copyright 2016 Crawler-Commons
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import crawlercommons.sitemaps.AbstractSiteMap.SitemapType;

@RunWith(JUnit4.class)
public class SiteMapParserTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSitemapIndex() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "text/xml";
        StringBuilder scontent = new StringBuilder(1024);
        scontent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n") //
                        .append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n") //
                        .append(" <sitemap>\n") //
                        .append("  <loc>http://www.example.com/sitemap1.xml.gz</loc>\n") //
                        .append("  <lastmod><![CDATA[2004-10-01T18:23:17+00:00]]></lastmod>\n") //
                        .append(" </sitemap>\n") //
                        .append("<sitemap>\n") //
                        .append("  <loc>http://www.example.com/sitemap2.xml.gz</loc>\n") //
                        .append("  <lastmod>2005-01-01</lastmod>\n") //
                        .append(" </sitemap>\n") //
                        .append("<sitemap>\n") //
                        .append("  <loc>http://www.example.com/dynsitemap?date=now&amp;all=true</loc>\n") //
                        .append(" </sitemap>\n") //
                        .append("<sitemap>\n") //
                        .append("  <loc>http://www.example.com/dynsitemap<![CDATA[?date=lastyear&all=false]]></loc>\n") //
                        .append(" </sitemap>\n") //
                        .append("</sitemapindex>");
        byte[] content = scontent.toString().getBytes(UTF_8);
        URL url = new URL("http://www.example.com/sitemapindex.xml");

        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(true, asm.isIndex());
        assertEquals(true, asm instanceof SiteMapIndex);

        SiteMapIndex smi = (SiteMapIndex) asm;
        assertEquals(4, smi.getSitemaps().size());

        AbstractSiteMap currentSiteMap = smi.getSitemap(new URL("http://www.example.com/sitemap1.xml.gz"));
        assertNotNull(currentSiteMap);
        assertEquals("http://www.example.com/sitemap1.xml.gz", currentSiteMap.getUrl().toString());
        assertEquals(SiteMap.convertToDate("2004-10-01T18:23:17+00:00"), currentSiteMap.getLastModified());

        assertTrue(currentSiteMap.toString().contains("T18:23"));

        currentSiteMap = smi.getSitemap(new URL("http://www.example.com/sitemap2.xml.gz"));
        assertNotNull(currentSiteMap);
        assertEquals("http://www.example.com/sitemap2.xml.gz", currentSiteMap.getUrl().toString());
        assertEquals(SiteMap.convertToDate("2005-01-01"), currentSiteMap.getLastModified());

        currentSiteMap = smi.getSitemap(new URL("http://www.example.com/dynsitemap?date=now&all=true"));
        assertNotNull("<loc> with entities not found", currentSiteMap);
        assertEquals("http://www.example.com/dynsitemap?date=now&all=true", currentSiteMap.getUrl().toString());

        currentSiteMap = smi.getSitemap(new URL("http://www.example.com/dynsitemap?date=lastyear&all=false"));
        assertNotNull("<loc> with CDATA not found", currentSiteMap);
        assertEquals("http://www.example.com/dynsitemap?date=lastyear&all=false", currentSiteMap.getUrl().toString());
    }

    @Test
    public void testSitemapWithNamespace() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.setStrictNamespace(true);
        byte[] content = getResourceAsBytes("src/test/resources/sitemaps/sitemap.ns.xml");

        URL url = new URL("http://www.example.com/sitemap.ns.xml");
        AbstractSiteMap asm = parser.parseSiteMap(content, url);
        assertEquals(SitemapType.XML, asm.getType());
        assertEquals(true, asm instanceof SiteMap);
        assertEquals(true, asm.isProcessed());
        SiteMap sm = (SiteMap) asm;

        assertEquals(2, sm.getSiteMapUrls().size());
        assertEquals(SiteMapURL.ChangeFrequency.DAILY, sm.getSiteMapUrls().iterator().next().getChangeFrequency());
    }

    @Test
    public void testSitemapWithWrongNamespace() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        parser.setStrictNamespace(true);

        byte[] content = getResourceAsBytes("src/test/resources/sitemaps/sitemap.badns.xml");

        URL url = new URL("http://www.example.com/sitemap.badns.xml");
        AbstractSiteMap asm;
        try {
            asm = parser.parseSiteMap(content, url);
            fail("Expected an UnknownFormatException because of wrong namespace");
        } catch (UnknownFormatException e) {
            assertTrue(e.getMessage().matches("Namespace .* not accepted"));
        }

        // try again in lenient mode
        parser.setStrictNamespace(false);
        asm = parser.parseSiteMap(content, url);
        assertEquals(SitemapType.XML, asm.getType());
        assertEquals(true, asm instanceof SiteMap);
        assertEquals(true, asm.isProcessed());
        SiteMap sm = (SiteMap) asm;

        assertEquals(2, sm.getSiteMapUrls().size());
    }

    @Test
    public void testSitemapTXT() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "text/plain";
        String scontent = "http://www.example.com/catalog?item=1\nhttp://www.example.com/catalog?item=11";
        byte[] content = scontent.getBytes(UTF_8);
        URL url = new URL("http://www.example.com/sitemap.txt");

        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);

        SiteMap sm = (SiteMap) asm;
        assertEquals(2, sm.getSiteMapUrls().size());
    }

    @Test
    public void testSitemapTXTWithXMLExt() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String scontent = "http://www.example.com/catalog?item=1\nhttp://www.example.com/catalog?item=11";
        byte[] content = scontent.getBytes(UTF_8);
        URL url = new URL("http://www.example.com/sitemap.xml");
        String contentType = "text/plain";

        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);

        SiteMap sm = (SiteMap) asm;
        assertEquals(2, sm.getSiteMapUrls().size());
    }

    @Test
    public void testSitemapTXTWithWrongMimeType() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String scontent = "http://www.example.com/catalog?item=1\nhttp://www.example.com/catalog?item=11";
        byte[] content = scontent.getBytes(UTF_8);
        URL url = new URL("http://www.example.com/sitemap.xml");
        String contentType = "application/bogus";

        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);

        SiteMap sm = (SiteMap) asm;
        assertEquals(2, sm.getSiteMapUrls().size());
    }

    @Test
    public void testSitemapXML() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "text/xml";
        byte[] content = getXMLSitemapAsBytes();
        URL url = new URL("http://www.example.com/sitemap.xml");

        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);

        SiteMap sm = (SiteMap) asm;
        assertEquals(5, sm.getSiteMapUrls().size());

        SiteMapURL[] found = sm.getSiteMapUrls().toArray(new SiteMapURL[5]);
        for (int i = 0; i < found.length; i++) {
            assertEquals(SITEMAP_URLS[i].replaceAll("&amp;", "&"), found[i].getUrl().toExternalForm());
        }
    }

    @Test
    public void testSitemapXMLMediaTypes() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        byte[] content = getXMLSitemapAsBytes();
        URL url = new URL("http://www.example.com/sitemap.nonXmlExt");

        final String[] XML_CONTENT_TYPES = new String[] { "text/xml", "application/x-xml", "application/xml", "application/atom+xml", "application/rss+xml" };
        for (String contentType : XML_CONTENT_TYPES) {
            AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
            assertEquals(false, asm.isIndex());
            assertEquals(true, asm instanceof SiteMap);
            SiteMap sm = (SiteMap) asm;
            assertEquals(5, sm.getSiteMapUrls().size());
            SiteMapURL[] found = sm.getSiteMapUrls().toArray(new SiteMapURL[5]);
            for (int i = 0; i < found.length; i++) {
                assertEquals(SITEMAP_URLS[i].replaceAll("&amp;", "&"), found[i].getUrl().toExternalForm());
            }
        }
    }

    /**
     * This Sitemap contains badly formatted XML and can't be read
     */
    @Test(expected = UnknownFormatException.class)
    public void testSitemapParserBrokenXml() throws IOException, UnknownFormatException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "text/xml";
        StringBuilder scontent = new StringBuilder(1024);
        scontent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">")
                        .append("<url><!-- This file is not a valid XML file --></url>").append("<url><loc> http://cs.harding.edu/fmccown/sitemaps/something.html</loc>")
                        .append("</url><!-- missing opening url tag --></url></urlset>");
        byte[] content = scontent.toString().getBytes(UTF_8);
        URL url = new URL("http://www.example.com/sitemapindex.xml");

        parser.parseSiteMap(contentType, content, url); // This Sitemap contains
                                                        // badly formatted XML
                                                        // and can't be read
    }

    @Test
    public void testMissingLocSitemapIndexFile() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        byte[] content = getResourceAsBytes("src/test/resources/sitemaps/sitemap.index.xml");

        URL url = new URL("http://www.example.com/sitemap.index.xml");
        AbstractSiteMap asm = parser.parseSiteMap(content, url);
        assertEquals(true, asm.isIndex());
        assertEquals(true, asm instanceof SiteMapIndex);
        SiteMapIndex sm = (SiteMapIndex) asm;
        assertEquals(15, sm.getSitemaps().size());
        String sitemap = "https://example.com/sitemap.jss?portalCode=10260&lang=en";
        assertNotNull("Sitemap " + sitemap + " not found in sitemap index", sm.getSitemap(new URL(sitemap)));
    }

    @Test
    public void testSitemapGZ() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "application/gzip";
        byte[] content = getResourceAsBytes("src/test/resources/sitemaps/xmlSitemap.gz");

        URL url = new URL("http://www.example.com/sitemap.xml.gz");
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(5, sm.getSiteMapUrls().size());
    }

    @Test
    public void testSitemapTextGZ() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "application/gzip";
        byte[] content = this.getResourceAsBytes("src/test/resources/sitemaps/sitemap.txt.gz");

        URL url = new URL("http://www.example.com/sitemap.txt.gz");
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(5, sm.getSiteMapUrls().size());
    }

    @Test
    public void testSitemapGZMediaTypes() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        byte[] content = getResourceAsBytes("src/test/resources/sitemaps/xmlSitemap.gz");

        final String[] GZ_CONTENT_TYPES = new String[] { "application/gzip", "application/x-gzip", "application/x-gunzip", "application/gzipped", "application/gzip-compressed", "gzip/document" };
        for (String contentType : GZ_CONTENT_TYPES) {
            URL url = new URL("http://www.example.com/sitemap");
            AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
            assertEquals(false, asm.isIndex());
            assertEquals(true, asm instanceof SiteMap);
            SiteMap sm = (SiteMap) asm;
            assertEquals(5, sm.getSiteMapUrls().size());
        }
    }

    @Test(expected = UnknownFormatException.class)
    public void testSitemapWithInvalidContent() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "application/octet-stream";
        byte[] content = "this is a bogus sitemap".getBytes(StandardCharsets.UTF_8);
        URL url = new URL("http://www.example.com/sitemap");

        parser.parseSiteMap(contentType, content, url);
    }

    @Test
    public void testLenientParser() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "text/xml";
        StringBuilder scontent = new StringBuilder(1024);
        scontent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">").append("<url>")
                        .append("<loc>http://www.example.com/</loc>").append("</url>").append("</urlset>");
        byte[] content = scontent.toString().getBytes(UTF_8);

        URL url = new URL("http://www.example.com/subsection/sitemap.xml");
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);

        SiteMap sm = (SiteMap) asm;
        assertEquals(0, sm.getSiteMapUrls().size());

        // Now try again with lenient parsing. We should get one invalid URL
        parser = new SiteMapParser(false);
        asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);

        sm = (SiteMap) asm;
        assertEquals(1, sm.getSiteMapUrls().size());
        assertFalse(sm.getSiteMapUrls().iterator().next().isValid());
    }

    @Test
    public void testAtomFormat() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        byte[] content = getResourceAsBytes("src/test/resources/sitemaps/atom.xml");
        URL url = new URL("http://example.org/atom.xml");

        SiteMap sm = (SiteMap) parser.parseSiteMap(content, url);
        assertEquals(1, sm.getSiteMapUrls().size());
        assertEquals(new URL("http://example.org/2003/12/13/atom03"), sm.getSiteMapUrls().iterator().next().getUrl());
    }

    @Test
    public void testRSSFormat() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser(true, false);
        byte[] content = getResourceAsBytes("src/test/resources/rss/feed.rss");
        URL url = new URL("https://www.example.com/index.php?feed/rss");

        SiteMap sm = (SiteMap) parser.parseSiteMap(content, url);
        assertEquals(4, sm.getSiteMapUrls().size());
        Iterator<SiteMapURL> it = sm.getSiteMapUrls().iterator();
        assertEquals(new URL("https://www.example.com/blog/post/1"), it.next().getUrl());
        assertEquals(new URL("https://www.example.com/guid.html"), it.next().getUrl());
        assertEquals(new URL("https://www.example.com/foo?q=a&l=en"), it.next().getUrl());
        assertEquals(new URL("https://www.example.com/foo?q=a&l=fr"), it.next().getUrl());
    }

    /**
     * Test processing RSS 1.0 sitemaps, which don't have an <rss> tag. E.g.
     * http://rss.slashdot.org/slashdot/slashdotMain?format=xml
     * 
     * See https://github.com/crawler-commons/crawler-commons/issues/87
     * 
     * @throws IOException
     * @throws UnknownFormatException
     */
    @Test
    public void testRSS10SyndicationFormat() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();

        String contentType = "text/xml";
        URL url = new URL("http://www.example.com/sitemapindex.xml");
        StringBuilder scontent = new StringBuilder(1024);
        scontent.append("<?xml version=\"1.0\"?>")
                        .append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"  xmlns=\"http://purl.org/rss/1.0/\">")
                        .append("<channel rdf:about=\"http://www.xml.com/xml/news.rss\">")
                        .append("<title>XML.com</title>")
                        .append("<link>http://www.example.com/pub</link>")
                        .append("<description>example.com</description>")
                        .append("<image rdf:resource=\"http://www.example.com/universal/images/xml_tiny.gif\" />")
                        .append("<items><rdf:Seq><rdf:li resource=\"http://www.example.com/pub/2000/08/09/xslt/xslt.html\" />")
                        .append("<rdf:li resource=\"http://www.example.com/pub/2000/08/09/rdfdb/index.html\" /></rdf:Seq></items></channel>")
                        .append("<image rdf:about=\"http://www.example.com/universal/images/xml_tiny.gif\"><title>XML.com</title><link>http://www.xml.com</link>")
                        .append("<url>http://www.example.com/universal/images/xml_tiny.gif</url></image>")
                        .append("<item rdf:about=\"http://www.example.com/pub/2000/08/09/xslt/xslt.html\"><title>Processing Inclusions with XSLT</title>")
                        .append("<link>http://www.example.com/pub/2000/08/09/xslt/xslt.html</link>")
                        .append("<description>Processing document inclusions with general XML tools can be problematic. This article proposes a way of preserving inclusion"
                                        + "information through SAX-based processing. </description> </item> </rdf:RDF>");
        byte[] content = scontent.toString().getBytes(UTF_8);
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);

        SiteMap sm = (SiteMap) asm;
        assertEquals(1, sm.getSiteMapUrls().size());
        assertEquals("http://www.example.com/pub/2000/08/09/xslt/xslt.html", sm.getSiteMapUrls().iterator().next().getUrl().toString());

        // Test RDF content type
        contentType = "application/rdf+xml";
        asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(1, ((SiteMap) asm).getSiteMapUrls().size());

        // Test without content type
        asm = parser.parseSiteMap(content, url);
        assertEquals(1, ((SiteMap) asm).getSiteMapUrls().size());
    }

    @Test
    public void testRSSPubDate() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "text/xml";
        byte[] content = getResourceAsBytes("src/test/resources/rss/xmlRss_pubDate.xml");
        URL url = new URL("http://www.example.com/rss.xml");

        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertSame("Not an RSS", SitemapType.RSS, asm.getType());
        assertNotNull("GMT timestamp not parsed", asm.getLastModified());
        long pubDate = 1483619690000L; // Thu, 05 Jan 17 12:34:50 GMT
        assertEquals("GMT timestamp", pubDate, asm.getLastModified().getTime());
        SiteMap rss = (SiteMap) asm;
        assertEquals("Incorrect items count", 7, rss.getSiteMapUrls().size());
        Iterator<SiteMapURL> it = rss.getSiteMapUrls().iterator();
        assertPubDate("Local differential offset", "article_1", pubDate + 1000, it);
        assertPubDate("Short year", "article_2", pubDate + 2000, it);
        assertPubDate("No weekday", "article_3", pubDate + 3000, it);
        assertPubDate("No weekday and short year", "article_4", pubDate + 4000, it);
        assertPubDate("No time zone(incorrect)", "article_5", null, it);
        assertPubDate("Empty field", "article_6", null, it);
        assertPubDate("Missed field", "article_7", null, it);
    }

    private static void assertPubDate(String message, String path, Long pubDate, Iterator<SiteMapURL> it) {
        assertTrue(message + " item missed", it.hasNext());
        SiteMapURL url = it.next();
        assertEquals(message + " link", "http://www.example.com/" + path, url.getUrl().toString());
        if (pubDate == null) {
            assertNull(message + " pubDate not NULL", url.getLastModified());
        } else {
            assertNotNull(message + " pubDate is missing", url.getLastModified());
            assertEquals(message + " pub date", pubDate.longValue(), url.getLastModified().getTime());
        }
    }

    @Test
    public void testPartialSitemapsAllowed() throws UnknownFormatException, IOException {

        SiteMapParser parser = new SiteMapParser(false, true);
        String contentType = "text/xml";
        StringBuilder scontent = new StringBuilder(1024);
        scontent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">").append("<url>")
                        .append("<loc>http://www.example.com/</lo");

        byte[] content = scontent.toString().getBytes(UTF_8);

        URL url = new URL("http://www.example.com/subsection/sitemap.xml");

        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);

        SiteMap sm = (SiteMap) asm;
        assertEquals(1, sm.getSiteMapUrls().size());
        assertFalse(sm.getSiteMapUrls().iterator().next().isValid());
    }

    @Test
    public void testFileLocationValidation() {
        // examples from https://www.sitemaps.org/protocol.html#location
        String baseUrl = "http://example.com/catalog/sitemap.xml";
        String testUrl = "http://example.com/catalog/show?item=233&user=3453";
        SiteMap sitemap = new SiteMap(baseUrl);
        assertTrue(SiteMapParser.urlIsValid(sitemap.getBaseUrl(), testUrl));

        testUrl = "http://example.com/image/show?item=233&user=3453";
        assertFalse(SiteMapParser.urlIsValid(sitemap.getBaseUrl(), testUrl));

        testUrl = "https://example.com/catalog/page1.php";
        assertFalse(SiteMapParser.urlIsValid(sitemap.getBaseUrl(), testUrl));

        // do not use / in query part to determine base location
        baseUrl = "https://example.com/index.php?route=path/sitemap";
        sitemap = new SiteMap(baseUrl);
        assertTrue(SiteMapParser.urlIsValid(sitemap.getBaseUrl(), testUrl));
    }

    @Test
    public void testUrlLocUrl() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser(false);
        String contentType = "text/xml";
        StringBuilder scontent = new StringBuilder(1024);
        scontent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">").append("<url>").append("<loc>").append("<url>")
                        .append("<![CDATA[").append("http://jobs.optistaffing.com/EXPERIENCED-DISPATCHER-NEEDED-NOW----Jobs-in-Vancouver-WA/2333221").append("]]>").append("</url>").append("</loc>")
                        .append("<lastmod>2015-04-28</lastmod>").append("<changefreq>daily</changefreq>").append("</url>").append("</urlset>");

        byte[] content = scontent.toString().getBytes(UTF_8);

        URL url = new URL("http://www.example.com/subsection/sitemap.xml");
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);

        SiteMap sm = (SiteMap) asm;
        assertEquals(1, sm.getSiteMapUrls().size());
        assertFalse(sm.getSiteMapUrls().iterator().next().isValid());
    }

    @Test
    public void testPartialSitemapIndicesAllowed() throws UnknownFormatException, IOException {

        SiteMapParser parser = new SiteMapParser(false, true);
        String contentType = "text/xml";
        StringBuilder scontent = new StringBuilder(1024);
        scontent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">")
                        .append("<sitemap><loc>http://www.example.com/sitemap1.xml.gz</loc><las");
        byte[] content = scontent.toString().getBytes(UTF_8);

        URL url = new URL("http://www.example.com/subsection/sitemap.xml");

        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(true, asm.isIndex());
        assertEquals(true, asm instanceof SiteMapIndex);

        SiteMapIndex smi = (SiteMapIndex) asm;
        assertEquals(1, smi.getSitemaps().size());
    }

    @Test
    public void testWalkSiteMap() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "text/xml";
        byte[] content = getXMLSitemapAsBytes();
        URL url = new URL("http://www.example.com/sitemap.xml");

        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        final List<SiteMapURL> urls = new ArrayList<>();

        parser.walkSiteMap(asm, urls::add);
        assertEquals(((SiteMap) asm).getSiteMapUrls().size(), urls.size());
    }

    /**
     * Returns a good simple default XML sitemap as a byte array
     */
    private byte[] getXMLSitemapAsBytes() {
        StringBuilder scontent = new StringBuilder(1024);
        scontent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        scontent.append("<url>  <loc>").append(SITEMAP_URLS[0]).append("</loc>  <lastmod>2005-01-01</lastmod>").append("  <changefreq>monthly</changefreq>").append("  <priority>0.8</priority>")
                        .append("</url>");
        scontent.append("<url>  <loc>").append(SITEMAP_URLS[1]).append("</loc>  <changefreq>weekly</changefreq>").append("</url>");
        scontent.append("<url>  <loc>").append(SITEMAP_URLS[2]).append("</loc>  <lastmod>2004-12-23</lastmod>").append("  <changefreq>weekly</changefreq>").append("</url>");
        scontent.append("<url>  <loc>").append(SITEMAP_URLS[3]).append("</loc>  <lastmod>2004-12-23T18:00:15+00:00</lastmod>").append("  <priority>0.3</priority>").append("</url>");
        scontent.append("<url>  <loc><url><![CDATA[").append(SITEMAP_URLS[4]).append("]]></url></loc>  <lastmod>2004-11-23</lastmod>").append("</url>");
        scontent.append("</urlset>");

        return scontent.toString().getBytes(UTF_8);
    }

    /**
     * Read a test resource file and return its content as byte array.
     * 
     * @param resourceName
     *            path to the resource file
     * @return byte content of the file
     * @throws IOException
     */
    private byte[] getResourceAsBytes(String resourceName) throws IOException {
        File file = new File(resourceName);
        InputStream is = new FileInputStream(file);
        return IOUtils.toByteArray(is);
    }

    private static String[] SITEMAP_URLS = { "http://www.example.com/", //
                    "http://www.example.com/catalog?item=12&amp;desc=vacation_hawaii", //
                    "http://www.example.com/catalog?item=73&amp;desc=vacation_new_zealand", //
                    "http://www.example.com/catalog?item=74&amp;desc=vacation_newfoundland", //
                    "http://www.example.com/catalog?item=83&desc=vacation_usa" };

}
