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

import static crawlercommons.sitemaps.SiteMapParser.LOG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import crawlercommons.sitemaps.extension.ExtensionMetadata;
import crawlercommons.sitemaps.extension.NewsAttributes;

/** Handle SAX events in the Google News sitemap extension namespace. */
public class NewsHandler extends ExtensionHandler {

    private NewsAttributes currAttr;
    private StringBuilder currVal;

    public NewsHandler() {
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
        if ("news".equals(localName)) {
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
        if ("news".equals(localName)) {
            if (currAttr != null && currAttr.isValid()) {
                attributes.add(currAttr);
            }
            resetCurrent();
            return;
        }
        if (currAttr == null) {
            currAttr = new NewsAttributes();
        }
        if (value.isEmpty()) {
            // skip value but reset StringBuilder
        } else if ("name".equals(localName)) {
            currAttr.setName(value);
        } else if ("title".equals(localName)) {
            currAttr.setTitle(value);
        } else if ("language".equals(localName)) {
            currAttr.setLanguage(value);
        } else if ("publication_date".equals(localName)) {
            currAttr.setPublicationDate(getDateValue(value));
        } else if ("genres".equals(localName)) {
            String[] genresList = commaSeparated.split(value);
            List<NewsAttributes.NewsGenre> _genres = new ArrayList<>();
            for (String genre : genresList) {
                try {
                    _genres.add(NewsAttributes.NewsGenre.valueOf(genre.trim()));
                } catch (IllegalArgumentException e) {
                    LOG.debug("Unsupported news sitemap genre: {}", genre);
                }
            }
            currAttr.setGenres(_genres.toArray(new NewsAttributes.NewsGenre[_genres.size()]));
        } else if ("keywords".equals(localName)) {
            currAttr.setKeywords(commaSeparated.split(value));
        } else if ("stock_tickers".equals(localName)) {
            String[] stockTickers = commaSeparated.split(value);
            if (stockTickers.length > 5) {
                stockTickers = Arrays.copyOfRange(stockTickers, 0, 5);
            }
            currAttr.setStockTickers(stockTickers);
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
