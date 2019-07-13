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

package crawlercommons.sitemaps.sax;

import static crawlercommons.sitemaps.SiteMapParser.LOG;
import static crawlercommons.sitemaps.SiteMapParser.urlIsValid;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMap;
import crawlercommons.sitemaps.SiteMapURL;
import crawlercommons.sitemaps.AbstractSiteMap.SitemapType;

/**
 * Parse XML document which is assumed to be in RSS format. RSS 2.0 example:
 * 
 * <pre>
 * {@code
 * <?xml version="1.0"?>
 *   <rss version="2.0">
 *     <channel>
 *       <title>Lift Off News</title>
 *       <link>http://liftoff.msfc.nasa.gov/</link>
 *       <description>Liftoff to Space Exploration.</description>
 *       <language>en-us</language>
 *       <pubDate>Tue, 10 Jun 2003 04:00:00 GMT</pubDate>
 *       <lastBuildDate>Tue, 10 Jun 2003 09:41:01 GMT</lastBuildDate>
 *       <docs>http://blogs.law.harvard.edu/tech/rss</docs>
 *       <generator>Weblog Editor 2.0</generator>
 *       <managingEditor>editor@example.com</managingEditor>
 *       <webMaster>webmaster@example.com</webMaster>
 *       <ttl>5</ttl>
 *       <item>
 *         <title>Star City</title>
 *         <link>http://liftoff.msfc.nasa.gov/news/2003/news-starcity.asp</link>
 *         <description>How do Americans get ready to work with Russians aboard the
 *         International Space Station? They take a crash course in culture,
 *         language and protocol at Russia's Star City.
 *         </description>
 *         <pubDate>Tue, 03 Jun 2003 09:39:21 GMT</pubDate>
 *         <guid>http://liftoff.msfc.nasa.gov/2003/06/03.html#item573</guid>
 *       </item>
 *       <item>
 *         <title>Space Exploration</title>
 *         <link>http://liftoff.msfc.nasa.gov/</link>
 *         <description>Sky watchers in Europe, Asia, and parts of Alaska and Canada 
 *         will experience a partial eclipse of the Sun on Saturday, May 31.
 *         </description>
 *         <pubDate>Fri, 30 May 2003 11:06:42 GMT</pubDate>
 *         <guid>http://liftoff.msfc.nasa.gov/2003/05/30.html#item572</guid>
 *       </item>
 *     </channel>
 *   </rss>
 * }
 * </pre>
 */
class RSSHandler extends DelegatorHandler {

    private SiteMap sitemap;
    private URL locURL;
    private ZonedDateTime lastMod;
    boolean valid;

    RSSHandler(URL url, LinkedList<String> elementStack, boolean strict) {
        super(elementStack, strict);
        sitemap = new SiteMap(url);
        sitemap.setType(SitemapType.RSS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see crawlercommons.sitemaps.sax.DelegatorHandler#startElement(java.lang
     * .String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see crawlercommons.sitemaps.sax.DelegatorHandler#endElement(java.lang
     * .String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("link".equals(localName)) {
            setLocURL();
        } else if ("guid".equals(localName)) {
            // accept as link if
            // - a valid absolute URL (not a URN, UUID or similar)
            // - and no <link> found yet
            if (locURL == null) {
                setLocURL();
            }
            resetCharacterBuffer();
        } else if ("item".equals(localName)) {
            maybeAddSiteMapUrl();
        } else if ("rss".equals(localName)) {
            sitemap.setProcessed(true);
        } else if ("pubDate".equals(localName)) {
            String value = getAndResetCharacterBuffer();
            lastMod = AbstractSiteMap.parseRSSTimestamp(value);
            if (lastMod != null && "channel".equals(super.currentElementParent())) {
                sitemap.setLastModified(lastMod);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see crawlercommons.sitemaps.sax.DelegatorHandler#characters(char[], int,
     * int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String localName = super.currentElement();
        if ("pubDate".equals(localName) || "link".equals(localName) || "guid".equals(localName)) {
            appendCharacterBuffer(ch, start, length);
        }
    }

    @Override
    public AbstractSiteMap getSiteMap() {
        return sitemap;
    }

    private void setLocURL() {
        String loc = getAndResetCharacterBuffer();
        if (loc == null) {
            return;
        }
        String value = stripAllBlank(loc);
        if (value.isEmpty()) {
            return;
        }
        try {
            // check that the value is a valid URL
            locURL = new URL(sitemap.getUrl(), value);
        } catch (MalformedURLException e) {
            LOG.debug("Bad url: [{}]", value);
            LOG.trace("Can't create an entry with a bad URL", e);
        }
    }

    private void maybeAddSiteMapUrl() {
        if (locURL != null) {
            boolean valid = urlIsValid(sitemap.getBaseUrl(), locURL.toString());
            if (!isStrict() || valid) {
                SiteMapURL sUrl = new SiteMapURL(locURL, valid);
                sUrl.setLastModified(lastMod);
                sitemap.addSiteMapUrl(sUrl);
            }
        }
        locURL = null;
        lastMod = null;
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        maybeAddSiteMapUrl();
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        maybeAddSiteMapUrl();
    }
}
