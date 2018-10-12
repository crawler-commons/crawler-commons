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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import crawlercommons.sitemaps.extension.ExtensionMetadata;
import crawlercommons.sitemaps.extension.ImageAttributes;

/** Handle SAX events in the Google Image sitemap extension namespace. */
public class ImageHandler extends ExtensionHandler {

    private ImageAttributes currAttr;
    private StringBuilder currVal;

    public ImageHandler() {
        reset();
    }

    @Override
    public void reset() {
        super.reset();
        resetCurrent();
    }

    private void resetCurrent() {
        currAttr = null;
        currVal = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("image".equals(localName)) {
            // add last attribute and reset in case of unclosed
            // elements
            if (currAttr != null && currAttr.isValid()) {
                this.attributes.add(currAttr);
            }
            resetCurrent();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String value = currVal.toString().trim();
        if ("image".equals(localName)) {
            if (currAttr != null && currAttr.isValid()) {
                attributes.add(currAttr);
            }
            resetCurrent();
            return;
        }
        if (currAttr == null) {
            currAttr = new ImageAttributes();
        }
        if (value.isEmpty()) {
            // skip value but reset StringBuilder
        } else if ("loc".equals(localName)) {
            currAttr.setLoc(getURLValue(value));
        } else if ("caption".equals(localName)) {
            currAttr.setCaption(value);
        } else if ("title".equals(localName)) {
            currAttr.setTitle(value);
        } else if ("geo_location".equals(localName)) {
            currAttr.setGeoLocation(value);
        } else if ("license".equals(localName)) {
            currAttr.setLicense(getURLValue(value));
        }
        // reset StringBuilder
        currVal = new StringBuilder();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currVal.append(String.valueOf(ch, start, length));
    }

    @Override
    public ExtensionMetadata[] getAttributes() {
        if (currAttr != null && currAttr.isValid()) {
            /*
             * add current element to attribute list, do not reset in case
             * getAttributes is called during parsing of a sitemap <url> element
             */
            attributes.add(currAttr);
        }
        return super.getAttributes();
    }

}
