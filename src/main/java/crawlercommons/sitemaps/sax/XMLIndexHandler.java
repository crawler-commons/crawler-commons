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
    private String loc;
    private boolean locClosed;
    private Date lastMod;
    private int i = 0;

    XMLIndexHandler(URL url, LinkedList<String> elementStack, boolean strict) {
        super(elementStack, strict);
        sitemap = new SiteMapIndex(url);
        sitemap.setType(SitemapType.INDEX);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // flush any unclosed or missing <sitemap> element
        if (loc != null && loc.length() > 0 && ("loc".equals(localName) || "sitemap".equals(localName))) {
            if (!isAllBlank(loc)) {
                maybeAddSiteMap();
                return;
            }
            loc = null;
            if ("sitemap".equals(localName)) {
                // reset also attributes
                locClosed = false;
                lastMod = null;
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isStrictNamespace() && !isAcceptedNamespace(uri)) {
            return;
        }
        if ("sitemap".equals(localName)) {
            if (!locClosed) {
                // closing </sitemap> without closed </loc>
                // try text in <sitemap> as <loc>
                loc = getAndResetCharacterBuffer();
            }
            maybeAddSiteMap();
        } else if ("sitemapindex".equals(localName)) {
            sitemap.setProcessed(true);
        } else if ("lastmod".equals(localName)) {
            String value = getAndResetCharacterBuffer();
            if (value != null) {
                lastMod = SiteMap.convertToDate(value);
            }
        } else if ("loc".equals(localName)) {
            loc = getAndResetCharacterBuffer();
            locClosed = true;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String localName = super.currentElement();
        if ("loc".equals(localName) || "lastmod".equals(localName)) {
            appendCharacterBuffer(ch, start, length);
        } else if (!locClosed) {
            // try non-whitespace text content as loc
            // when no loc element has been specified
            String value = stripAllBlank(String.valueOf(ch, start, length));
            if (!value.isEmpty()) {
                appendCharacterBuffer(value);
            }
        }
    }

    @Override
    public AbstractSiteMap getSiteMap() {
        return sitemap;
    }

    private void maybeAddSiteMap() {
        if (loc == null) {
            return;
        }
        String value = stripAllBlank(loc);
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
        loc = null;
        locClosed = false;
        lastMod = null;
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        maybeAddSiteMap();
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        maybeAddSiteMap();
    }
}
