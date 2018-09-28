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
 * Google mobile sitemap attributes, see
 * http://www.google.de/schemas/sitemap-mobile/1.0/ and
 * https://www.google.com/schemas/sitemap-mobile/1.0/sitemap-mobile.xsd:
 * <blockquote>Mobile sitemaps just contain an empty "mobile" tag to identify a
 * URL as having mobile content.</blockquote>
 */
public class MobileAttributes extends ExtensionMetadata {

    @Override
    public String toString() {
        return "Mobile content avaiblabe: yes";
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof MobileAttributes)) {
            return false;
        }
        return true;
    }

}
