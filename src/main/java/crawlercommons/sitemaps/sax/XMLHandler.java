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
import crawlercommons.sitemaps.AbstractSiteMap.SitemapType;
import crawlercommons.sitemaps.Namespace;
import crawlercommons.sitemaps.SiteMap;
import crawlercommons.sitemaps.SiteMapURL;

/**
 * Parse XML that contains a valid Sitemap. Example of a Sitemap:
 * 
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 *   <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
 *     <url>
 *       <loc>http://www.example.com/</loc>
 *       <lastmod>lastmod>2005-01-01</lastmod>
 *       <changefreq>monthly</changefreq>
 *       <priority>0.8</priority>
 *     </url>
 *     <url>
 *       <loc>http://www.example.com/catalog?item=12&amp;desc=vacation_hawaii</loc>
 *       <changefreq>weekly</changefreq>
 *     </url>
 *   </urlset>
 * }
 * </pre>
 */
class XMLHandler extends DelegatorHandler {

    private SiteMap sitemap;
    private StringBuilder loc;
    private String lastMod;
    private String changeFreq;
    private String priority;
    private int i = 0;

    XMLHandler(URL url, LinkedList<String> elementStack, boolean strict) {
        super(elementStack, strict);
        sitemap = new SiteMap(url);
        sitemap.setType(SitemapType.XML);
        loc = new StringBuilder();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (isStrictNamespace() && !Namespace.SITEMAP.equals(uri)) {
            return;
        }

        // flush any unclosed or missing URL element
        if (loc.length() > 0 && ("loc".equals(localName) || "url".equals(localName))) {
            // check whether loc isn't white space only
            for (int i = 0; i < loc.length(); i++) {
                if (!Character.isWhitespace(loc.charAt(i))) {
                    maybeAddSiteMapUrl();
                    return;
                }
            }
            loc = new StringBuilder();
            if ("url".equals(localName)) {
                // reset also attributes
                lastMod = null;
                changeFreq = null;
                priority = null;
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isStrictNamespace() && !Namespace.SITEMAP.equals(uri)) {
            return;
        }
        if ("url".equals(localName) && "urlset".equals(currentElementParent())) {
            maybeAddSiteMapUrl();
        } else if ("urlset".equals(localName)) {
            sitemap.setProcessed(true);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        String localName = super.currentElement();
        String value = String.valueOf(ch, start, length);
        if ("loc".equals(localName) || "url".equals(localName)) {
            loc.append(value);
        } else if ("changefreq".equals(localName)) {
            changeFreq = value;
        } else if ("lastmod".equals(localName)) {
            lastMod = value;
        } else if ("priority".equals(localName)) {
            priority = value;
        }
    }

    public AbstractSiteMap getSiteMap() {
        return sitemap;
    }

    private void maybeAddSiteMapUrl() {
        String value = loc.toString().trim();
        try {
            // check that the value is a valid URL
            URL locURL = new URL(value);
            boolean valid = urlIsValid(sitemap.getBaseUrl(), value);
            if (valid || !isStrict()) {
                SiteMapURL sUrl = new SiteMapURL(locURL, valid);
                sUrl.setLastModified(lastMod);
                sUrl.setChangeFrequency(changeFreq);
                sUrl.setPriority(priority);
                sitemap.addSiteMapUrl(sUrl);
                LOG.debug("  {}. {}", (++i), sUrl);
            }
        } catch (MalformedURLException e) {
            LOG.debug("Bad url: [{}]", value);
            LOG.trace("Can't create an entry with a bad URL", e);
        } finally {
            loc = new StringBuilder();
            lastMod = null;
            changeFreq = null;
            priority = null;
        }
    }

    public void error(SAXParseException e) throws SAXException {
        maybeAddSiteMapUrl();
    }

    public void fatalError(SAXParseException e) throws SAXException {
        maybeAddSiteMapUrl();
    }
}
