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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

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

    private void normalizeTest(String weird, String normal) {
        assertEquals(normal, normalizer.filter(weird), "normalizing: " + weird);
    }

}
