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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.AbstractSiteMap.SitemapType;
import crawlercommons.sitemaps.SiteMap;
import crawlercommons.sitemaps.SiteMapURL;
import crawlercommons.sitemaps.extension.Extension;
import crawlercommons.sitemaps.sax.extension.ExtensionHandler;

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
    private String loc;
    private String lastMod;
    private String changeFreq;
    private String priority;
    private int i = 0;
    private boolean currentElementNamespaceIsValid;
    private String currentElementNamespace;
    protected Map<Extension, ExtensionHandler> extensionHandlers;

    XMLHandler(URL url, LinkedList<String> elementStack, boolean strict) {
        super(elementStack, strict);
        sitemap = new SiteMap(url);
        sitemap.setType(SitemapType.XML);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElementNamespace = uri;
        if (isExtensionNamespace(uri)) {
            ExtensionHandler eh = getExtensionHandler(uri);
            eh.startElement(uri, localName, qName, attributes);
            return;
        } else if (isStrictNamespace() && !isAcceptedNamespace(uri)) {
            LOG.debug("Skip element <{}>, namespace <{}> not accepted", localName, uri);
            currentElementNamespaceIsValid = false;
            return;
        }
        currentElementNamespaceIsValid = true;

        // flush any unclosed or missing URL element
        if ("loc".equals(localName) || "url".equals(localName)) {
            if (loc == null) {
                loc = getAndResetCharacterBuffer();
            }
            if (loc != null && !isAllBlank(loc)) {
                maybeAddSiteMapUrl();
                return;
            }
            loc = null;
            if ("url".equals(localName)) {
                // reset also attributes
                lastMod = null;
                changeFreq = null;
                priority = null;
            }
        }
        resetCharacterBuffer();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isExtensionNamespace(uri)) {
            ExtensionHandler eh = getExtensionHandler(uri);
            eh.endElement(uri, localName, qName);
            return;
        } else if (isStrictNamespace() && !isAcceptedNamespace(uri)) {
            return;
        }
        if ("url".equals(localName)) {
            if ("urlset".equals(currentElementParent())) {
                maybeAddSiteMapUrl();
            }
        } else if ("urlset".equals(localName)) {
            sitemap.setProcessed(true);
        } else if ("loc".equals(localName)) {
            loc = getAndResetCharacterBuffer();
        } else if ("changefreq".equals(localName)) {
            changeFreq = getAndResetCharacterBuffer();
        } else if ("lastmod".equals(localName)) {
            lastMod = getAndResetCharacterBuffer();
        } else if ("priority".equals(localName)) {
            priority = getAndResetCharacterBuffer();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (isExtensionNamespace(currentElementNamespace)) {
            ExtensionHandler eh = getExtensionHandler(currentElementNamespace);
            eh.characters(ch, start, length);
            return;
        } else if (isStrictNamespace() && !currentElementNamespaceIsValid) {
            return;
        }
        String localName = currentElement();
        if ("loc".equals(localName) || "url".equals(localName) || "changefreq".equals(localName) || "lastmod".equals(localName) || "priority".equals(localName)) {
            appendCharacterBuffer(ch, start, length);
        }
    }

    @Override
    public AbstractSiteMap getSiteMap() {
        return sitemap;
    }

    private void maybeAddSiteMapUrl() {
        String value = null;
        if (loc != null) {
            value = stripAllBlank(loc);
        } else if ("loc".equals(currentElement())) {
            value = getAndResetCharacterBuffer();
        }
        if (value == null || isAllBlank(value)) {
            return;
        }
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
                if (extensionHandlers != null) {
                    for (Entry<Extension, ExtensionHandler> e : extensionHandlers.entrySet()) {
                        sUrl.addAttributesForExtension(e.getKey(), e.getValue().getAttributes());
                    }
                }
            }
        } catch (MalformedURLException e) {
            LOG.debug("Bad url: [{}]", value);
            LOG.trace("Can't create an entry with a bad URL", e);
        } finally {
            loc = null;
            lastMod = null;
            changeFreq = null;
            priority = null;
            resetExtensionHandlers();
        }
    }

    /**
     * Registers and returns an ExtensionHandler instance bound to this handler
     * 
     * @param uri
     *            URI of sitemap extension namespace
     * @return handler for the sitemap extension defined by XML namespace
     */
    protected ExtensionHandler getExtensionHandler(String uri) {
        if (extensionNamespaces.containsKey(uri)) {
            Extension ext = extensionNamespaces.get(uri);
            if (extensionHandlers == null) {
                extensionHandlers = new TreeMap<>();
            }
            if (!extensionHandlers.containsKey(ext)) {
                extensionHandlers.put(ext, ExtensionHandler.create(ext));
            }
            return extensionHandlers.get(ext);
        }
        return null;
    }

    protected Collection<ExtensionHandler> getExtensionHandlers() {
        if (extensionHandlers == null) {
            return new ArrayList<ExtensionHandler>();
        }
        return extensionHandlers.values();
    }

    /**
     * Reset all extension handlers. Attributes of sitemap extensions are bound
     * to a single {@link SiteMapURL}, handlers should be reset if a sitemap URL
     * is closed.
     */
    public void resetExtensionHandlers() {
        if (extensionHandlers != null) {
            for (Entry<Extension, ExtensionHandler> e : extensionHandlers.entrySet()) {
                e.getValue().reset();
            }
        }
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
