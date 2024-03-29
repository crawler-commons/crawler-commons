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

package crawlercommons.sitemaps.extension;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data model for Google extension to the sitemap protocol regarding images
 * indexing, as per http://www.google.com/schemas/sitemap-image/1.1
 */
@SuppressWarnings("serial")
public class ImageAttributes extends ExtensionMetadata {

    public static final String LOC = "loc";
    public static final String CAPTION = "caption";
    public static final String GEO_LOCATION = "geo_location";
    public static final String TITLE = "title";
    public static final String LICENSE = "license";

    /**
     * Image location attribute found under image/loc (required)
     */
    private URL loc;

    /**
     * Image caption attribute found under image/caption (optional)
     */
    private String caption;

    /**
     * Image geo location attribute found under image/geo_location (optional)
     */
    private String geoLocation;

    /**
     * Image title attribute found under image/title (optional)
     */
    private String title;

    /**
     * Image license attribute found under image/license (optional)
     */
    private URL license;

    public URL getLoc() {
        return loc;
    }

    public void setLoc(URL loc) {
        this.loc = loc;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public URL getLicense() {
        return license;
    }

    public void setLicense(URL license) {
        this.license = license;
    }

    public ImageAttributes() {
    }

    public ImageAttributes(URL loc) {
        this.loc = loc;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Image loc: ").append(loc);
        sb.append(", caption: ").append(caption);
        sb.append(", title: ").append(title);
        sb.append(", geoLocation: ").append(geoLocation);
        sb.append(", license: ").append(license);
        return sb.toString();
    }

    @Override
    public boolean isValid() {
        return loc != null;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof ImageAttributes)) {
            return false;
        }
        ImageAttributes that = (ImageAttributes) other;
        return urlEquals(loc, that.loc) //
                        && Objects.equals(caption, that.caption) //
                        && Objects.equals(geoLocation, that.geoLocation) //
                        && Objects.equals(title, that.title) //
                        && urlEquals(license, that.license);
    }

    @Override
    public Map<String, String[]> asMap() {
        Map<String, String[]> map = new HashMap<>();

        if (loc != null) {
            map.put(LOC, new String[]{ loc.toString() });
        }

        if (caption != null) {
            map.put(CAPTION, new String[]{ caption });
        }

        if (geoLocation != null) {
            map.put(GEO_LOCATION, new String[]{ geoLocation });
        }

        if (title != null) {
            map.put(TITLE, new String[]{ title });
        }

        if (license != null) {
            map.put(LICENSE, new String[]{ license.toString() });
        }
        return Collections.unmodifiableMap(map);
    }
}