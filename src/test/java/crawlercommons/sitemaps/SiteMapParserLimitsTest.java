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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Test;

/**
 * Test that the limits on the number of URLs and the size of a sitemap are
 * enforced, see {@link SiteMapParser#setMaxUrls(int)} and
 * {@link SiteMapParser#setMaxBytes(long)}.
 */
public class SiteMapParserLimitsTest {

    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    /**
     * Generate a document containing the given number of items.
     * 
     * @param header
     *            content preceding the items
     * @param item
     *            format of a single item, <code>%d</code> is replaced by the
     *            number of the item
     * @param footer
     *            content following the items
     * @param numItems
     *            number of items
     */
    private byte[] generate(String header, String item, String footer, int numItems) {
        StringBuilder sb = new StringBuilder(header);
        for (int i = 0; i < numItems; i++) {
            sb.append(String.format(Locale.ROOT, item, i));
        }
        sb.append(footer);
        return sb.toString().getBytes(UTF_8);
    }

    private byte[] getXmlSitemap(int numUrls) {
        return generate(XML_DECLARATION + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n", //
                        " <url><loc>http://www.example.com/page%d.html</loc></url>\n", "</urlset>", numUrls);
    }

    private byte[] getXmlSitemapIndex(int numSitemaps) {
        return generate(XML_DECLARATION + "<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n", //
                        " <sitemap><loc>http://www.example.com/sitemap%d.xml</loc></sitemap>\n", "</sitemapindex>", numSitemaps);
    }

    private byte[] getRssFeed(int numItems) {
        return generate(XML_DECLARATION + "<rss version=\"2.0\">\n<channel>\n<title>Test</title>\n<link>http://www.example.com/</link>\n", //
                        " <item><link>http://www.example.com/page%d.html</link></item>\n", "</channel>\n</rss>", numItems);
    }

    private byte[] getAtomFeed(int numEntries) {
        return generate(XML_DECLARATION + "<feed xmlns=\"http://www.w3.org/2005/Atom\">\n<title>Test</title>\n", //
                        " <entry><link href=\"http://www.example.com/page%d.html\"/></entry>\n", "</feed>", numEntries);
    }

    private byte[] getTextSitemap(int numUrls) {
        return generate("", "http://www.example.com/page%d.html\n", "", numUrls);
    }

