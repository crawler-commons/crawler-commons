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

import crawlercommons.sitemaps.SiteMapURL;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;

/**
 * Container for attributes of a {@link SiteMapURL} defined by a sitemap
 * extension.
 */
@SuppressWarnings("serial")
public abstract class ExtensionMetadata implements Serializable {

    public abstract boolean equals(Object other);

    public abstract Map<String, String[]> asMap();

    /**
     * Validate extension metadata.
     * 
     * Extensions should override this method in order to validate the
     * extension-specific set of metadata attributes.
     * 
     * @return true if all required extension attributes are present and
     *         attributes are valid.
     */
    public boolean isValid() {
        return true;
    }

    /**
     * Compare URLs by their string representation because calling
     * {@link URL#equals(Object)} may trigger an unwanted and potentially slow
     * DNS lookup to resolve the host part
     */
    protected static boolean urlEquals(URL a, URL b) {
        return (a == b) || (a != null && b != null && a.toString().equals(b.toString()));
    }

    /**
     * Look up an enum constant by name, ignoring case and surrounding white
     * space. Sitemap extension values are written in different cases in the
     * wild, for example "subscription" for Subscription or "hd" for HD.
     * 
     * @param <E>
     *            enum type
     * @param enumType
     *            class of the enum
     * @param name
     *            value found in the sitemap
     * @return the matching enum constant
     * @throws IllegalArgumentException
     *             if no constant matches the value
     */
    public static <E extends Enum<E>> E valueOfIgnoreCase(Class<E> enumType, String name) {
        if (name != null) {
            String trimmed = name.trim();
            for (E constant : enumType.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(trimmed)) {
                    return constant;
                }
            }
        }
        throw new IllegalArgumentException("No enum constant " + enumType.getCanonicalName() + "." + name);
    }

}
