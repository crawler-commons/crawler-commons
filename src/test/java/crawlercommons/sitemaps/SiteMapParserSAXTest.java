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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SiteMapParserSAXTest {

    private static final Logger LOG = LoggerFactory.getLogger(SiteMapParserSAXTest.class);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSitemapIndex() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParserSAX();
        String contentType = "text/xml";
        StringBuilder scontent = new StringBuilder(1024);
        scontent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n").append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n").append(" <sitemap>\n")
                        .append("  <loc>http://www.example.com/sitemap1.xml.gz</loc>\n").append("  <lastmod><![CDATA[2004-10-01T18:23:17+00:00]]></lastmod>\n").append(" </sitemap>\n")
                        .append("<sitemap>\n").append("  <loc>http://www.example.com/sitemap2.xml.gz</loc>\n").append("  <lastmod>2005-01-01</lastmod>\n").append(" </sitemap>\n")
                        .append("<sitemap>\n").append("  <loc>http://www.example.com/dynsitemap?date=now&amp;all=true</loc>\n").append(" </sitemap>\n").append("<sitemap>\n")
                        .append("  <loc>http://www.example.com/dynsitemap<![CDATA[?date=lastyear&all=false]]></loc>\n").append(" </sitemap>\n").append("</sitemapindex>");
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
    public void testFullDateFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm+hh:00", Locale.ROOT);
        Date date = new Date();
        LOG.info(format.format(date));
        LOG.info(SiteMap.getFullDateFormat().format(date));
    }

    @Test
    public void testSitemapTXT() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParserSAX();
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
        SiteMapParser parser = new SiteMapParserSAX();
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
    public void testSitemapXML() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParserSAX();
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
            assertEquals(sitemapURLs[i].replaceAll("&amp;", "&"), found[i].getUrl().toExternalForm());
        }
    }

    @Test
    public void testSitemapXMLMediaTypes() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParserSAX();
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
                assertEquals(sitemapURLs[i].replaceAll("&amp;", "&"), found[i].getUrl().toExternalForm());
            }
        }
    }

    /**
     * This Sitemap contains badly formatted XML and can't be read
     */
    @Test(expected = UnknownFormatException.class)
    public void testSitemapParserBrokenXml() throws IOException, UnknownFormatException {
        SiteMapParser parser = new SiteMapParserSAX();
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
        SiteMapParser parser = new SiteMapParserSAX();
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
        SiteMapParser parser = new SiteMapParserSAX();
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
        SiteMapParser parser = new SiteMapParserSAX();
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
        SiteMapParser parser = new SiteMapParserSAX();
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
    public void testSitemapWithOctetMediaType() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParserSAX();
        String contentType = "application/octet-stream";
        byte[] content = getXMLSitemapAsBytes();
        URL url = new URL("http://www.example.com/sitemap");

        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);

        SiteMap sm = (SiteMap) asm;
        assertEquals(5, sm.getSiteMapUrls().size());

        SiteMapURL[] found = sm.getSiteMapUrls().toArray(new SiteMapURL[5]);
        for (int i = 0; i < found.length; i++) {
            assertEquals(sitemapURLs[i], found[i].getUrl().toExternalForm());
        }
    }

    @Test
    public void testLenientParser() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParserSAX();
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
        parser = new SiteMapParserSAX(false);
        asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);

        sm = (SiteMap) asm;
        assertEquals(1, sm.getSiteMapUrls().size());
        assertFalse(sm.getSiteMapUrls().iterator().next().isValid());
    }

    @Test
    public void testAtomFormat() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParserSAX();
        byte[] content = getResourceAsBytes("src/test/resources/sitemaps/atom.xml");
        URL url = new URL("http://example.org/atom.xml");

        SiteMap sm = (SiteMap) parser.parseSiteMap(content, url);
        assertEquals(1, sm.getSiteMapUrls().size());
        assertEquals(new URL("http://example.org/2003/12/13/atom03"), sm.getSiteMapUrls().iterator().next().getUrl());
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
        SiteMapParser parser = new SiteMapParserSAX();

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
    }

    @Test
    public void testPartialSitemapsAllowed() throws UnknownFormatException, IOException {

        SiteMapParser parser = new SiteMapParserSAX(false, true);
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
    public void testUrlLocUrl() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParserSAX(false);
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

        SiteMapParser parser = new SiteMapParserSAX(false, true);
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

    /**
     * Returns a good simple default XML sitemap as a byte array
     */
    private byte[] getXMLSitemapAsBytes() {
        StringBuilder scontent = new StringBuilder(1024);
        scontent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        scontent.append("<url>  <loc>").append(sitemapURLs[0]).append("</loc>  <lastmod>2005-01-01</lastmod>").append("  <changefreq>monthly</changefreq>").append("  <priority>0.8</priority>")
                        .append("</url>");
        scontent.append("<url>  <loc>").append(sitemapURLs[1]).append("</loc>  <changefreq>weekly</changefreq>").append("</url>");
        scontent.append("<url>  <loc>").append(sitemapURLs[2]).append("</loc>  <lastmod>2004-12-23</lastmod>").append("  <changefreq>weekly</changefreq>").append("</url>");
        scontent.append("<url>  <loc>").append(sitemapURLs[3]).append("</loc>  <lastmod>2004-12-23T18:00:15+00:00</lastmod>").append("  <priority>0.3</priority>").append("</url>");
        scontent.append("<url>  <loc><url><![CDATA[").append(sitemapURLs[4]).append("]]></url></loc>  <lastmod>2004-11-23</lastmod>").append("</url>");
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

    private static String[] sitemapURLs = new String[] { "http://www.example.com/", "http://www.example.com/catalog?item=12&amp;desc=vacation_hawaii",
                    "http://www.example.com/catalog?item=73&amp;desc=vacation_new_zealand", "http://www.example.com/catalog?item=74&amp;desc=vacation_newfoundland",
                    "http://www.example.com/catalog?item=83&desc=vacation_usa" };

}
