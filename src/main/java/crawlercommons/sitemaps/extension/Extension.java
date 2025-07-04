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

/**
 * Sitemap extensions supported by the parser.
 * 
 * See
 * <ul>
 * <li><a href=
 * "https://en.wikipedia.org/wiki/Sitemaps#Additional_sitemap_types">Wikipedia:
 * Additional sitemap types</a></li>
 * <li><a href=
 * "https://developers.google.com/search/docs/crawling-indexing/sitemaps/combine-sitemap-extensions"
 * >How to combine sitemap extensions</a></li>
 * </ul>
 */
public enum Extension {
    /**
     * Google News sitemaps, see
     * https://support.google.com/news/publisher-center/answer/74288
     */
    NEWS,
    /**
     * Google Image sitemaps, see
     * https://support.google.com/webmasters/answer/178636
     */
    IMAGE,
    /**
     * Google Video sitemaps, see
     * https://support.google.com/webmasters/answer/80471
     */
    VIDEO,
    /**
     * Usage of <code>&lt;xhtml:links&gt;</code> in sitemaps to include
     * localized page versions/variants, see
     * https://support.google.com/webmasters/answer/189077
     */
    LINKS,
    /**
     * <cite>Mobile sitemaps just contain an empty "mobile" tag to identify a
     * URL as having mobile content</cite>, cf.
     * http://www.google.com/schemas/sitemap-mobile/1.0
     */
    MOBILE,
    /**
     * <cite>PageMaps is a structured data format that Google created to enable
     * website creators to embed data and notes in their webpages.</cite>, cf.
     * https://support.google.com/programmable-search/answer/1628213
     */
    PAGEMAPS
}
