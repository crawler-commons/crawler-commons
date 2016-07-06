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
 * Parse XML that contains a valid Sitemap. Example of a Sitemap: <?xml
 * version="1.0" encoding="UTF-8"?> <urlset
 * xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"> <url> <loc>http:
 * //www.example.com/</loc> <lastmod>2005-01-01</lastmod>
 * <changefreq>monthly</changefreq> <priority>0.8</priority> </url> <url> <loc
 * >http://www.example.com/catalog?item=12&amp;desc=vacation_hawaii </loc>
 * <changefreq>weekly</changefreq> </url> </urlset>
 * 
 * @author mdeboer
 */
class XMLHandler extends DelegatorHandler {

    private SiteMap sitemap;
    private URL loc;
    private String lastMod;
    private String changeFreq;
    private String priority;
    boolean valid;
    private int i = 0;

    XMLHandler(URL url, LinkedList<String> elementStack, boolean strict) {
        super(elementStack, strict);
        sitemap = new SiteMap(url);
        sitemap.setType(SitemapType.XML);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        LOG.debug("here we go");
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("url".equals(qName) && "urlset".equals(currentElementParent())) {
            maybeAddSiteMapUrl();
        } else if ("urlset".equals(qName)) {
            sitemap.setProcessed(true);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        String qName = super.currentElement();
        String value = String.valueOf(ch, start, length);
        if ("loc".equals(qName) || "url".equals(qName)) {
            try {
                loc = new URL(value);
                valid = urlIsValid(sitemap.getBaseUrl(), value);
            } catch (MalformedURLException e) {
                LOG.debug("Bad url: [{}]", value);
                LOG.trace("Can't create an entry with a bad URL", e);
            }
        } else if ("changefreq".equals(qName)) {
            changeFreq = value;
        } else if ("lastmod".equals(qName)) {
            lastMod = value;
        } else if ("priority".equals(qName)) {
            priority = value;
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
                SiteMapURL sUrl = new SiteMapURL(loc.toString(), lastMod, changeFreq, priority, valid);
                sitemap.addSiteMapUrl(sUrl);
                LOG.debug("  {}. {}", (++i), sUrl);
            }
        }
        loc = null;
        lastMod = null;
        changeFreq = null;
        priority = null;
    }

    public void error(SAXParseException e) throws SAXException {
        maybeAddSiteMapUrl();
    }

    public void fatalError(SAXParseException e) throws SAXException {
        maybeAddSiteMapUrl();
    }
}
