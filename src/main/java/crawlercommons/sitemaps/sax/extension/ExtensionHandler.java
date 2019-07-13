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

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.xml.sax.helpers.DefaultHandler;

import crawlercommons.sitemaps.SiteMap;
import crawlercommons.sitemaps.SiteMapURL;
import crawlercommons.sitemaps.extension.Extension;
import crawlercommons.sitemaps.extension.ExtensionMetadata;

/**
 * Handler to be called for elements in the namespace of a sitemap extension.
 * Parses the extension elements and creates extension-specific attributes to be
 * assigned to a {@link SiteMapURL}.
 */
public abstract class ExtensionHandler extends DefaultHandler {

    protected static Pattern commaSeparated = Pattern.compile("\\s*,\\s*");

    protected List<ExtensionMetadata> attributes = new ArrayList<>();

    public static ExtensionHandler create(Extension extension) {
        switch (extension) {
            case NEWS:
                return new NewsHandler();
            case VIDEO:
                return new VideoHandler();
            case IMAGE:
                return new ImageHandler();
            case LINKS:
                return new LinksHandler();
            case MOBILE:
                return new MobileHandler();
            default:
                return null;
        }
    }

    public ExtensionMetadata[] getAttributes() {
        return attributes.toArray(new ExtensionMetadata[0]);
    }

    public void reset() {
        attributes.clear();
    }

    protected static ZonedDateTime getDateValue(String value) {
        return SiteMap.convertToZonedDateTime(value);
    }

    protected static URL getURLValue(final String value) {
        if (value != null) {
            try {
                return new URL(value);
            } catch (MalformedURLException e) {
                LOG.debug("Invalid URL value: {}", value);
            }
        }
        return null;
    }

    protected static Integer getIntegerValue(String value) {
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LOG.debug("Invalid integer value: {}", value);
            }
        }
        return null;
    }

    protected static Float getFloatValue(String value) {
        if (value != null) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                LOG.debug("Invalid float value: {}", value);
            }
        }
        return null;
    }

    protected static Boolean getYesNoBooleanValue(String value, String elemName) {
        if ("no".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        } else if ("yes".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        } else {
            LOG.debug("Unexpected value for {} node: {}", elemName, value);
        }
        return null;
    }

}
