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

import java.util.Map;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import crawlercommons.sitemaps.extension.ExtensionMetadata;
import crawlercommons.sitemaps.extension.VideoAttributes;
import crawlercommons.sitemaps.extension.VideoAttributes.VideoPrice;
import crawlercommons.sitemaps.extension.VideoAttributes.VideoPriceResolution;
import crawlercommons.sitemaps.extension.VideoAttributes.VideoPriceType;

/** Handle SAX events in the Google Video sitemap extension namespace. */
public class VideoHandler extends ExtensionHandler {

    private VideoAttributes currAttr;
    private StringBuilder currVal;
    private String relationAttr;
    private Map<String, String> priceAttr;
    private static String[] PRICE_ATTRIBUTES = { "currency", "type", "resolution" };

    public VideoHandler() {
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
        if ("video".equals(localName)) {
            // add last attribute and reset in case of unclosed
            // elements
            if (currAttr != null && currAttr.isValid()) {
                this.attributes.add(currAttr);
            }
            resetCurrent();
            return;
        }
        if (currAttr == null) {
            currAttr = new VideoAttributes();
        }
        if ("restriction".equals(localName) || "platform".equals(localName)) {
            relationAttr = attributes.getValue("relationship");
        } else if ("gallery_loc".equals(localName)) {
            currAttr.setGalleryTitle(attributes.getValue("title"));
        } else if ("uploader".equals(localName)) {
            currAttr.setUploaderInfo(getURLValue(attributes.getValue("info")));
        } else if ("price".equals(localName)) {
            priceAttr = new TreeMap<>();
            for (String a : PRICE_ATTRIBUTES) {
                String v = attributes.getValue(a);
                if (v != null) {
                    priceAttr.put(a, v);
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String value = currVal.toString().trim().replaceAll("\\s+", " ");
        if ("video".equals(localName)) {
            if (currAttr != null && currAttr.isValid()) {
                // add current attribute to attribute list
                attributes.add(currAttr);
            }
            resetCurrent();
            return;
        }
        if (currAttr == null) {
            currAttr = new VideoAttributes();
        }
        if ("price".equals(localName)) {
            Float fvalue = getFloatValue(value);
            String currency = null;
            VideoPriceType type = VideoPriceType.own;
            VideoPriceResolution resolution = null;
            if (priceAttr != null) {
                if (priceAttr.containsKey("currency")) {
                    currency = priceAttr.get("currency").trim();
                }
                String t = priceAttr.get("type");
                if (t != null && !t.trim().isEmpty()) {
                    try {
                        type = VideoPriceType.valueOf(t.trim());
                    } catch (IllegalArgumentException e) {
                        LOG.debug("Illegal value for price type: {}", type);
                    }
                }
                String r = priceAttr.get("resolution");
                if (r != null && !r.trim().isEmpty()) {
                    try {
                        resolution = VideoPriceResolution.valueOf(r.trim());
                    } catch (IllegalArgumentException e) {
                        LOG.debug("Illegal value for price resolution: {}", resolution);
                    }
                }
            }
            VideoPrice price = new VideoPrice(currency, fvalue, type, resolution);
            currAttr.addPrice(price);
            priceAttr = null;
        } else if (value.isEmpty()) {
            // skip value but reset StringBuilder
        } else if ("thumbnail_loc".equals(localName)) {
            currAttr.setThumbnailLoc(getURLValue(value));
        } else if ("title".equals(localName)) {
            currAttr.setTitle(value);
        } else if ("description".equals(localName)) {
            currAttr.setDescription(value);
        } else if ("content_loc".equals(localName)) {
            currAttr.setContentLoc(getURLValue(value));
        } else if ("player_loc".equals(localName)) {
            currAttr.setPlayerLoc(getURLValue(value));
        } else if ("duration".equals(localName)) {
            Integer duration = getIntegerValue(value);
            if (duration != null && (duration < 0 || duration > 28800)) {
                LOG.debug("Invalid value for specified duration: {}", duration);
                duration = null;
            }
            currAttr.setDuration(duration);
        } else if ("expiration_date".equals(localName)) {
            currAttr.setExpirationDate(getDateValue(value));
        } else if ("rating".equals(localName)) {
            currAttr.setRating(getFloatValue(value));
        } else if ("view_count".equals(localName)) {
            currAttr.setViewCount(getIntegerValue(value));
        } else if ("publication_date".equals(localName)) {
            currAttr.setPublicationDate(getDateValue(value));
        } else if ("family_friendly".equals(localName)) {
            currAttr.setFamilyFriendly(!"No".equalsIgnoreCase(value));
        } else if ("tag".equals(localName)) {
            currAttr.addTag(value);
        } else if ("category".equals(localName)) {
            currAttr.setCategory(value);
        } else if ("restriction".equals(localName)) {
            if (relationAttr != null) {
                String[] vals = value.split("\\s+");
                if ("allow".equalsIgnoreCase(relationAttr)) {
                    currAttr.setAllowedCountries(vals);
                } else if ("deny".equalsIgnoreCase(relationAttr)) {
                    currAttr.setRestrictedCountries(vals);
                }
            }
            relationAttr = null;
        } else if ("gallery_loc".equals(localName)) {
            currAttr.setGalleryLoc(getURLValue(value));
        } else if ("requires_subscription".equals(localName)) {
            currAttr.setRequiresSubscription(getYesNoBooleanValue(value, localName));
        } else if ("uploader".equals(localName)) {
            currAttr.setUploader(value);
        } else if ("platform".equals(localName)) {
            if (relationAttr != null) {
                String[] vals = value.split("\\s+");
                if ("allow".equalsIgnoreCase(relationAttr)) {
                    currAttr.setAllowedPlatforms(vals);
                } else if ("deny".equalsIgnoreCase(relationAttr)) {
                    currAttr.setRestrictedPlatforms(vals);
                }
            }
            relationAttr = null;
        } else if ("live".equals(localName)) {
            currAttr.setLive(getYesNoBooleanValue(value, localName));
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
