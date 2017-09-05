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

import java.net.URL;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.Namespace;
import crawlercommons.sitemaps.UnknownFormatException;

/**
 * Provides a base SAX handler for parsing of XML documents representing
 * sub-classes of AbstractSiteMap.
 */
public class DelegatorHandler extends DefaultHandler {

    private LinkedList<String> elementStack;
    private DelegatorHandler delegate;
    private URL url;
    private boolean strict;
    private UnknownFormatException exception;
    

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

    protected void setException(UnknownFormatException exception) {
        this.exception = exception;
    }

    protected UnknownFormatException getException() {
        return exception;
    }

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
        
        if ( Namespace.SITEMAP.equals(uri) ) {
	        if ("sitemapindex".equals(localName)) {
	            delegate = new XMLIndexHandler(url, elementStack, strict);
	        } else if ("urlset".equals(localName)) {
	            delegate = new XMLHandler(url, elementStack, strict);
	        }
        } else if ("feed".equals(localName)) {
            delegate = new AtomHandler(url, elementStack, strict);
        }
        // See if it is a RSS feed by looking for the localName "channel" element . 
        // This avoids the issue
        // of having the outer tag named <rdf:RDF> that was causing this code to
        // fail. Inside of
        // the <rss> or <rdf> tag is a <channel> tag, so we can use that.
        // See https://github.com/crawler-commons/crawler-commons/issues/87
        // and also RSS 1.0 specification http://web.resource.org/rss/1.0/spec
        else if ("channel".equals(localName)) {
            delegate = new RSSHandler(url, elementStack, strict);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (delegate != null) {
            delegate.endElement(uri, localName, qName);
        }
        elementStack.pop();
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if (delegate != null) {
            delegate.characters(ch, start, length);
        }
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

    public void error(SAXParseException e) throws SAXException {
        if (delegate != null) {
            delegate.error(e);
        }
    }

    public void fatalError(SAXParseException e) throws SAXException {
        if (delegate != null) {
            delegate.fatalError(e);
        }
    }
}
