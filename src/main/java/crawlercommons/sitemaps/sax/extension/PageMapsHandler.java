/**
 * Copyright 2023 Crawler-Commons
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

import crawlercommons.sitemaps.extension.PageMap;
import crawlercommons.sitemaps.extension.PageMapDataObject;

/**
 * Handle SAX events in the Google's Programmable Search Engine <a href=
 * "https://support.google.com/programmable-search/answer/1628213">PageMaps</a>
 * extension namespace.
 */
public class PageMapsHandler extends ExtensionHandler {

    private PageMap currPageMap;
    private PageMapDataObject currDataObj;
    private String currAttrName;
    private StringBuilder currAttrVal = new StringBuilder();
    private String currAttrValFromAttr;

    public PageMapsHandler() {
        reset();
    }

    @Override
    public void reset() {
        super.reset();
        resetCurrent();
    }

    private void resetCurrent() {
        currDataObj = null;
        currAttrName = null;
        currAttrVal.setLength(0);
        currAttrValFromAttr = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (localName) {
            case "PageMap":
            currPageMap = new PageMap();
                break;
            case "DataObject":
            currDataObj = new PageMapDataObject(attributes.getValue("type"), attributes.getValue("id"));
            if (currPageMap == null) {
                // ignore lonesome DataObject elements
            } else {
                currPageMap.addDataObject(currDataObj);
            }
                break;
            case "Attribute":
            currAttrVal.setLength(0);
            currAttrName = attributes.getValue("name");
            if (attributes.getValue("value") != null) {
                /*
                 * The PageMaps specification
                 * (https://support.google.com/programmable-search/answer/
                 * 1628213) describes for PageMaps embedded in HTML that the
                 * attribute value is given as element attribute named "value".
                 * For sitemaps it should be given as character data. However,
                 * some PageMaps sitemaps in the wild also use the HTML
                 * mechanism. We fall back to the HTML mechanism if there is no
                 * or white space only character data.
                 */
                currAttrValFromAttr = attributes.getValue("value");
            }
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (localName) {
            case "PageMap":
            attributes.add(currPageMap);
                break;
            case "DataObject":
                break;
            case "Attribute":
            String currAttrValStr = currAttrVal.toString().trim();
            if (currDataObj == null) {
                // ignore lonesome attributes
            } else if (currAttrValStr.isEmpty() && currAttrValFromAttr != null) {
                /*
                 * If there is no or white space only character data, fall back
                 * to the HTML mechanism and use the content of the attribute
                 * "value".
                 */
                currDataObj.addAttribute(currAttrName, currAttrValFromAttr);
            } else {
                currDataObj.addAttribute(currAttrName, currAttrValStr);
            }
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currAttrVal.append(String.valueOf(ch, start, length));
    }

}
