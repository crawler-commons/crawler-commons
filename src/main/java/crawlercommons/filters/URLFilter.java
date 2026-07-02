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

package crawlercommons.filters;

import java.net.URI;

import crawlercommons.url.CrawlerURL;

public abstract class URLFilter {

    /**
     * Returns a modified version of the input URL or null if the URL should be
     * removed
     *
     * @param urlString
     *            a URL string to check against filter(s)
     * @return a filtered URL
     **/
    public abstract String filter(String urlString);

    /**
     * Returns a modified version of the input URL or null if the URL should be
     * removed.
     *
     * <p>
     * Convenience overload accepting a {@link java.net.URI} (see <a href=
     * "https://github.com/crawler-commons/crawler-commons/issues/556">issue
     * #556</a>). The default implementation delegates to
     * {@link #filter(String)} using the URI's string form, so existing
     * subclasses keep working without modification. Subclasses may override it
     * to avoid the round-trip through {@link String}.
     * </p>
     *
     * @param uri
     *            a URI to check against filter(s)
     * @return a filtered URL string
     **/
    public String filter(URI uri) {
        return filter(uri.toString());
    }

    /**
     * Returns a modified version of the input URL or null if the URL should be
     * removed.
     *
     * <p>
     * Convenience overload accepting a {@link CrawlerURL} (see <a href=
     * "https://github.com/crawler-commons/crawler-commons/issues/556">issue
     * #556</a>). The default implementation delegates to
     * {@link #filter(String)} using {@link CrawlerURL#toStringURL()}, so
     * existing subclasses keep working without modification. Subclasses may
     * override it to reuse the already-parsed representation carried by the
     * {@link CrawlerURL}.
     * </p>
     *
     * @param url
     *            a {@link CrawlerURL} to check against filter(s)
     * @return a filtered URL string
     **/
    public String filter(CrawlerURL url) {
        return filter(url.toStringURL());
    }

}
