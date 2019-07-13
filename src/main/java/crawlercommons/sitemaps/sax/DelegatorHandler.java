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
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.Namespace;
import crawlercommons.sitemaps.UnknownFormatException;
import crawlercommons.sitemaps.extension.Extension;

/**
 * Provides a base SAX handler for parsing of XML documents representing
 * sub-classes of AbstractSiteMap.
 */
public class DelegatorHandler extends DefaultHandler {

    private LinkedList<String> elementStack;
    private DelegatorHandler delegate;
    private URL url;
    private boolean strict;
    private boolean strictNamespace;
    private UnknownFormatException exception;
    private Set<String> acceptedNamespaces;
    protected Map<String, Extension> extensionNamespaces;
    private StringBuilder characterBuffer = new StringBuilder();

    protected DelegatorHandler(LinkedList<String> elementStack, boolean strict) {
        this.elementStack = elementStack;
        this.strict = strict;
    }

    public DelegatorHandler(URL url, boolean strict) {
        this.elementStack = new LinkedList<String>();
        this.url = url;
        this.strict = strict;
    }

    protected URL getUrl() {
        return url;
    }

    protected boolean isStrict() {
        return strict;
    }

    protected boolean isStrictNamespace() {
        return strictNamespace;
    }

    public void setStrictNamespace(boolean s) {
        strictNamespace = s;
    }

    public void setAcceptedNamespaces(Set<String> acceptedSet) {
        acceptedNamespaces = acceptedSet;
    }

    protected boolean isAcceptedNamespace(String uri) {
        return acceptedNamespaces.contains(uri);
    }

    public void setExtensionNamespaces(Map<String, Extension> extensionMap) {
        extensionNamespaces = extensionMap;
    }

    protected boolean isExtensionNamespace(String uri) {
        if (extensionNamespaces == null) {
            return false;
        }
        return extensionNamespaces.containsKey(uri);
    }

    protected void setException(UnknownFormatException exception) {
        this.exception = exception;
    }

    public UnknownFormatException getException() {
        return exception;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (elementStack.isEmpty() || delegate == null) {
            startRootElement(uri, localName, qName, attributes);
        } else {
            elementStack.push(localName);
        }
        if (delegate != null) {
            delegate.startElement(uri, localName, qName, attributes);
        }
    }

    private void startRootElement(String uri, String localName, String qName, Attributes attributes) {
        elementStack.push(localName);

        if ("feed".equals(localName)) {
            delegate = new AtomHandler(url, elementStack, strict);
        }
        // See if it is a RSS feed by looking for the localName "channel"
        // element. This avoids the issue of having the outer tag named
        // <rdf:RDF> that was causing this code to fail. Inside of
        // the <rss> or <rdf> tag is a <channel> tag, so we can use that.
        // See https://github.com/crawler-commons/crawler-commons/issues/87
        // and also RSS 1.0 specification http://web.resource.org/rss/1.0/spec
        else if ("channel".equals(localName)) {
            delegate = new RSSHandler(url, elementStack, strict);
        } else if ("sitemapindex".equals(localName)) {
            delegate = new XMLIndexHandler(url, elementStack, strict);
        } else if ("urlset".equals(localName)) {
            delegate = new XMLHandler(url, elementStack, strict);
        } else {
            LOG.debug("Skipped unknown root element <{}> in {}", localName, url);
            return;
        }
        // configure delegate
        delegate.setStrictNamespace(isStrictNamespace());
        delegate.setAcceptedNamespaces(acceptedNamespaces);
        // validate XML namespace
        if (isStrictNamespace()) {
            if (delegate instanceof AtomHandler || delegate instanceof RSSHandler) {
                // no namespace checking for feeds
                return;
            }
            if (!isAcceptedNamespace(uri) && uri.startsWith("/")) {
                // first, try to resolve relative namespace URI (deprecated but
                // not forbidden), e.g., //www.sitemaps.org/schemas/sitemap/0.9
                try {
                    URL u = new URL(url, uri);
                    uri = u.toString();
                } catch (MalformedURLException e) {
                    LOG.warn("Failed to resolve relative namespace URI {} in sitemap {}", uri, url);
                }
            }
            if (!isAcceptedNamespace(uri)) {
                String msg;
                if (!Namespace.isSupported(uri)) {
                    msg = "Unsupported namespace <" + uri + ">";
                } else {
                    msg = "Namespace <" + uri + "> not accepted";
                }
                setException(new UnknownFormatException(msg));
                delegate = null;
                return;
            }
        }
        delegate.setExtensionNamespaces(extensionNamespaces);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (delegate != null) {
            delegate.endElement(uri, localName, qName);
        }
        elementStack.pop();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        if (delegate != null) {
            delegate.characters(ch, start, length);
        }
    }

    protected void appendCharacterBuffer(char ch[], int start, int length) {
        for (int i = start; i < start + length; i++) {
            characterBuffer.append(ch[i]);
        }
    }

    protected void appendCharacterBuffer(String str) {
        characterBuffer.append(str);
    }

    protected String getAndResetCharacterBuffer() {
        String value = characterBuffer.toString();
        resetCharacterBuffer();
        return value;
    }

    protected void resetCharacterBuffer() {
        characterBuffer = new StringBuilder();
    }

    protected String currentElement() {
        return elementStack.peek();
    }

    protected String currentElementParent() {
        return (elementStack.size() < 2) ? null : elementStack.get(1);
    }

    public AbstractSiteMap getSiteMap() {
        if (delegate == null)
            return null;
        return delegate.getSiteMap();
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        if (delegate != null) {
            delegate.error(e);
        }
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        if (delegate != null) {
            delegate.fatalError(e);
        }
    }

    /**
     * Return true if character sequence contains only white space including
     * Unicode whitespace, cf. {@link #isWhitespace(char)}
     */
    public static boolean isAllBlank(CharSequence charSeq) {
        for (int i = 0; i < charSeq.length(); i++) {
            if (!isWhitespace(charSeq.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether character is any Unicode whitespace, including the space
     * characters not covered by {@link Character#isWhitespace(char)}
     */
    public static boolean isWhitespace(char c) {
        return Character.isWhitespace(c) || c == '\u00a0' || c == '\u2007' || c == '\u202f';
    }

    /** Trim all whitespace including Unicode whitespace */
    public static String stripAllBlank(CharSequence charSeq) {
        if (charSeq.length() == 0) {
            return charSeq.toString();
        }
        int start = 0;
        int end = charSeq.length() - 1;
        while (isWhitespace(charSeq.charAt(start)) && start < end) {
            start++;
        }
        if (start < end) {
            while (isWhitespace(charSeq.charAt(end))) {
                end--;
            }
        }
        return charSeq.subSequence(start, end + 1).toString();
    }
}