    private byte[] gzip(byte[] content) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(bytes)) {
            gz.write(content);
        }
        return bytes.toByteArray();
    }

    private URL url(String url) throws Exception {
        return new URI(url).toURL();
    }

    private SiteMap parse(SiteMapParser parser, String contentType, byte[] content, String url) throws Exception {
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url(url));
        assertFalse(asm.isIndex());
        assertTrue(asm.isProcessed());
        return (SiteMap) asm;
    }

    @Test
    public void testDefaultLimits() {
        SiteMapParser parser = new SiteMapParser();
        assertEquals(50000, parser.getMaxUrls());
        assertEquals(52428800L, parser.getMaxBytes());
    }

    @Test
    public void testInvalidLimits() {
        SiteMapParser parser = new SiteMapParser();
        assertThrows(IllegalArgumentException.class, () -> parser.setMaxUrls(0));
        assertThrows(IllegalArgumentException.class, () -> parser.setMaxBytes(-1));
    }

    @Test
    public void testXmlSitemapDefaultMaxUrls() throws Exception {
        SiteMapParser parser = new SiteMapParser();
        // a sitemap with exactly the max. number of URLs is not truncated
        SiteMap sm = parse(parser, "text/xml", getXmlSitemap(SiteMapParser.MAX_URLS), "http://www.example.com/sitemap.xml");
        assertEquals(SiteMapParser.MAX_URLS, sm.getSiteMapUrls().size());

        sm = parse(parser, "text/xml", getXmlSitemap(SiteMapParser.MAX_URLS + 1), "http://www.example.com/sitemap.xml");
        assertEquals(SiteMapParser.MAX_URLS, sm.getSiteMapUrls().size());
    }

    @Test
    public void testXmlSitemapMaxUrls() throws Exception {
        SiteMapParser parser = new SiteMapParser();
        parser.setMaxUrls(10);
        SiteMap sm = parse(parser, "text/xml", getXmlSitemap(10), "http://www.example.com/sitemap.xml");
        assertEquals(10, sm.getSiteMapUrls().size());

        sm = parse(parser, "text/xml", getXmlSitemap(25), "http://www.example.com/sitemap.xml");
        assertEquals(10, sm.getSiteMapUrls().size());
        assertEquals("http://www.example.com/page9.html", sm.getSiteMapUrls().toArray(new SiteMapURL[0])[9].getUrl().toString());
    }

    @Test
    public void testXmlSitemapMaxUrlsSkippedNotCounted() throws Exception {
        SiteMapParser parser = new SiteMapParser();
        parser.setMaxUrls(10);
        // skip every second URL
        parser.setURLFilter((String u) -> u.matches(".*[02468]\\.html") ? u : null);
        SiteMap sm = parse(parser, "text/xml", getXmlSitemap(20), "http://www.example.com/sitemap.xml");
        assertEquals(10, sm.getSiteMapUrls().size());
    }

    @Test
    public void testGzippedXmlSitemapMaxUrls() throws Exception {
        SiteMapParser parser = new SiteMapParser();
        parser.setMaxUrls(10);
        SiteMap sm = parse(parser, "application/gzip", gzip(getXmlSitemap(25)), "http://www.example.com/sitemap.xml.gz");
        assertEquals(10, sm.getSiteMapUrls().size());
    }

    @Test
    public void testSitemapIndexMaxUrls() throws Exception {
        SiteMapParser parser = new SiteMapParser();
        parser.setMaxUrls(10);
        AbstractSiteMap asm = parser.parseSiteMap("text/xml", getXmlSitemapIndex(25), url("http://www.example.com/sitemapindex.xml"));
        assertTrue(asm.isIndex());
        assertTrue(asm.isProcessed());
        assertEquals(10, ((SiteMapIndex) asm).getSitemaps().size());
    }

    @Test
    public void testRssMaxUrls() throws Exception {
        SiteMapParser parser = new SiteMapParser();
        parser.setMaxUrls(10);
        SiteMap sm = parse(parser, "application/rss+xml", getRssFeed(25), "http://www.example.com/feed.rss");
        assertEquals(10, sm.getSiteMapUrls().size());
    }

    @Test
    public void testAtomMaxUrls() throws Exception {
        SiteMapParser parser = new SiteMapParser();
        parser.setMaxUrls(10);
        SiteMap sm = parse(parser, "application/atom+xml", getAtomFeed(25), "http://www.example.com/feed.atom");
        assertEquals(10, sm.getSiteMapUrls().size());
    }

    @Test
    public void testTextSitemapMaxUrls() throws Exception {
        SiteMapParser parser = new SiteMapParser();
        parser.setMaxUrls(10);
        SiteMap sm = parse(parser, "text/plain", getTextSitemap(10), "http://www.example.com/sitemap.txt");
        assertEquals(10, sm.getSiteMapUrls().size());

        sm = parse(parser, "text/plain", getTextSitemap(25), "http://www.example.com/sitemap.txt");
        assertEquals(10, sm.getSiteMapUrls().size());

        sm = parse(parser, "application/gzip", gzip(getTextSitemap(25)), "http://www.example.com/sitemap.txt.gz");
        assertEquals(10, sm.getSiteMapUrls().size());
    }

    @Test
    public void testMaxBytesNotExceeded() throws Exception {
        SiteMapParser parser = new SiteMapParser();
        byte[] xml = getXmlSitemap(100);
        byte[] text = getTextSitemap(100);

        // content with a size equal to the limit is accepted
        parser.setMaxBytes(xml.length);
        assertEquals(100, parse(parser, "text/xml", xml, "http://www.example.com/sitemap.xml").getSiteMapUrls().size());
        assertEquals(100, parse(parser, "application/gzip", gzip(xml), "http://www.example.com/sitemap.xml.gz").getSiteMapUrls().size());

        parser.setMaxBytes(text.length);
        assertEquals(100, parse(parser, "text/plain", text, "http://www.example.com/sitemap.txt").getSiteMapUrls().size());
        assertEquals(100, parse(parser, "application/gzip", gzip(text), "http://www.example.com/sitemap.txt.gz").getSiteMapUrls().size());
    }

    @Test
    public void testMaxBytesExceeded() throws Exception {
        SiteMapParser parser = new SiteMapParser();
        byte[] xml = getXmlSitemap(100);
        byte[] text = getTextSitemap(100);

        parser.setMaxBytes(xml.length - 1);
        assertSizeLimitExceeded(parser, "text/xml", xml, "http://www.example.com/sitemap.xml");
        assertSizeLimitExceeded(parser, "application/gzip", gzip(xml), "http://www.example.com/sitemap.xml.gz");

        parser.setMaxBytes(text.length - 1);
        assertSizeLimitExceeded(parser, "text/plain", text, "http://www.example.com/sitemap.txt");
        assertSizeLimitExceeded(parser, "application/gzip", gzip(text), "http://www.example.com/sitemap.txt.gz");
    }

    @Test
    public void testMaxBytesExceededGzipHighlyCompressed() throws Exception {
        SiteMapParser parser = new SiteMapParser();
        parser.setMaxBytes(1024 * 1024);
        // long runs of white space: small if compressed, large if uncompressed
        byte[] xml = generate(XML_DECLARATION + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n", //
                        " <url><loc>http://www.example.com/page%d.html</loc></url>" + " ".repeat(1024) + "\n", "</urlset>", 2048);
        byte[] content = gzip(xml);
        assertTrue(content.length < parser.getMaxBytes());
        assertSizeLimitExceeded(parser, "application/gzip", content, "http://www.example.com/sitemap.xml.gz");
    }

    @Test
    public void testMaxBytesExceededPartialAllowed() throws Exception {
        SiteMapParser parser = new SiteMapParser(true, true);
        byte[] xml = getXmlSitemap(100);
        byte[] text = getTextSitemap(100);

        parser.setMaxBytes(xml.length / 2);
        int n = parse(parser, "text/xml", xml, "http://www.example.com/sitemap.xml").getSiteMapUrls().size();
        assertTrue(n > 0 && n < 100, "Expected truncated sitemap but got " + n + " URLs");
        n = parse(parser, "application/gzip", gzip(xml), "http://www.example.com/sitemap.xml.gz").getSiteMapUrls().size();
        assertTrue(n > 0 && n < 100, "Expected truncated sitemap but got " + n + " URLs");

        parser.setMaxBytes(text.length / 2);
        n = parse(parser, "text/plain", text, "http://www.example.com/sitemap.txt").getSiteMapUrls().size();
        assertTrue(n > 0 && n < 100, "Expected truncated sitemap but got " + n + " URLs");
        n = parse(parser, "application/gzip", gzip(text), "http://www.example.com/sitemap.txt.gz").getSiteMapUrls().size();
        assertTrue(n > 0 && n < 100, "Expected truncated sitemap but got " + n + " URLs");
    }

    private void assertSizeLimitExceeded(SiteMapParser parser, String contentType, byte[] content, String url) {
        UnknownFormatException e = assertThrows(UnknownFormatException.class, () -> parser.parseSiteMap(contentType, content, url(url)));
        assertTrue(e.getMessage().contains("exceeds the size limit"), "Unexpected message: " + e.getMessage());
    }
}
