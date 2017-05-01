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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Provides helpers for parsing extensions to the standard sitemap protocol.
 * At the current time, only XML sitemaps are supported.
 * Moreover, only Google' video, images, links and news extensions are supported.
 */
public class SiteMapExtensionParser {
    private final static Logger LOGGER = LoggerFactory.getLogger("sitemaps.parser.extension");

    public static final String VIDEOS_NS = "http://www.google.com/schemas/sitemap-video/1.1";
    public static final String IMAGES_NS = "http://www.google.com/schemas/sitemap-image/1.1";
    public static final String LINKS_NS = "http://www.w3.org/1999/xhtml";
    public static final String NEWS_NS = "http://www.google.com/schemas/sitemap-news/0.9";

    public static final ImageAttributes[] parseImages(final Element element) {
        ImageAttributes[] images = null;

        NodeList imageNodes = element.getElementsByTagNameNS(IMAGES_NS, "image");

        if (imageNodes.getLength() > 0) {
            images = new ImageAttributes[imageNodes.getLength()];
            for (int i=0; i<imageNodes.getLength(); i++) {
                images[i] = parseImageNode(imageNodes.item(i));
            }
        }
        return images;
    }

    public static final LinkAttributes[] parseLinks(final Element element) {
        LinkAttributes[] links = null;

        NodeList linkNodes = element.getElementsByTagNameNS(LINKS_NS, "link");
        if (linkNodes.getLength() > 0) {
            links = new LinkAttributes[linkNodes.getLength()];
            for (int i=0; i<linkNodes.getLength(); i++) {
                links[i] = parseLinkNode(linkNodes.item(i));
            }
        }
        return links;
    }

    public static final VideoAttributes[] parseVideos(final Element element) {
        VideoAttributes[] videos = null;
        NodeList videoNodes = element.getElementsByTagNameNS(VIDEOS_NS, "video");
        if (videoNodes.getLength() > 0) {
            videos = new VideoAttributes[videoNodes.getLength()];
            for (int i=0; i<videoNodes.getLength(); i++) {
                videos[i] = parseVideoNode(videoNodes.item(i));
            }
        }
        return videos;
    }

    public static final NewsAttributes parseNews(final Element element) {
        NewsAttributes news = null;
        NodeList newsNodes = element.getElementsByTagNameNS(NEWS_NS, "news");
        if (newsNodes.getLength() > 0) {
            news = parseNewsNode(newsNodes.item(0));
        }
        return news;
    }

    private static NewsAttributes parseNewsNode(final Node node) {
        Element elem = (Element)node;

        NodeList publicationNodes = elem.getElementsByTagNameNS(NEWS_NS, "publication");
        String name = null;
        String language = null;

        if (publicationNodes.getLength() > 0) {
            Element publication = (Element)publicationNodes.item(0);
            name = getElementValue(publication, NEWS_NS, "name");
            language = getElementValue(publication, NEWS_NS, "language");
        }
        String genresStr = getElementValue(elem, NEWS_NS, "genres");
        NewsAttributes.NewsGenre[] genres = null;
        if (genresStr != null) {
            String[] genresList = StringUtils.split(genresStr, ",");
            List<NewsAttributes.NewsGenre> _genres = new ArrayList<>();
            for (String genre : genresList) {
                try {
                    _genres.add(NewsAttributes.NewsGenre.valueOf(genre.trim()));
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Unsupported genre: " + genre);
                }
            }
            genres = _genres.toArray(new NewsAttributes.NewsGenre[_genres.size()]);
        }
        Date publicationDate = getElementDateValue(elem, NEWS_NS, "publication_date");
        String title = getElementValue(elem, NEWS_NS, "title");
        String[] keywords = null;
        String keywordsStr = getElementValue(elem, NEWS_NS, "keywords");
        if (keywordsStr != null) {
            keywords = StringUtils.split(keywordsStr, ",");
            for (int i=0; i<keywords.length; i++) {
                keywords[i] = keywords[i].trim();
            }
        }
        String stockTickersStr = getElementValue(elem, NEWS_NS, "stock_tickers");
        String stockTickers[] = null;
        if (stockTickersStr != null) {
            stockTickers = StringUtils.split(stockTickersStr, ",", 5);
            for (int i = 0; i < stockTickers.length; i++) {
                stockTickers[i] = stockTickers[i].trim();
            }
            if (stockTickers.length == 5 && stockTickers[4].indexOf(",") != -1) {
                stockTickers[4] = stockTickers[4].substring(0, stockTickers[4].indexOf(","));
            }
        }
        NewsAttributes newsAttributes = new NewsAttributes(name, language, publicationDate, title);
        newsAttributes.setGenres(genres);
        newsAttributes.setKeywords(keywords);
        newsAttributes.setStockTickers(stockTickers);
        return newsAttributes;
    }

