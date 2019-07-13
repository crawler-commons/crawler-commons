/**
 * Copyright 2018 Crawler-Commons
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

package crawlercommons.sitemaps.sax.extension;

import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import crawlercommons.sitemaps.extension.LinkAttributes;

/** Handle SAX events in the Google Image sitemap extension namespace. */
public class LinksHandler extends ExtensionHandler {

    public LinksHandler() {
        reset();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("link".equals(localName)) {
            String href = attributes.getValue("href");
            if (href != null && !href.trim().isEmpty()) {
                URL url = getURLValue(href.trim());
                if (url != null) {
                    LinkAttributes attr = new LinkAttributes(url);
                    this.attributes.add(attr);
                    Map<String, String> params = new TreeMap<>();
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String k = attributes.getLocalName(i);
                        if (!k.equals("href")) {
                            params.put(k, attributes.getValue(i).trim());
                        }
                    }
                    attr.setParams(params);
                }
            }
        }
    }
}
