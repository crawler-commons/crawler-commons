/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

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
        scontent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">")
                .append("<sitemap>")
                .append("  <loc>http://www.example.com/sitemap1.xml.gz</loc>")
                .append("  <lastmod>2004-10-01T18:23:17+00:00</lastmod>")
                .append("</sitemap>")
                .append("<sitemap>")
                .append("  <loc>http://www.example.com/sitemap2.xml.gz</loc>")
                .append("  <lastmod>2005-01-01</lastmod>")
                .append("</sitemap>")
                .append("</sitemapindex>");
        byte[] content = scontent.toString().getBytes();
        URL url = new URL("http://www.example.com/sitemapindex.xml");

        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(true, asm.isIndex());
        assertEquals(true, asm instanceof SiteMapIndex);

        SiteMapIndex smi = (SiteMapIndex) asm;
        assertEquals(2, smi.getSitemaps().size());

        AbstractSiteMap currentSiteMap = smi.getSitemap(new URL("http://www.example.com/sitemap1.xml.gz"));
        assertNotNull(currentSiteMap);
        assertEquals("http://www.example.com/sitemap1.xml.gz", currentSiteMap.getUrl().toString());
        assertEquals(SiteMap.convertToDate("2004-10-01T18:23:17+00:00"), currentSiteMap.getLastModified());

        currentSiteMap = smi.getSitemap(new URL("http://www.example.com/sitemap2.xml.gz"));
        assertNotNull(currentSiteMap);
        assertEquals("http://www.example.com/sitemap2.xml.gz", currentSiteMap.getUrl().toString());
        assertEquals(SiteMap.convertToDate("2005-01-01"), currentSiteMap.getLastModified());
    }

    @Test
    public void testSitemapTXT() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "text/plain";
        String scontent = "http://www.example.com/catalog?item=1\nhttp://www.example.com/catalog?item=11";
        byte[] content = scontent.getBytes();
        URL url = new URL("http://www.example.com/sitemap.txt");

        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);

        SiteMap sm = (SiteMap) asm;
        assertEquals(2, sm.getSiteMapUrls().size());
    }

    @Test
    @Ignore
    public void testSitemapTXTWithXMLExt() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String scontent = "http://www.example.com/catalog?item=1\nhttp://www.example.com/catalog?item=11";
        byte[] content = scontent.getBytes();
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
        SiteMapParser parser = new SiteMapParser();
        String contentType = "text/xml";
        byte[] content = getXMLSitemapAsBytes();
        URL url = new URL("http://www.example.com/sitemap.xml");

        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);

        SiteMap sm = (SiteMap) asm;
        assertEquals(5, sm.getSiteMapUrls().size());
    }

    @Test
    public void testSitemapXMLMediaTypes() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        byte[] content = getXMLSitemapAsBytes();
        URL url = new URL("http://www.example.com/sitemap.nonXmlExt");

        // TODO (Avi) Remove "application/x-xml" when changing to Tika MediaType recognition, add it back after release of Tike v1.6
        final String[] XML_CONTENT_TYPES = new String[]{"text/xml", "application/xml", "application/atom+xml", "application/rss+xml", "application/x-xml"};
        for (String contentType: XML_CONTENT_TYPES) {
            AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
            assertEquals(false, asm.isIndex());
            assertEquals(true, asm instanceof SiteMap);
            SiteMap sm = (SiteMap) asm;
            assertEquals(5, sm.getSiteMapUrls().size());
        }
    }

    /**
     * This Sitemap contains badly formatted XML and can't be read
     * */
    @Test (expected = UnknownFormatException.class)
    public void testSitemapParserBrokenXml() throws IOException, UnknownFormatException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "text/xml";
        StringBuilder scontent = new StringBuilder(1024);
        scontent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">")
                .append("<url><!-- This file is not a valid XML file --></url>")
                .append("<url><loc> http://cs.harding.edu/fmccown/sitemaps/something.html</loc>")
                .append("</url><!-- missing opening url tag --></url></urlset>");
        byte[] content = scontent.toString().getBytes();
        URL url = new URL("http://www.example.com/sitemapindex.xml");

        parser.parseSiteMap(contentType, content, url); // This Sitemap contains badly formatted XML and can't be read
    }

    @Test
    public void testSitemapGZ() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "application/gzip";
        File gzSitemapFile = new File("src/test/resources/xmlSitemap.gz");
        InputStream is = new FileInputStream(gzSitemapFile);
        byte[] content = IOUtils.toByteArray(is);

        URL url = new URL("http://www.example.com/sitemap.xml.gz");
        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);
        SiteMap sm = (SiteMap) asm;
        assertEquals(5, sm.getSiteMapUrls().size());
    }

    @Test
    public void testSitemapGZMediaTypes() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        File gzSitemapFile = new File("src/test/resources/xmlSitemap.gz");
        InputStream is = new FileInputStream(gzSitemapFile);
        byte[] content = IOUtils.toByteArray(is);

        // TODO (Avi) Remove "application/x-gunzip", "application/gzipped", "application/gzip-compressed", "gzip/document" when changing to Tika MediaType recognition, add them back after release of Tike v1.6
        final String[] GZ_CONTENT_TYPES = new String[]{"application/gzip", "application/x-gzip", "application/x-gunzip", "application/gzipped", "application/gzip-compressed", "gzip/document"};
        for (String contentType: GZ_CONTENT_TYPES) {
            URL url = new URL("http://www.example.com/sitemap");
            AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
            assertEquals(false, asm.isIndex());
            assertEquals(true, asm instanceof SiteMap);
            SiteMap sm = (SiteMap) asm;
            assertEquals(5, sm.getSiteMapUrls().size());
        }
    }

    @Test (expected = UnknownFormatException.class)
    @Ignore
    public void testSitemapWithOctetMediaType() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "application/octet-stream";
        byte[] content = getXMLSitemapAsBytes();
        URL url = new URL("http://www.example.com/sitemap");

        AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
        assertEquals(false, asm.isIndex());
        assertEquals(true, asm instanceof SiteMap);

        SiteMap sm = (SiteMap) asm;
        assertEquals(5, sm.getSiteMapUrls().size());
    }

    @Test
    public void testLenientParser() throws UnknownFormatException, IOException {
        SiteMapParser parser = new SiteMapParser();
        String contentType = "text/xml";
        StringBuilder scontent = new StringBuilder(1024);
        scontent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">")
                .append("<url>")
                .append("<loc>http://www.example.com/</loc>")
                .append("</url>")
                .append("</urlset>");
        byte[] content = scontent.toString().getBytes();

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

    /** Returns a good simple default XML sitemap as a byte array */
    private byte[] getXMLSitemapAsBytes() {
        StringBuilder scontent = new StringBuilder(1024);
        scontent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">")
                .append("<url>")
                .append("  <loc>http://www.example.com/</loc>")
                .append("  <lastmod>2005-01-01</lastmod>")
                .append("  <changefreq>monthly</changefreq>")
                .append("  <priority>0.8</priority>")
                .append("</url>")
                .append("<url>")
                .append("  <loc>http://www.example.com/catalog?item=12&amp;desc=vacation_hawaii</loc>")
                .append("  <changefreq>weekly</changefreq>")
                .append("</url>")
                .append("<url>")
                .append("  <loc>http://www.example.com/catalog?item=73&amp;desc=vacation_new_zealand</loc>")
                .append("  <lastmod>2004-12-23</lastmod>")
                .append("  <changefreq>weekly</changefreq>")
                .append("</url>")
                .append("<url>")
                .append("  <loc>http://www.example.com/catalog?item=74&amp;desc=vacation_newfoundland</loc>")
                .append("  <lastmod>2004-12-23T18:00:15+00:00</lastmod>")
                .append("  <priority>0.3</priority>")
                .append("</url>")
                .append("<url>")
                .append("  <loc>http://www.example.com/catalog?item=83&amp;desc=vacation_usa</loc>")
                .append("  <lastmod>2004-11-23</lastmod>")
                .append("</url>")
                .append("</urlset>");

        return scontent.toString().getBytes();
    }
}