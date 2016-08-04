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
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMap;
import crawlercommons.sitemaps.SiteMapURL;
import crawlercommons.sitemaps.AbstractSiteMap.SitemapType;

/**
 * Parse the XML document which is assumed to be in Atom format. Atom 1.0
 * example:
 * 
 * <?xml version="1.0" encoding="utf-8"?> <feed
 * xmlns="http://www.w3.org/2005/Atom">
 * 
 * <title>Example Feed</title> <subtitle>A subtitle.</subtitle> <link
 * href="http://example.org/feed/" rel="self"/> <link
 * href="http://example.org/"/> <modified>2003-12-13T18:30:02Z</modified>
 * <author> <name>John Doe</name> <email>johndoe@example.com</email> </author>
 * <id>urn:uuid:60a76c80-d399-11d9-b91C-0003939e0af6</id>
 * 
 * <entry> <title>Atom-Powered Robots Run Amok</title> <link
 * href="http://example.org/2003/12/13/atom03"/>
 * <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>
 * <updated>2003-12-13T18:30:02Z</updated> <summary>Some text.</summary>
 * </entry>
 * 
 * </feed>
 */
class AtomHandler extends DelegatorHandler {

    private SiteMap sitemap;
    private URL loc;
    private String lastMod;
    boolean valid;
    private int i = 0;

    AtomHandler(URL url, LinkedList<String> elementStack, boolean strict) {
        super(elementStack, strict);
        sitemap = new SiteMap(url);
        sitemap.setType(SitemapType.ATOM);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("entry".equals(localName)) {
            loc = null;
            lastMod = null;
        } else if ("link".equals(localName)) {
            String href = attributes.getValue("href");
            LOG.debug("href = {}", href);
            try {
                loc = new URL(href);
                valid = urlIsValid(sitemap.getBaseUrl(), href);
            } catch (MalformedURLException e) {
                LOG.trace("Can't create an entry with a bad URL", e);
                LOG.debug("Bad url: [{}]", href);
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("entry".equals(currentElement())) {
            maybeAddSiteMapUrl();
        } else if ("feed".equals(currentElement())) {
            sitemap.setProcessed(true);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        String qName = super.currentElement();
        String value = String.valueOf(ch, start, length);
        if ("updated".equals(qName)) {
            lastMod = value;
        }
    }

    public AbstractSiteMap getSiteMap() {
        return sitemap;
    }

    private void maybeAddSiteMapUrl() {
        if (valid || !isStrict()) {
            if (loc == null) {
                LOG.debug("Missing url");
                LOG.trace("Can't create an entry with a missing URL");
            } else {
                SiteMapURL sUrl = new SiteMapURL(loc.toString(), lastMod, null, null, valid);
                sitemap.addSiteMapUrl(sUrl);
                LOG.debug("  {}. {}", (++i), sUrl);
            }
        }
        loc = null;
        lastMod = null;
    }

    public void error(SAXParseException e) throws SAXException {
        maybeAddSiteMapUrl();
    }

    public void fatalError(SAXParseException e) throws SAXException {
        maybeAddSiteMapUrl();
    }

}
