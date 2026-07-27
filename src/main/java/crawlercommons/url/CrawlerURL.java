/**
 * Copyright 2026 Crawler-Commons
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

package crawlercommons.url;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * A lightweight, immutable wrapper around a single URL whose alternate
 * representations ({@link java.net.URI}, {@link java.net.URL} and the
 * {@link String} form) and parsed components (scheme, host, port, path, query,
 * fragment, user-info, authority) are computed <em>lazily on first access</em>
 * and then <em>cached</em> in private fields, so repeated access never
 * re-parses.
 *
 * <p>
 * This class exists to address <a href=
 * "https://github.com/crawler-commons/crawler-commons/issues/556">issue
 * #556</a>: crawler-commons APIs historically accepted either {@link String} or
 * {@link java.net.URL}, never {@link java.net.URI}, forcing downstream crawlers
 * to repeatedly convert between representations. At the scale of hundreds of
 * millions of URLs those conversions are wasteful. {@code CrawlerURL} lets a
 * caller hand crawler-commons whatever representation it already has and only
 * pay the cost of converting to another representation if and when it is
 * actually needed -- and then only once.
 * </p>
 *
 * <h2>Error handling</h2>
 * <p>
 * The original input is always retained verbatim and is returned by
 * {@link #toStringURL()} without any parsing. Conversions ({@link #toJavaURL()},
 * {@link #toJavaURI()}) and component accessors parse lazily. If parsing fails,
 * the conversion getters throw an <em>unchecked</em>
 * {@link IllegalStateException} wrapping the underlying
 * {@link java.net.MalformedURLException} / {@link java.net.URISyntaxException}.
 * This keeps call sites clean (no checked exceptions). Both a successful parse
 * and a parse failure are cached, so a malformed instance fails fast and
 * cheaply on every subsequent call.
 * </p>
 *
 * <h2>Thread-safety</h2>
 * <p>
 * Instances are effectively immutable. The lazy caches use plain (non-volatile)
 * fields; in the presence of a data race the worst that can happen is that the
 * (idempotent) computation is performed by more than one thread. No
 * synchronization is used by design.
 * </p>
 *
 * @since 1.6
 */
public final class CrawlerURL {

    /**
     * The canonical string form of the URL. Never {@code null}. Set once at
     * construction time, so {@link #toStringURL()} never has to parse anything.
     */
    private final String stringUrl;

    private URI uri;
    private boolean uriComputed;
    private IllegalStateException uriError;

    private URL url;
    private boolean urlComputed;
    private IllegalStateException urlError;

    private boolean componentsComputed;
    private String scheme;
    private String host;
    private int port = -1;
    private String path;
    private String query;
    private String fragment;
    private String userInfo;
    private String authority;

    private CrawlerURL(String stringUrl, URI uri, URL url) {
        this.stringUrl = stringUrl;
        if (uri != null) {
            this.uri = uri;
            this.uriComputed = true;
        }
        if (url != null) {
            this.url = url;
            this.urlComputed = true;
        }
    }

    /**
     * Creates a {@code CrawlerURL} from its {@link String} representation. The
     * string is stored verbatim and only parsed lazily when an alternate
     * representation or a component is requested.
     *
     * @param url
     *            the URL in string form; must not be {@code null}
     * @return a new {@code CrawlerURL}
     * @see <a href=
     *      "https://github.com/crawler-commons/crawler-commons/issues/556">issue
     *      #556</a>
     */
    public static CrawlerURL of(String url) {
        Objects.requireNonNull(url, "url must not be null");
        return new CrawlerURL(url, null, null);
    }

    /**
     * Creates a {@code CrawlerURL} from a {@link java.net.URI}. The supplied URI
     * is cached, so {@link #toJavaURI()} returns it without re-parsing.
     *
     * @param uri
     *            the source URI; must not be {@code null}
     * @return a new {@code CrawlerURL}
     */
    public static CrawlerURL of(URI uri) {
        Objects.requireNonNull(uri, "uri must not be null");
        return new CrawlerURL(uri.toString(), uri, null);
    }

    /**
     * Creates a {@code CrawlerURL} from a {@link java.net.URL}. The supplied URL
     * is cached, so {@link #toJavaURL()} returns it without re-parsing.
     *
     * @param url
     *            the source URL; must not be {@code null}
     * @return a new {@code CrawlerURL}
     */
    public static CrawlerURL of(URL url) {
        Objects.requireNonNull(url, "url must not be null");
        return new CrawlerURL(url.toString(), null, url);
    }

    /**
     * Alias of {@link #of(URI)}.
     *
     * @param uri
     *            the source URI; must not be {@code null}
     * @return a new {@code CrawlerURL}
     */
    public static CrawlerURL fromJavaURI(URI uri) {
        return of(uri);
    }

    /**
     * Alias of {@link #of(URL)}.
     *
     * @param url
     *            the source URL; must not be {@code null}
     * @return a new {@code CrawlerURL}
     */
    public static CrawlerURL fromJavaURL(URL url) {
        return of(url);
    }

