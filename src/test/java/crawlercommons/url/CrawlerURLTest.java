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

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CrawlerURLTest {

    private static final String SAMPLE = "http://user:pw@www.example.com:8080/a/b/c?q=1&r=2#frag";

    @Test
    public final void testRoundTripFromString() throws Exception {
        CrawlerURL u = CrawlerURL.of(SAMPLE);
        assertEquals(SAMPLE, u.toStringURL());
        assertEquals(new URI(SAMPLE), u.toJavaURI());
        assertEquals(new URI(SAMPLE).toURL(), u.toJavaURL());
    }

    @Test
    public final void testRoundTripFromURI() throws Exception {
        URI uri = new URI(SAMPLE);
        CrawlerURL u = CrawlerURL.of(uri);
        assertEquals(SAMPLE, u.toStringURL());
        assertEquals(uri, u.toJavaURI());
        assertEquals(uri.toURL(), u.toJavaURL());
    }

    @Test
    public final void testRoundTripFromURL() throws Exception {
        URL url = new URI(SAMPLE).toURL();
        CrawlerURL u = CrawlerURL.of(url);
        assertEquals(url.toString(), u.toStringURL());
        assertEquals(url, u.toJavaURL());
        assertEquals(new URI(SAMPLE), u.toJavaURI());
    }

    @Test
    public final void testFactoryAliases() throws Exception {
        URI uri = new URI(SAMPLE);
        URL url = uri.toURL();
        assertEquals(CrawlerURL.of(uri), CrawlerURL.fromJavaURI(uri));
        assertEquals(CrawlerURL.of(url), CrawlerURL.fromJavaURL(url));
    }

    @Test
    public final void testConversionsCached() {
        CrawlerURL u = CrawlerURL.of(SAMPLE);
        URI uri1 = u.toJavaURI();
        URI uri2 = u.toJavaURI();
        assertSame(uri1, uri2, "URI should be cached and identical on repeated access");

        URL url1 = u.toJavaURL();
        URL url2 = u.toJavaURL();
        assertSame(url1, url2, "URL should be cached and identical on repeated access");
    }

    @Test
    public final void testSuppliedRepresentationsAreCached() throws Exception {
        URI uri = new URI(SAMPLE);
        assertSame(uri, CrawlerURL.of(uri).toJavaURI(), "supplied URI must be returned as-is");

        URL url = uri.toURL();
        assertSame(url, CrawlerURL.of(url).toJavaURL(), "supplied URL must be returned as-is");
    }

    @Test
    public final void testComponentExtraction() {
        CrawlerURL u = CrawlerURL.of(SAMPLE);
        assertEquals("http", u.getScheme());
        assertEquals("www.example.com", u.getHost());
        assertEquals(8080, u.getPort());
        assertEquals("/a/b/c", u.getPath());
        assertEquals("q=1&r=2", u.getQuery());
        assertEquals("frag", u.getFragment());
        assertEquals("user:pw", u.getUserInfo());
        assertEquals("user:pw@www.example.com:8080", u.getAuthority());
    }

    @Test
    public final void testDefaultPortIsMinusOne() {
        CrawlerURL u = CrawlerURL.of("http://www.example.com/path");
        assertEquals(-1, u.getPort());
        assertEquals("www.example.com", u.getHost());
        assertNull(u.getQuery());
        assertNull(u.getFragment());
        assertNull(u.getUserInfo());
    }

    @Test
    public final void testComponentsCached() {
        CrawlerURL u = CrawlerURL.of(SAMPLE);
        assertEquals(u.getHost(), u.getHost());
        assertEquals(u.getScheme(), u.getScheme());
        assertEquals(u.getPath(), u.getPath());
        // Same string instance returned each time (cached, not re-parsed).
        assertSame(u.getHost(), u.getHost());
    }

    @Test
    public final void testEqualsHashCodeToString() {
        CrawlerURL a = CrawlerURL.of(SAMPLE);
        CrawlerURL b = CrawlerURL.of(SAMPLE);
        CrawlerURL c = CrawlerURL.of("http://other.example.org/");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertEquals(SAMPLE, a.toString());
        assertEquals(a.toStringURL(), a.toString());
        assertNotEquals(a, null);
        assertNotEquals(a, "a string");
        assertTrue(a.equals(a));
    }

    @Test
    public final void testEqualsAcrossFactories() throws Exception {
        CrawlerURL fromString = CrawlerURL.of(SAMPLE);
        CrawlerURL fromUri = CrawlerURL.of(new URI(SAMPLE));
        assertEquals(fromString, fromUri);
        assertEquals(fromString.hashCode(), fromUri.hashCode());
    }

    @Test
    public final void testMalformedUrlThrowsUnchecked() {
        // Not a valid URL (no protocol) -> toJavaURL must throw unchecked.
        CrawlerURL u = CrawlerURL.of("www.example.com/no-scheme");
        IllegalStateException ex = assertThrows(IllegalStateException.class, u::toJavaURL);
        assertNotNull(ex.getCause());
        // string form is always available regardless
        assertEquals("www.example.com/no-scheme", u.toStringURL());
    }

    @Test
    public final void testMalformedUriThrowsUnchecked() {
        // Space is illegal in a raw URI.
        CrawlerURL u = CrawlerURL.of("http://example.com/a b c");
        assertThrows(IllegalStateException.class, u::toJavaURI);
        assertEquals("http://example.com/a b c", u.toStringURL());
    }

    @Test
    public final void testMalformedConversionFailureCached() {
        CrawlerURL u = CrawlerURL.of("http://example.com/a b c");
        IllegalStateException first = assertThrows(IllegalStateException.class, u::toJavaURI);
        IllegalStateException second = assertThrows(IllegalStateException.class, u::toJavaURI);
        assertSame(first, second, "the cached failure should be reused, not recomputed");
    }

    @Test
    public final void testStringFactoryRejectsNull() {
        assertThrows(NullPointerException.class, () -> CrawlerURL.of((String) null));
        assertThrows(NullPointerException.class, () -> CrawlerURL.of((URI) null));
        assertThrows(NullPointerException.class, () -> CrawlerURL.of((URL) null));
    }

    private static void assertNotNull(Object o) {
        assertFalse(o == null, "expected non-null");
    }
}