    private static LinkAttributes parseLinkNode(final Node node) {
        final Element elem = (Element)node;
        NamedNodeMap attributes = elem.getAttributes();
        URI href = null;
        Map<String, String> params = new HashMap<>(attributes.getLength()-1);
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                final Node attribute = attributes.item(i);
                final String name = attribute.getNodeName();
                final String value = attribute.getNodeValue();
                if ("href".equalsIgnoreCase(name)) {
                    if (value != null) {
                        try {
                            href = new URI(value);
                        } catch (URISyntaxException e) {
                            LOGGER.warn("Invalid URI in link href attribute: " + value);
                        }
                    } else {
                        LOGGER.warn("Unexpected null value for Link href attribute");
                    }
                } else {
                    if (name != null && value != null) {
                        params.put(name, value);
                    }
                }
            }
            LinkAttributes linkAttributes = new LinkAttributes(href);
            linkAttributes.setParams(params);
            return linkAttributes;
        } else {
            return null;
        }

    }

    private static final ImageAttributes parseImageNode(final Node node) {
        final Element elem = (Element) node;

        final URL loc = getElementURLValue(elem, IMAGES_NS, "loc");
        final String caption = getElementValue(elem, IMAGES_NS, "caption");
        final String geoLocation = getElementValue(elem, IMAGES_NS, "geo_location");
        final String title = getElementValue(elem, IMAGES_NS, "title");
        final URL license = getElementURLValue(elem, IMAGES_NS, "license");
        final ImageAttributes imageAttributes = new ImageAttributes(loc);
        imageAttributes.setCaption(caption);
        imageAttributes.setGeoLocation(geoLocation);
        imageAttributes.setTitle(title);
        imageAttributes.setLicense(license);

        return imageAttributes;
    }


    private static final VideoAttributes parseVideoNode(final Node node) {
        final Element elem = (Element) node;
        URL thumbnailLoc = getElementURLValue(elem, VIDEOS_NS, "thumbnail_loc");
        String title = getElementValue(elem, VIDEOS_NS, "title");
        String description = getElementValue(elem, VIDEOS_NS, "description");
        URL contentLoc = getElementURLValue(elem, VIDEOS_NS, "content_loc");
        URL playerLoc = getElementURLValue(elem, VIDEOS_NS, "player_loc");
        Integer duration = getElementIntegerValue(elem, VIDEOS_NS, "duration");
        if (duration != null &&  (duration < 0 || duration > 28800)) {
            LOGGER.warn("Invalid value for specified duration: " + duration);
            duration = null;
        }
        Date expirationDate = getElementDateValue(elem, VIDEOS_NS, "expiration_date");
        Float rating = getElementFloatValue(elem, VIDEOS_NS, "rating");
        Integer viewCount = getElementIntegerValue(elem, VIDEOS_NS, "view_count");
        Date publicationDate = getElementDateValue(elem, VIDEOS_NS, "publication_date");
        String familyFriendlyStr = getElementValue(elem, VIDEOS_NS, "family_friendly");
        Boolean familyFriendly = null;
        if (familyFriendlyStr != null) {
            familyFriendly = !"No".equalsIgnoreCase(familyFriendlyStr);
        }
        String[] tags = getElementValues(elem, VIDEOS_NS, "tag");
        String category = getElementValue(elem, VIDEOS_NS, "category");
        String restrictionStr = getElementValue(elem, VIDEOS_NS, "restriction");
        String[] restrictions = restrictionStr != null ? StringUtils.split(restrictionStr, " ") : null;
        String restrictionRelationship = getElementAttributeValue(elem, VIDEOS_NS, "restriction", "relationship");
        String[] restrictedCountries = null;
        String[] allowedCountries = null;
        if (restrictionRelationship != null) {
            if ("allow".equalsIgnoreCase(restrictionRelationship)) {
                allowedCountries = restrictions;
            } else if ("deny".equalsIgnoreCase(restrictionRelationship)) {
                restrictedCountries = restrictions;
            }
        }
        URL galleryLoc = getElementURLValue(elem, VIDEOS_NS, "gallery_loc");
        String galleryTitle = getElementAttributeValue(elem, VIDEOS_NS, "gallery_loc", "title");
        NodeList priceNodes = elem.getElementsByTagNameNS(VIDEOS_NS, "price");
        VideoAttributes.VideoPrice[] prices = parseVideoPriceNodes(priceNodes);
        Boolean requiresSubscription = null;
        String requiresSubscriptionStr = getElementValue(elem, VIDEOS_NS, "requires_subscription");
        if (requiresSubscriptionStr != null) {
            if ("no".equalsIgnoreCase(requiresSubscriptionStr)) {
                requiresSubscription = false;
            } else if ("yes".equalsIgnoreCase(requiresSubscriptionStr)) {
                requiresSubscription = true;
            } else {
                LOGGER.warn("Unexpected value for requires_subscription node: " + requiresSubscriptionStr);
            }

        }
        String uploader = getElementValue(elem, VIDEOS_NS, "uploader");
        URL uploaderInfo = getElementURLAttributeValue(elem, VIDEOS_NS, "uploader", "info");

        String platformStr = getElementValue(elem, VIDEOS_NS, "platform");
        String[] platforms = platformStr != null ? StringUtils.split(platformStr.toLowerCase(Locale.ENGLISH), " ") : null;
        // to do filter platforms to fit in web, mobile, tv or use explicit enum.
        String platformRelationship = getElementAttributeValue(elem, VIDEOS_NS, "platform", "relationship");
        String[] restrictedPlatforms = null;
        String[] allowedPlatforms = null;
        if (platformRelationship != null) {
            if ("allow".equalsIgnoreCase(platformRelationship)) {
                allowedPlatforms = platforms;
            } else if ("deny".equalsIgnoreCase(platformRelationship)) {
                restrictedPlatforms = platforms;
            } else {
                LOGGER.warn("Unexpected relationship attribute value for platform node: " + platformRelationship);
            }
        }

        Boolean isLive = null;
        String isLiveStr = getElementValue(elem, VIDEOS_NS, "live");
        if (isLiveStr != null) {
            if ("yes".equalsIgnoreCase(isLiveStr)) {
                isLive = true;
            } else if ("no".equalsIgnoreCase(isLiveStr)) {
                isLive = false;
            } else {
                LOGGER.warn("Unexpected value for live node: " + isLiveStr);
            }
        }

        VideoAttributes attributes = new VideoAttributes(thumbnailLoc, title, description, contentLoc, playerLoc);
        attributes.setDuration(duration);
        attributes.setExpirationDate(expirationDate);
        attributes.setRating(rating);
        attributes.setViewCount(viewCount);
        attributes.setPublicationDate(publicationDate);
        attributes.setFamilyFriendly(familyFriendly);
        attributes.setTags(tags);
        attributes.setCategory(category);
        attributes.setRestrictedCountries(restrictedCountries);
        attributes.setAllowedCountries(allowedCountries);
        attributes.setGalleryLoc(galleryLoc);
        attributes.setGalleryTitle(galleryTitle);
        attributes.setPrices(prices);
        attributes.setRequiresSubscription(requiresSubscription);
        attributes.setUploader(uploader);
        attributes.setUploaderInfo(uploaderInfo);
        attributes.setRestrictedPlatforms(restrictedPlatforms);
        attributes.setAllowedPlatforms(allowedPlatforms);
        attributes.setLive(isLive);

        return attributes;
    }


    private static VideoAttributes.VideoPrice[] parseVideoPriceNodes(NodeList nodes) {
        if (nodes.getLength() > 0) {
            VideoAttributes.VideoPrice[] prices = new VideoAttributes.VideoPrice[nodes.getLength()];
            for (int i=0; i<nodes.getLength(); i++) {
                Element priceElem = (Element)nodes.item(i);
                String price = priceElem.getTextContent().trim();
                float _price = Float.NaN;
                if (price != null) {
                    try {
                        _price = Float.parseFloat(price);
                    } catch (NumberFormatException e) {
                        LOGGER.warn("Invalid float value for price: " + price);
                    }
                }
                String currency = priceElem.getAttribute("currency");
                String type = priceElem.getAttribute("type");
                VideoAttributes.VideoPriceType _type = null;
                try {
                    _type = type.isEmpty() ? VideoAttributes.VideoPriceType.own :
                        VideoAttributes.VideoPriceType.valueOf(type);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Illegal value for price type: " + type);
                    _type = VideoAttributes.VideoPriceType.own;
                }
                String resolution = priceElem.getAttribute("resolution");
                VideoAttributes.VideoPriceResolution _resolution = null;
                try{
                    _resolution = resolution.isEmpty() ? null:
                        VideoAttributes.VideoPriceResolution.valueOf(resolution);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Illegal value for price resolution: " + resolution);
                }
                prices[i] = new VideoAttributes.VideoPrice(currency, _price, _type, _resolution);
            }
            return prices;
        }
        return null;
    }

    private static String[] getElementValues(Element elem, String namespaceURI, String elementName) {
        String[] values = null;
        final NodeList nodes = elem.getElementsByTagNameNS(namespaceURI, elementName);
        if (nodes.getLength() > 0) {
            values = new String[nodes.getLength()];
            for (int i=0; i<nodes.getLength(); i++) {
                values[i] = nodes.item(i).getTextContent().trim();
            }
        }
        return values;
    }

    private static Float getElementFloatValue(Element elem, String namespaceURI, String elementName) {
        final String value = getElementValue(elem, namespaceURI, elementName);
        if (value != null) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid float value: " + value);
            }
        }
        return null;
    }

    private static Date getElementDateValue(Element elem, String namespaceURI, String elementName) {
        return SiteMap.convertToDate(getElementValue(elem, namespaceURI, elementName));
    }

    private static Integer getElementIntegerValue(Element elem, String namespaceURI, String elementName) {
        final String value = getElementValue(elem, namespaceURI, elementName);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid integer value: " + value);
            }
        }
        return null;
    }

    private static URL getElementURLAttributeValue(Element elem, String namespaceURI, String elementName, String attribute) {
        final String value = getElementAttributeValue(elem, namespaceURI, elementName, attribute);
        if (value != null) {
            try {
                return new URL(value);
            } catch (MalformedURLException e) {
                LOGGER.warn("Invalid URL: value");
            }
        }
        return null;
    }

    private static URL getElementURLValue(Element elem, String namespaceURI, String elementName) {
        final String value = getElementValue(elem, namespaceURI, elementName);
        if (value != null) {
            try {
                return new URL(value);
            } catch (MalformedURLException e) {
                LOGGER.warn("Invalid URL value: " + value);
            }
        }
        return null;
    }

    private static String getElementValue(Element elem, String namespaceURI, String elementName) {
        NodeList list = elem.getElementsByTagNameNS(namespaceURI, elementName);
        if (list == null)
            return null;
        Element e = (Element) list.item(0);
        if (e != null) {
            return e.getTextContent().trim();
        }
        return null;
    }


    private static String getElementAttributeValue(Element elem, String namespaceURI, String elementName, String attributeName) {

        NodeList list = elem.getElementsByTagNameNS(namespaceURI, elementName);
        Element e = (Element) list.item(0);
        if (e != null) {
            return e.getAttribute(attributeName).trim();
        }

        return null;
    }

}
