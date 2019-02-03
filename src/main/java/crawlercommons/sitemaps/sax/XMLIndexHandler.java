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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.AbstractSiteMap.SitemapType;

/**
 * Parse XML that contains a Sitemap Index. Example Sitemap Index:
 * 
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 *   <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
 *     <sitemap>
 *       <loc>http://www.example.com/sitemap1.xml.gz</loc>
 *       <lastmod>2004-10-01T18:23:17+00:00</lastmod>
 *     </sitemap>
 *     <sitemap>
 *       <loc>http://www.example.com/sitemap2.xml.gz</loc>
 *       <lastmod>2005-01-01</lastmod>
 *     </sitemap>
 *   </sitemapindex>
 * }
 * </pre>
 */
class XMLIndexHandler extends DelegatorHandler {

    private SiteMapIndex sitemap;
    private StringBuilder loc;
    private boolean locClosed;
    private Date lastMod;
    private int i = 0;

    XMLIndexHandler(URL url, LinkedList<String> elementStack, boolean strict) {
        super(elementStack, strict);
        sitemap = new SiteMapIndex(url);
        sitemap.setType(SitemapType.INDEX);
        loc = new StringBuilder();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // flush any unclosed or missing <sitemap> element
        if (loc.length() > 0 && ("loc".equals(localName) || "sitemap".equals(localName))) {
            if (!isAllBlank(loc)) {
                maybeAddSiteMap();
                return;
            }
            loc = new StringBuilder();
            if ("sitemap".equals(localName)) {
                // reset also attributes
                locClosed = false;
                lastMod = null;
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isStrictNamespace() && !isAcceptedNamespace(uri)) {
            return;
        }
        if ("sitemap".equals(currentElement())) {
            maybeAddSiteMap();
        } else if ("sitemapindex".equals(currentElement())) {
            sitemap.setProcessed(true);
        } else if ("loc".equals(currentElement())) {
            locClosed = true;
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        String localName = super.currentElement();
        String value = String.valueOf(ch, start, length);
        if ("loc".equals(localName)) {
            loc.append(value);
        } else if ("lastmod".equals(localName)) {
            lastMod = SiteMap.convertToDate(value);
        } else {
            value = value.trim();
            if (!value.isEmpty() && !locClosed) {
                // try non-whitespace text content as loc
                // when no loc element has been specified
                loc.append(value);
            }
        }
    }

    public AbstractSiteMap getSiteMap() {
        return sitemap;
    }

    private void maybeAddSiteMap() {
        String value = loc.toString().trim();
        try {
            // check that the value is a valid URL
            URL locURL = new URL(value);
            SiteMap s = new SiteMap(locURL, lastMod);
            sitemap.addSitemap(s);
            LOG.debug("  {}. {}", (i + 1), s);
        } catch (MalformedURLException e) {
            LOG.trace("Don't create an entry with a bad URL", e);
            LOG.debug("Bad url: [{}]", value);
        }
        loc = new StringBuilder();
        locClosed = false;
        lastMod = null;
    }

    public void error(SAXParseException e) throws SAXException {
        maybeAddSiteMap();
    }

    public void fatalError(SAXParseException e) throws SAXException {
        maybeAddSiteMap();
    }
}
