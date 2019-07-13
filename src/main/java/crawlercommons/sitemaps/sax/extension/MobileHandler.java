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
import crawlercommons.sitemaps.extension.MobileAttributes;

/** Handle SAX events in the Google Mobile sitemap extension namespace. */
public class MobileHandler extends ExtensionHandler {

    private static MobileAttributes[] noMobileAttributes = new MobileAttributes[0];
    private static MobileAttributes[] mobileAttributes = new MobileAttributes[1];
    static {
        mobileAttributes[0] = new MobileAttributes();
    }

    private boolean mobileElementFound = false;

    public MobileHandler() {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("mobile".equals(localName)) {
            mobileElementFound = true;
        }
    }

    @Override
    public ExtensionMetadata[] getAttributes() {
        if (mobileElementFound) {
            return mobileAttributes;
        }
        return noMobileAttributes;
    }

    @Override
    public void reset() {
        super.reset();
        mobileElementFound = false;
    }

}