    /**
     * Returns the {@link java.net.URL} representation, computing and caching it
     * on first access.
     *
     * @return the {@code URL} form of this instance
     * @throws IllegalStateException
     *             if the underlying value cannot be parsed as a {@code URL}
     *             (wraps the {@link java.net.MalformedURLException})
     */
    public URL toJavaURL() {
        if (!urlComputed) {
            try {
                // Convert via URI to avoid the deprecated URL(String)
                // constructor, reusing the lazily-cached URI when present.
                URI u = (uri != null) ? uri : new URI(stringUrl);
                this.uri = u;
                this.uriComputed = true;
                this.url = u.toURL();
            } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
                this.urlError = new IllegalStateException("Cannot convert to java.net.URL: " + stringUrl, e);
            }
            this.urlComputed = true;
        }
        if (urlError != null) {
            throw urlError;
        }
        return url;
    }

    /**
     * Returns the {@link java.net.URI} representation, computing and caching it
     * on first access.
     *
     * @return the {@code URI} form of this instance
     * @throws IllegalStateException
     *             if the underlying value cannot be parsed as a {@code URI}
     *             (wraps the {@link java.net.URISyntaxException})
     */
    public URI toJavaURI() {
        if (!uriComputed) {
            try {
                this.uri = new URI(stringUrl);
            } catch (URISyntaxException e) {
                this.uriError = new IllegalStateException("Cannot convert to java.net.URI: " + stringUrl, e);
            }
            this.uriComputed = true;
        }
        if (uriError != null) {
            throw uriError;
        }
        return uri;
    }

    /**
     * Returns the string form of this URL. This is the original input (or the
     * {@code toString()} of the original {@code URI}/{@code URL}) and is
     * returned without any parsing. Never {@code null} for a valid instance.
     *
     * @return the string form of this URL
     */
    public String toStringURL() {
        return stringUrl;
    }

    /**
     * Parses the URL into its components exactly once, using
     * {@link java.net.URI} semantics. Falls back to {@link java.net.URL}
     * semantics when the value is a valid {@code URL} but not a valid
     * {@code URI}. If neither parses, all components remain {@code null} / -1.
     */
    private void ensureComponents() {
        if (componentsComputed) {
            return;
        }
        URI parsedUri = null;
        try {
            parsedUri = toJavaURI();
        } catch (IllegalStateException ignore) {
            // not a valid URI; fall back to URL below
        }
        if (parsedUri != null) {
            this.scheme = parsedUri.getScheme();
            this.host = parsedUri.getHost();
            this.port = parsedUri.getPort();
            this.path = parsedUri.getPath();
            this.query = parsedUri.getQuery();
            this.fragment = parsedUri.getFragment();
            this.userInfo = parsedUri.getUserInfo();
            this.authority = parsedUri.getAuthority();
        } else {
            try {
                URL parsedUrl = toJavaURL();
                this.scheme = parsedUrl.getProtocol();
                this.host = parsedUrl.getHost() == null || parsedUrl.getHost().isEmpty() ? null : parsedUrl.getHost();
                this.port = parsedUrl.getPort();
                this.path = parsedUrl.getPath();
                this.query = parsedUrl.getQuery();
                this.fragment = parsedUrl.getRef();
                this.userInfo = parsedUrl.getUserInfo();
                this.authority = parsedUrl.getAuthority();
            } catch (IllegalStateException ignore) {
                // neither URI nor URL; leave components at their defaults
            }
        }
        this.componentsComputed = true;
    }

    /**
     * Returns the scheme (a.k.a. protocol), or {@code null} if undefined.
     *
     * @return the scheme component
     */
    public String getScheme() {
        ensureComponents();
        return scheme;
    }

    /**
     * Returns the host, or {@code null} if undefined.
     *
     * @return the host component
     */
    public String getHost() {
        ensureComponents();
        return host;
    }

    /**
     * Returns the port, or {@code -1} if unspecified.
     *
     * @return the port component, or {@code -1}
     */
    public int getPort() {
        ensureComponents();
        return port;
    }

    /**
     * Returns the path, or {@code null} if undefined.
     *
     * @return the path component
     */
    public String getPath() {
        ensureComponents();
        return path;
    }

    /**
     * Returns the query string, or {@code null} if undefined.
     *
     * @return the query component
     */
    public String getQuery() {
        ensureComponents();
        return query;
    }

    /**
     * Returns the fragment (a.k.a. ref), or {@code null} if undefined.
     *
     * @return the fragment component
     */
    public String getFragment() {
        ensureComponents();
        return fragment;
    }

    /**
     * Returns the user-info, or {@code null} if undefined.
     *
     * @return the user-info component
     */
    public String getUserInfo() {
        ensureComponents();
        return userInfo;
    }

    /**
     * Returns the authority, or {@code null} if undefined.
     *
     * @return the authority component
     */
    public String getAuthority() {
        ensureComponents();
        return authority;
    }

    /**
     * Two {@code CrawlerURL} instances are equal iff their string forms
     * ({@link #toStringURL()}) are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CrawlerURL)) {
            return false;
        }
        return stringUrl.equals(((CrawlerURL) o).stringUrl);
    }

    /**
     * Hash code consistent with {@link #equals(Object)}; based on the string
     * form.
     */
    @Override
    public int hashCode() {
        return stringUrl.hashCode();
    }

    /**
     * Returns {@link #toStringURL()}.
     */
    @Override
    public String toString() {
        return toStringURL();
    }
}
