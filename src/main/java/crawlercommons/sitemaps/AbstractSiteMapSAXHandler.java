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

import java.net.URL;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Provides a base SAX handler for parsing of XML documents representing
 * sub-classes of AbstractSiteMap.
 */
public class AbstractSiteMapSAXHandler extends DefaultHandler {

    private LinkedList<String> elementStack;
    private AbstractSiteMapSAXHandler delegate;
    private URL url;
    private boolean strict;
    private UnknownFormatException exception;

    protected AbstractSiteMapSAXHandler(LinkedList<String> elementStack, boolean strict) {
        this.elementStack = elementStack;
        this.strict = strict;
    }

    public AbstractSiteMapSAXHandler(URL url, boolean strict) {
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
            elementStack.push(qName);
        }
    }

    private void startRootElement(String uri, String localName, String qName, Attributes attributes) {
        elementStack.push(qName);
        if ("sitemapindex".equals(qName)) {
            delegate = new SiteMapIndexSAXHandler(url, elementStack, strict);
        } else if ("urlset".equals(qName)) {
            delegate = new SiteMapSAXHandler(url, elementStack, strict);
        } else if ("feed".equals(qName)) {
            delegate = new SiteMapAtomSAXHandler(url, elementStack, strict);
        }
        // See if it is a RSS feed by looking for a "channel" element. This
        // avoids the issue
        // of having the outer tag named <rdf:RDF> that was causing this code to
        // fail. Inside of
        // the <rss> or <rdf> tag is a <channel> tag, so we can use that.
        // See https://github.com/crawler-commons/crawler-commons/issues/87
        // and also RSS 1.0 specification http://web.resource.org/rss/1.0/spec
        else if ("channel".equals(qName)) {
            delegate = new SiteMapRSSSAXHandler(url, elementStack, strict);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (delegate != null)
            delegate.endElement(uri, localName, qName);
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
