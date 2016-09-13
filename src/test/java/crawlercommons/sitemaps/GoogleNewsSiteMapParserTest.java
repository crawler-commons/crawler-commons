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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Lavelle
 *
 */
public class GoogleNewsSiteMapParserTest extends AbstractSiteMapTest {

  @Test
  public void testGoogleNewsSiteMap() throws UnknownFormatException, IOException {
    
    SiteMapParser parser = new SiteMapParser();
    String contentType = "text/xml";
    URL url = new URL("http://www.example.org/sitemapindex.xml");
    StringBuilder scontent = new StringBuilder(1024);
    scontent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" + 
        "        xmlns:news=\"http://www.google.com/schemas/sitemap-news/0.9\">\n" + 
        "  <url>\n" + 
        "    <loc>http://www.example.org/business/article55.html</loc>\n" + 
        "    <news:news>\n" + 
        "      <news:publication>\n" + 
        "        <news:name>The Sample Times</news:name>\n" + 
        "        <news:language>en</news:language>\n" + 
        "      </news:publication>\n" + 
        "      <news:genres>PressRelease, Blog</news:genres>\n" + 
        "      <news:publication_date>2008-12-23</news:publication_date>\n" + 
        "      <news:title>Companies A, B in Merger Talks</news:title>\n" + 
        "      <news:keywords>business, merger, acquisition, A, B</news:keywords>\n" + 
        "      <news:stock_tickers>NASDAQ:A, NASDAQ:B</news:stock_tickers>\n" + 
        "    </news:news>\n" + 
        "  </url>\n" + 
        "</urlset>");                 
    byte[] content = scontent.toString().getBytes("UTF-8");
    AbstractSiteMap asm = parser.parseSiteMap(contentType, content, url);
    assertEquals(false, asm.isIndex());
    assertEquals(true, asm instanceof SiteMap);

    SiteMap sm = (SiteMap) asm;
    assertEquals(1, sm.getSiteMapUrls().size());
    assertEquals("http://www.example.org/business/article55.html", sm.getSiteMapUrls().iterator().next().getUrl().toString());
    SiteMapURL siteMapUrl = sm.getSiteMapUrls().iterator().next();
      assertTrue(siteMapUrl instanceof GoogleNewsSiteMapURL);
      
    GoogleNewsSiteMapURL googleNewsSiteMapUrl = (GoogleNewsSiteMapURL)siteMapUrl;
    assertEquals("Companies A, B in Merger Talks", googleNewsSiteMapUrl.getTitle());
    List<String> expectedGenres = Arrays.asList("PressRelease","Blog");
    List<String> expectedKeywords = Arrays.asList("business","merger","acquisition","A","B");
    List<String> expectedStockTickers = Arrays.asList("NASDAQ:A","NASDAQ:B");

    assertEquals(expectedKeywords, googleNewsSiteMapUrl.getKeywords());
    assertEquals(expectedGenres, googleNewsSiteMapUrl.getGenres());
    assertEquals(expectedStockTickers, googleNewsSiteMapUrl.getStockTickers());
    assertEquals("The Sample Times", googleNewsSiteMapUrl.getPublicationName());
    assertEquals("en", googleNewsSiteMapUrl.getPublicationLanguage());
    assertEquals("Tue Dec 23 00:00:00 GMT 2008", googleNewsSiteMapUrl.getPublicationDate().toString());
  }
}
