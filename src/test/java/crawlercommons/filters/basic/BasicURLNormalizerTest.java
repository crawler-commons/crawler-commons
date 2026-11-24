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

package crawlercommons.filters.basic;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import crawlercommons.filters.basic.BasicURLNormalizer.IdnNormalization;
import crawlercommons.utils.idn.IdnConverter;
import crawlercommons.utils.idn.IdnConverters;

/** Unit tests for BasicURLNormalizer. */
public class BasicURLNormalizerTest {
    private static BasicURLNormalizer normalizer;

    @BeforeAll
    public static void setup() {
        normalizer = new BasicURLNormalizer();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/normalizer/weirdToNormalizedUrls.csv")
    void testBasicNormalizer(String weirdUrl, String expectedNormalizedUrl) {
        assertEquals(expectedNormalizedUrl, normalizer.filter(weirdUrl), "normalizing: " + weirdUrl);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/normalizer/invalidUrls.csv")
    void testBasicNormalizerExceptionCaught(String weirdUrl) {
        assertNull(normalizer.filter(weirdUrl), "normalizing: " + weirdUrl);
    }

    @Test
    public void testRemoveSessionQueryParameters() {
        normalizer = BasicURLNormalizer.newBuilder().queryParamsToRemove(asList("sid", "phpsessid", "sessionid", "jsessionid")).build();
        normalizeTest("http://foo.com/foo.php?phpsessid=2Aa3ASdfasfdadf&a=1", "http://foo.com/foo.php?a=1");
        normalizeTest("http://foo.com/foo.php?phpsessid=2Aa3ASdfasfdadf&a=1&b", "http://foo.com/foo.php?a=1&b");
        normalizeTest("http://foo.com/foo.php?phpsessid=2Aa3ASdfasfdadf", "http://foo.com/foo.php");
    }

    @ParameterizedTest
    @CsvSource({
                    // sort query parameters lexicographically
                    "https://example.com/path/query.php?foo=bar&hello=world,", //
                    "https://example.com/path/query.php?hello=world&foo=bar,https://example.com/path/query.php?foo=bar&hello=world", //
                    // not a query param
                    "https://example.com/path/show.php?article300,", //
                    // not a query param, slash must remain
                    "https://example.com/path/show.php?/category/3,", //
                    // not a query param, slash and %2F have distinct semantics
                    "https://example.com/?categoryA/categoryB/this_%2F_that,", //
                    // issue #559: percent-encoded unreserved chars in the path
                    // shorten the file when unescaped; the query must still be
                    // parsed without a StringIndexOutOfBoundsException
                    "https://example.org/p%2d1/p%2d2/?q=f,https://example.org/p-1/p-2/?q=f", //
                    "https://example.org/p%2d1/p%2d2/?b=2&a=1,https://example.org/p-1/p-2/?a=1&b=2" })
    public void testQueryParameters(String url, String urlNorm) {
        normalizer = new BasicURLNormalizer();
        if (urlNorm == null) {
            urlNorm = url;
        }
        normalizeTest(url, urlNorm);
    }

    /**
     * Issue #587: the square brackets enclosing an IPv6 host are reserved
     * delimiters and must survive escaping, otherwise the URL is rejected as
     * malformed.
     */
    @ParameterizedTest
    @CsvSource({ //
                    "http://[::1]/,", //
                    "http://[::1],http://[::1]/", //
                    "http://[2001:db8::1]/path?q=1,", //
                    // host is lowercased, default port is dropped
                    "http://[2001:DB8::1]/,http://[2001:db8::1]/", //
                    "http://[::1]:80/,http://[::1]/", //
                    "https://[::1]:443/,https://[::1]/", //
                    // non-default port is kept
                    "http://[::1]:8080/x,", //
                    // IPv4-mapped IPv6 address
                    "http://[::ffff:127.0.0.1]/,", //
                    // path normalization still applies
                    "http://[::1]/a/../b,http://[::1]/b", //
                    // userinfo is stripped as for any other host
                    "http://user:pass@[::1]/x,http://[::1]/x", //
                    // brackets outside the host are still escaped
                    "http://example.com/[x],http://example.com/%5Bx%5D" })
    public void testIPv6Host(String url, String urlNorm) {
        normalizer = new BasicURLNormalizer();
        normalizeTest(url, urlNorm == null ? url : urlNorm);
    }

    /** Issue #587: bracketed hosts which are not valid IPv6 literals. */
    @ParameterizedTest
    @CsvSource({ "http://[::1", "http://[]/", "http://[::1]./", //
                    // percent-encoding is not part of the IP-literal grammar
                    "http://[::%31]/" })
    public void testInvalidIPv6Host(String url) {
        normalizer = new BasicURLNormalizer();
        assertNull(normalizer.filter(url), "normalizing: " + url);
    }

    @Test
    public void testHostToUnicode() {
        normalizer = BasicURLNormalizer.newBuilder().idnNormalization(BasicURLNormalizer.IdnNormalization.UNICODE).build();
        normalizeTest("http://xn--schne-lua.xn--bcher-kva.de/", "http://schöne.bücher.de/");
        normalizeTest("https://xn--90ax2c.xn--p1ai/", "https://нэб.рф/");
    }

    @Test
    public void testNoIdnNormalization() {
        normalizer = BasicURLNormalizer.newBuilder().idnNormalization(BasicURLNormalizer.IdnNormalization.NONE).build();
        // leave the host name as is, even if it's mixed
        normalizeTest("http://schöne.xn--bcher-kva.de/", "http://schöne.xn--bcher-kva.de/");
    }

    /**
     * Issue #616: IDN normalization verifies the round trip between ASCII and
     * Unicode forms, so that the same URLs are accepted in PUNYCODE and UNICODE
     * mode and the results of both modes map to each other. Columns: input URL,
     * expected output with the IDNA2003 (jdk) converter in PUNYCODE and
     * UNICODE mode, expected output with the IDNA2008 / UTS #46 (icu4j)
     * converter in PUNYCODE and UNICODE mode. An empty value means that the URL
     * is rejected (<code>null</code>).
     */
    @ParameterizedTest
    @CsvSource({ //
                    // table in issue #616
                    "https://xn--strae-oqa.de/, https://xn--strae-oqa.de/, https://xn--strae-oqa.de/, https://xn--strae-oqa.de/, https://straße.de/", //
                    "https://straße.de/, https://strasse.de/, https://strasse.de/, https://xn--strae-oqa.de/, https://straße.de/", //
                    "https://strasse.de/, https://strasse.de/, https://strasse.de/, https://strasse.de/, https://strasse.de/", //
                    // Punycode label not decodable (BiDi rule): kept unchanged
                    "https://xn--ovh-2qe.ie/, https://xn--ovh-2qe.ie/, https://xn--ovh-2qe.ie/, https://xn--ovh-2qe.ie/, https://xn--ovh-2qe.ie/", //
                    "https://\u05DCovh.ie/, , , , ", //
                    // further IDNA2003 vs. IDNA2008 deviations
                    "https://faß.de/, https://fass.de/, https://fass.de/, https://xn--fa-hia.de/, https://faß.de/", //
                    "https://xn--fa-hia.de/, https://xn--fa-hia.de/, https://xn--fa-hia.de/, https://xn--fa-hia.de/, https://faß.de/", //
                    "https://xn--bcher-kva.xn--strae-oqa.de/, https://xn--bcher-kva.xn--strae-oqa.de/, https://bücher.xn--strae-oqa.de/, https://xn--bcher-kva.xn--strae-oqa.de/, https://bücher.straße.de/", //
                    // invalid Punycode labels are kept unchanged, cf. WHATWG
                    // URL web platform tests
                    "https://xn--zz.de/, https://xn--zz.de/, https://xn--zz.de/, https://xn--zz.de/, https://xn--zz.de/", //
                    "http://a.b.c.xn--pokxncvks/, http://a.b.c.xn--pokxncvks/, http://a.b.c.xn--pokxncvks/, http://a.b.c.xn--pokxncvks/, http://a.b.c.xn--pokxncvks/", //
                    "https://xn--/, https://xn--/, https://xn--/, https://xn--/, https://xn--/", //
                    "https://bücher.xn--zz.de/, https://xn--bcher-kva.xn--zz.de/, https://bücher.xn--zz.de/, https://xn--bcher-kva.xn--zz.de/, https://bücher.xn--zz.de/", //
                    "https://bücher\u3002xn--zz.de/, https://xn--bcher-kva.xn--zz.de/, https://bücher.xn--zz.de/, https://xn--bcher-kva.xn--zz.de/, https://bücher.xn--zz.de/", //
                    // ... but the remaining labels must be valid
                    "https://\u05DCovh.xn--zz.de/, , , , ", //
                    // IDNs valid under both IDNA2003 and IDNA2008
                    "https://bücher.de/, https://xn--bcher-kva.de/, https://bücher.de/, https://xn--bcher-kva.de/, https://bücher.de/", //
                    "https://BÜCHER.de/, https://xn--bcher-kva.de/, https://bücher.de/, https://xn--bcher-kva.de/, https://bücher.de/", //
                    "https://xn--bcher-kva.de/, https://xn--bcher-kva.de/, https://bücher.de/, https://xn--bcher-kva.de/, https://bücher.de/", //
                    "https://XN--BCHER-KVA.de/, https://xn--bcher-kva.de/, https://bücher.de/, https://xn--bcher-kva.de/, https://bücher.de/", //
                    "http://schöne.xn--bcher-kva.de/, http://xn--schne-lua.xn--bcher-kva.de/, http://schöne.bücher.de/, http://xn--schne-lua.xn--bcher-kva.de/, http://schöne.bücher.de/", //
                    "https://xn--90ax2c.xn--p1ai./, https://xn--90ax2c.xn--p1ai/, https://нэб.рф/, https://xn--90ax2c.xn--p1ai/, https://нэб.рф/", //
                    "https://xn--ls8h.la/, https://xn--ls8h.la/, https://💩.la/, https://xn--ls8h.la/, https://💩.la/", //
                    // trailing dot on a host name with a masked label
                    "https://xn--zz.de./, https://xn--zz.de/, https://xn--zz.de/, https://xn--zz.de/, https://xn--zz.de/", //
                    // empty labels are kept unchanged, regardless of the
                    // converter
                    "https://xn--zz..de/, https://xn--zz..de/, https://xn--zz..de/, https://xn--zz..de/, https://xn--zz..de/", //
                    "https://foo..xn--bcher-kva.de/, https://foo..xn--bcher-kva.de/, https://foo..bücher.de/, https://foo..xn--bcher-kva.de/, https://foo..bücher.de/", //
                    "https://foo..bücher.de/, https://foo..xn--bcher-kva.de/, https://foo..bücher.de/, https://foo..xn--bcher-kva.de/, https://foo..bücher.de/", //
                    // IDNA mapping produces a label separator (U+2488 -> "1."),
                    // changing the number of labels: rejected
                    "https://x\u2488y.xn--zz.de/, , , , ", //
                    // ASCII host names are never rejected: kept in ASCII form
                    // if the round trip fails, here BiDi rule (UTS #46 only)
                    "https://123abc.xn--mgbaam7a8h/, https://123abc.xn--mgbaam7a8h/, https://123abc.امارات/, https://123abc.xn--mgbaam7a8h/, https://123abc.xn--mgbaam7a8h/", //
                    "https://_dmarc.xn--4db.com/, https://_dmarc.xn--4db.com/, https://_dmarc.א.com/, https://_dmarc.xn--4db.com/, https://_dmarc.xn--4db.com/", //
                    // pure ASCII host names without Punycode labels
                    "https://www.example.com/, https://www.example.com/, https://www.example.com/, https://www.example.com/, https://www.example.com/", //
                    "https://fooxn--bar.example.com/, https://fooxn--bar.example.com/, https://fooxn--bar.example.com/, https://fooxn--bar.example.com/, https://fooxn--bar.example.com/" })
    public void testIdnRoundTrip(String url, String jdkPunycode, String jdkUnicode, String icuPunycode, String icuUnicode) {
        IdnConverter jdk = IdnConverters.jdk();
        IdnConverter icu = IdnConverters.icu4j();
        assertEquals(jdkPunycode, idnNormalizer(IdnNormalization.PUNYCODE, jdk).filter(url), "jdk, PUNYCODE: " + url);
        assertEquals(jdkUnicode, idnNormalizer(IdnNormalization.UNICODE, jdk).filter(url), "jdk, UNICODE: " + url);
        assertEquals(icuPunycode, idnNormalizer(IdnNormalization.PUNYCODE, icu).filter(url), "icu4j, PUNYCODE: " + url);
        assertEquals(icuUnicode, idnNormalizer(IdnNormalization.UNICODE, icu).filter(url), "icu4j, UNICODE: " + url);
        assertIdnNormalizationConsistent(jdk, url);
        assertIdnNormalizationConsistent(icu, url);
        // NONE: host name is only lowercased
        assertEquals(idnNormalizer(IdnNormalization.NONE, jdk).filter(url), idnNormalizer(IdnNormalization.NONE, icu).filter(url));
    }

    /**
     * Issue #616: pure ASCII host names without Punycode labels must not be
     * passed to the {@link IdnConverter}.
     */
    @ParameterizedTest
    @CsvSource({ "https://www.example.com/", "https://fooxn--bar.example.com/", "https://foo-xn--bar.example.com/", "http://[::1]/" })
    public void testIdnFastPath(String url) {
        IdnConverter failing = new IdnConverter() {
            @Override
            public String toASCII(String name) {
                throw new UnsupportedOperationException("toASCII(" + name + ")");
            }

            @Override
            public String toUnicode(String name) {
                throw new UnsupportedOperationException("toUnicode(" + name + ")");
            }

            @Override
            public String getName() {
                return "failing";
            }

            @Override
            public String getVersion() {
                return "0";
            }
        };
        for (IdnNormalization mode : IdnNormalization.values()) {
            assertEquals(url, idnNormalizer(mode, failing).filter(url), mode + ": " + url);
        }
    }

    /**
     * Issue #616: a URL is accepted in PUNYCODE mode iff it is accepted in
     * UNICODE mode, the outputs of both modes map to each other, and
     * normalization is idempotent.
     */
    private static void assertIdnNormalizationConsistent(IdnConverter converter, String url) {
        BasicURLNormalizer punycode = idnNormalizer(IdnNormalization.PUNYCODE, converter);
        BasicURLNormalizer unicode = idnNormalizer(IdnNormalization.UNICODE, converter);
        String msg = converter.getName() + ": " + url;
        String p = punycode.filter(url);
        String u = unicode.filter(url);
        if (p == null || u == null) {
            assertSame(p, u, "accepted in one mode only, " + msg + " -> " + p + " / " + u);
            return;
        }
        assertEquals(p, punycode.filter(u), msg);
        assertEquals(u, unicode.filter(p), msg);
        assertEquals(p, punycode.filter(p), msg);
        assertEquals(u, unicode.filter(u), msg);
    }

    private static BasicURLNormalizer idnNormalizer(IdnNormalization mode, IdnConverter converter) {
        return BasicURLNormalizer.newBuilder().idnNormalization(mode).idnConverter(converter).build();
    }

    private void normalizeTest(String weird, String normal) {
        assertEquals(normal, normalizer.filter(weird), "normalizing: " + weird);
    }

}
