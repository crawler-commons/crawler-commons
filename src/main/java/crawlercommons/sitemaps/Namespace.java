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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import crawlercommons.sitemaps.extension.Extension;

/**
 * supported sitemap formats:
 * https://www.sitemaps.org/protocol.html#otherformats
 */
public class Namespace {

    public static final String SITEMAP = "http://www.sitemaps.org/schemas/sitemap/0.9";

    /**
     * Legacy schema URIs from prior sitemap protocol versions and frequent
     * variants.
     */
    public static final String[] SITEMAP_LEGACY = { //
                    "https://www.sitemaps.org/schemas/sitemap/0.9", //
                    "http://www.sitemaps.org/schemas/sitemap/0.9/", //
                    "https://www.sitemaps.org/schemas/sitemap/0.9/", //
                    "http://www.google.com/schemas/sitemap/0.9", //
                    "https://www.google.com/schemas/sitemap/0.9", //
                    "http://www.google.com/schemas/sitemap/0.84", //
                    "https://www.google.com/schemas/sitemap/0.84", //
                    "http://www.google.com/schemas/sitemap/0.90", //
                    "https://sitemaps.org/schemas/sitemap/0.9",
                    };

    public static final String[] IMAGE = { //
                    "http://www.google.com/schemas/sitemap-image/1.1", //
                    "https://www.google.com/schemas/sitemap-image/1.1" //
                    };

    public static final String[] VIDEO = { //
                    "http://www.google.com/schemas/sitemap-video/1.1", //
                    "https://www.google.com/schemas/sitemap-video/1.1" //
    };

    public static final String[] NEWS = { //
                    "http://www.google.com/schemas/sitemap-news/0.9", //
                    "https://www.google.com/schemas/sitemap-news/0.9", //
                    "http://www.google.com/schemas/sitemap-news/0.84" //
    };

    public static final String[] MOBILE = { //
                    "http://www.google.com/schemas/sitemap-mobile/1.0", //
                    "https://www.google.com/schemas/sitemap-mobile/1.0" //
    };

    public static final String LINKS = "http://www.w3.org/1999/xhtml";

    /**
     * In contradiction to the protocol specification ("The Sitemap must ...
     * [s]pecify the namespace (protocol standard) within the &lt;urlset&gt;
     * tag."), some sitemaps do not define a (default) namespace.
     * 
     * By accepting the "empty" namespace, you'll get URLs even from those
     * sitemaps.
     */
    public static final String EMPTY = "";

    /**
     * RSS and Atom sitemap formats do not have strict definition. But if we do
     * not parse as namespace aware, then RSS/Atom files that choose to use
     * namespaces will break. The relaxed compromise for RSS/Atom is to always
     * parse as "namespace aware", but we will only match elements by the
     * localName, accepting any element namespace.
     */
    public static final String RSS_2_0 = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String ATOM_0_3 = "http://purl.org/atom/ns#";
    public static final String ATOM_1_0 = "http://www.w3.org/2005/Atom";

    public static final Set<String> SITEMAP_SUPPORTED_NAMESPACES = new HashSet<>();
    static {
        SITEMAP_SUPPORTED_NAMESPACES.add(SITEMAP);
        SITEMAP_SUPPORTED_NAMESPACES.addAll(Arrays.asList(SITEMAP_LEGACY));
        SITEMAP_SUPPORTED_NAMESPACES.addAll(Arrays.asList(IMAGE));
        SITEMAP_SUPPORTED_NAMESPACES.addAll(Arrays.asList(VIDEO));
        SITEMAP_SUPPORTED_NAMESPACES.addAll(Arrays.asList(NEWS));
        SITEMAP_SUPPORTED_NAMESPACES.add(LINKS);
    }

    /**
     * @param uri
     *            URI string identifying the namespace
     * @return true if namespace (identified by URI) is supported, false if the
     *         namespace is not supported or unknown
     */
    public static boolean isSupported(String uri) {
        return SITEMAP_SUPPORTED_NAMESPACES.contains(uri);
    }

    public static final Map<Extension, List<String>> SITEMAP_EXTENSION_NAMESPACES = new TreeMap<>();
    static {
        SITEMAP_EXTENSION_NAMESPACES.put(Extension.NEWS, Arrays.asList(NEWS));
        SITEMAP_EXTENSION_NAMESPACES.put(Extension.IMAGE, Arrays.asList(IMAGE));
        SITEMAP_EXTENSION_NAMESPACES.put(Extension.VIDEO, Arrays.asList(VIDEO));
        SITEMAP_EXTENSION_NAMESPACES.put(Extension.MOBILE, Arrays.asList(MOBILE));
        SITEMAP_EXTENSION_NAMESPACES.put(Extension.LINKS, Arrays.asList(LINKS));
    }
}
