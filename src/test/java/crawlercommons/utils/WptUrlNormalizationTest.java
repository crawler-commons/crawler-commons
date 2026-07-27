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

package crawlercommons.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import crawlercommons.filters.basic.BasicURLNormalizer;

/**
 * Runs the vendored WHATWG Web Platform Tests URL corpus
 * ({@code /url-utils/wpt-urltestdata.json}) through the crawler-commons URL
 * pipeline ({@link URLUtils#resolve(URL, String)} +
 * {@link BasicURLNormalizer}). Only the subset of cases that operate on the
 * http/https schemes is considered in scope; everything else is filtered out
 * (skipped, not failed).
 */
public class WptUrlNormalizationTest {

    private static final String RESOURCE = "/url-utils/wpt-urltestdata.json";

    /**
     * Known divergences between the WHATWG URL Standard and the
     * crawler-commons pipeline (which is built on {@link java.net.URI} plus
     * {@link BasicURLNormalizer}). Each entry is a {@link #key(String, String)}
     * value (input + " || " + base) for a case that is in scope but does not
     * yet produce the WHATWG-expected result.
     * <p>
     * Entries are tagged in their inline comment as either
     * {@code WHATWG-divergence} (structural differences between
     * {@link java.net.URI} + {@link BasicURLNormalizer} and the WHATWG URL
     * Standard) or {@code cc-gap} (likely crawler-commons bugs worth revisiting
     * as a follow-up to issue #535).
     */
    private static final Set<String> EXCLUSIONS = new HashSet<>();

    static {
        // WHATWG-divergence: cases where java.net.URI + BasicURLNormalizer
        // structurally differ from the WHATWG URL Standard (percent-encode
        // sets, backslash handling, default-port/userinfo handling, IDNA,
        // tab/newline stripping, IPv4/host canonicalization, etc.).
        EXCLUSIONS.add("http://example\t.\norg || http://example.org/foo/bar"); // WHATWG-divergence: tab/newline stripped before parsing; Java URI rejects
        EXCLUSIONS.add("http://user:pass@foo:21/bar;par?b#c || http://example.org/foo/bar"); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("https://test:@test || "); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("http:foo.com || http://example.org/foo/bar"); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("\t   :foo.com   \n || http://example.org/foo/bar"); // WHATWG-divergence: tab/newline stripped before parsing; Java URI rejects
        EXCLUSIONS.add(" foo.com   || http://example.org/foo/bar"); // WHATWG-divergence: leading/trailing C0/space not trimmed; Java URI rejects
        EXCLUSIONS.add("http://f:21/ b ? d # e  || http://example.org/foo/bar"); // WHATWG-divergence: leading/trailing C0/space not trimmed; Java URI rejects
        EXCLUSIONS.add("http://f:\n/c || http://example.org/foo/bar"); // WHATWG-divergence: tab/newline stripped before parsing; Java URI rejects
        EXCLUSIONS.add("http://f:999999/c || http://example.org/foo/bar"); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add(" || http://example.org/foo/bar"); // WHATWG-divergence: resolution/normalization differs from WHATWG URL Standard
        EXCLUSIONS.add("  \t || http://example.org/foo/bar"); // WHATWG-divergence: tab/newline stripped before parsing; Java URI rejects
        EXCLUSIONS.add(":foo.com\\ || http://example.org/foo/bar"); // WHATWG-divergence: backslash treated as path separator for special schemes; Java URI rejects
        EXCLUSIONS.add(":\\ || http://example.org/foo/bar"); // WHATWG-divergence: backslash treated as path separator for special schemes; Java URI rejects
        EXCLUSIONS.add(":# || http://example.org/foo/bar"); // WHATWG-divergence: crawler-commons drops the fragment component
        EXCLUSIONS.add("# || http://example.org/foo/bar"); // WHATWG-divergence: crawler-commons drops the fragment component
        EXCLUSIONS.add("#/ || http://example.org/foo/bar"); // WHATWG-divergence: crawler-commons drops the fragment component
        EXCLUSIONS.add("#\\ || http://example.org/foo/bar"); // WHATWG-divergence: backslash treated as path separator for special schemes; Java URI rejects
        EXCLUSIONS.add("#;? || http://example.org/foo/bar"); // WHATWG-divergence: crawler-commons drops the fragment component
        EXCLUSIONS.add("? || http://example.org/foo/bar"); // WHATWG-divergence: empty query/fragment delimiter preserved by WHATWG; cc drops it
        EXCLUSIONS.add("\\x || http://example.org/foo/bar"); // WHATWG-divergence: backslash treated as path separator for special schemes; Java URI rejects
        EXCLUSIONS.add("\\\\x\\hello || http://example.org/foo/bar"); // WHATWG-divergence: backslash treated as path separator for special schemes; Java URI rejects
        EXCLUSIONS.add("http://a:b@c:29/d || http://example.org/foo/bar"); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("http://\u00e9@\u00e9 || "); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("http://\u00e9@example.com || "); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("http::@c:29 || http://example.org/foo/bar"); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("http://&a:foo(b]c@d:2/ || http://example.org/foo/bar"); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://::@c@d:2 || http://example.org/foo/bar"); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("http://foo.com:b@d/ || http://example.org/foo/bar"); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("http://foo.com/\\@ || http://example.org/foo/bar"); // WHATWG-divergence: backslash treated as path separator for special schemes; Java URI rejects
        EXCLUSIONS.add("http:\\\\foo.com\\ || http://example.org/foo/bar"); // WHATWG-divergence: backslash treated as path separator for special schemes; Java URI rejects
        EXCLUSIONS.add("http:\\\\a\\b:c\\d@foo.com\\ || http://example.org/foo/bar"); // WHATWG-divergence: backslash treated as path separator for special schemes; Java URI rejects
        EXCLUSIONS.add("http://a:b@c\\ || "); // WHATWG-divergence: backslash treated as path separator for special schemes; Java URI rejects
        EXCLUSIONS.add("http://foo/path;a??e#f#g || http://example.org/foo/bar"); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://foo/abcd#foo?bar || http://example.org/foo/bar"); // WHATWG-divergence: crawler-commons drops the fragment component
        EXCLUSIONS.add("[61:24:74]:98 || http://example.org/foo/bar"); // WHATWG-divergence: IPv6 literal parsing/compression; Java URI rejects
        EXCLUSIONS.add("http:[61:27]/:foo || http://example.org/foo/bar"); // WHATWG-divergence: IPv6 literal canonicalization differs
        EXCLUSIONS.add("http://[2001::1] || http://example.org/foo/bar"); // WHATWG-divergence: IPv6 literal parsing/compression; Java URI rejects
        EXCLUSIONS.add("http://[::127.0.0.1] || http://example.org/foo/bar"); // WHATWG-divergence: IPv6 literal parsing/compression; Java URI rejects
        EXCLUSIONS.add("http://[0:0:0:0:0:0:13.1.68.3] || http://example.org/foo/bar"); // WHATWG-divergence: IPv6 literal parsing/compression; Java URI rejects
        EXCLUSIONS.add("http://[2001::1]:80 || http://example.org/foo/bar"); // WHATWG-divergence: IPv6 literal parsing/compression; Java URI rejects
        EXCLUSIONS.add("http:/example.com/ || http://example.org/foo/bar"); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("http:/ || http://example.com/"); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("https:/example.com/ || http://example.org/foo/bar"); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("http:example.com/ || http://example.org/foo/bar"); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("https:example.com/ || http://example.org/foo/bar"); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("/a/ /c || http://example.org/foo/bar"); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("/a%2fc || http://example.org/foo/bar"); // WHATWG-divergence: percent-encoding case normalization differs (cc uppercases)
        EXCLUSIONS.add("/a/%2f/c || http://example.org/foo/bar"); // WHATWG-divergence: percent-encoding case normalization differs (cc uppercases)
        EXCLUSIONS.add("#\u03b2 || http://example.org/foo/bar"); // WHATWG-divergence: crawler-commons drops the fragment component
        EXCLUSIONS.add("http://example.com/foo/%2e%2 || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://example.com/foo% || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://example.com/foo%2 || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://example.com/foo%2zbar || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://example.com/foo%2\u00c2\u00a9zbar || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://example.com/foo%41%7a || "); // WHATWG-divergence: percent-encoding preservation/decoding differs (cc decodes unreserved)
        EXCLUSIONS.add("http://example.com/foo\t\u0091%91 || "); // WHATWG-divergence: tab/newline stripped before parsing; Java URI rejects
        EXCLUSIONS.add("http://example.com/foo%00%51 || "); // WHATWG-divergence: percent-encoding preservation/decoding differs (cc decodes unreserved)
        EXCLUSIONS.add("http://example.com/%3A%3a%3C%3c || "); // WHATWG-divergence: percent-encoding case normalization differs (cc uppercases)
        EXCLUSIONS.add("http://example.com/foo\tbar || "); // WHATWG-divergence: tab/newline stripped before parsing; Java URI rejects
        EXCLUSIONS.add("http://example.com\\\\foo\\\\bar || "); // WHATWG-divergence: backslash treated as path separator for special schemes; Java URI rejects
        EXCLUSIONS.add("http://www.google.com/foo?bar=baz# || "); // WHATWG-divergence: crawler-commons drops the fragment component
        EXCLUSIONS.add("http://www.google.com/foo?bar=baz# \u00bb || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://192.0x00A80001 || "); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("http://www/foo%2Ehtml || "); // WHATWG-divergence: percent-encoding preservation/decoding differs (cc decodes unreserved)
        EXCLUSIONS.add("http://user:pass@/ || "); // WHATWG-divergence: empty host after userinfo not rejected
        EXCLUSIONS.add("http://%25DOMAIN:foobar@foodomain.com/ || "); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("http:\\\\www.google.com\\foo || "); // WHATWG-divergence: backslash treated as path separator for special schemes; Java URI rejects
        EXCLUSIONS.add("http:/example.com/ || "); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("https:/example.com/ || "); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("http:example.com/ || "); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("https:example.com/ || "); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("https://example.com/aaa/bbb/%2e%2e?query || "); // WHATWG-divergence: percent-encoding preservation/decoding differs (cc decodes unreserved)
        EXCLUSIONS.add("http:@www.example.com || "); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("http:/@www.example.com || "); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("http:a:b@www.example.com || "); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("http:/a:b@www.example.com || "); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("http://a:b@www.example.com || "); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("http::b@www.example.com || "); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("http:/:b@www.example.com || "); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("http://:b@www.example.com || "); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("http://user@/www.example.com || "); // WHATWG-divergence: empty host after userinfo not rejected
        EXCLUSIONS.add("http://@/www.example.com || "); // WHATWG-divergence: empty host after userinfo not rejected
        EXCLUSIONS.add("http://a:b@/www.example.com || "); // WHATWG-divergence: empty host after userinfo not rejected
        EXCLUSIONS.add("http:a:@www.example.com || "); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("http:/a:@www.example.com || "); // WHATWG-divergence: relative/opaque scheme prefix resolved against base differently
        EXCLUSIONS.add("http://a:@www.example.com || "); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("http://www.@pple.com || "); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("\u0000\u001b\u0004\u0012 http://example.com/\u001f \r  || "); // WHATWG-divergence: tab/newline stripped before parsing; Java URI rejects
        EXCLUSIONS.add("https://x/\ufffd?\ufffd#\ufffd || "); // WHATWG-divergence: crawler-commons drops the fragment component
        EXCLUSIONS.add("http://\uff05\uff14\uff11.com || http://other.com/"); // WHATWG-divergence: fullwidth/non-ASCII host validation differs (IDNA/forbidden)
        EXCLUSIONS.add("http://%ef%bc%85%ef%bc%94%ef%bc%91.com || http://other.com/"); // WHATWG-divergence: fullwidth/non-ASCII host validation differs (IDNA/forbidden)
        EXCLUSIONS.add("http://\uff05\uff10\uff10.com || http://other.com/"); // WHATWG-divergence: fullwidth/non-ASCII host validation differs (IDNA/forbidden)
        EXCLUSIONS.add("http://%ef%bc%85%ef%bc%90%ef%bc%90.com || http://other.com/"); // WHATWG-divergence: fullwidth/non-ASCII host validation differs (IDNA/forbidden)
        EXCLUSIONS.add("https://fa\u00df.ExAmPlE/ || "); // WHATWG-divergence: IDNA/UTS-46 host mapping differs from crawler-commons
        EXCLUSIONS.add("http://%30%78%63%30%2e%30%32%35%30.01 || http://other.com/"); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("http://%30%78%63%30%2e%30%32%35%30.01%2e || http://other.com/"); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("http://192.168.0.257 || http://other.com/"); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://\uff10\uff38\uff43\uff10\uff0e\uff10\uff12\uff15\uff10\uff0e\uff10\uff11 || http://other.com/"); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("http://./ || "); // WHATWG-divergence: bare '.'/'..' host normalized/dropped differently than WHATWG
        EXCLUSIONS.add("http://../ || "); // WHATWG-divergence: bare '.'/'..' host normalized/dropped differently than WHATWG
        EXCLUSIONS.add("http://foo:\ud83d\udca9@example.com/bar || http://other.com/"); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("https://localhost#\ud83d\udd25 || "); // WHATWG-divergence: crawler-commons drops the fragment component
        EXCLUSIONS.add("https://@test@test@example:800/ || http://doesnotmatter/"); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("https://@@@example || http://doesnotmatter/"); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("http://`{}:`{}@h/`{}?`{} || http://doesnotmatter/"); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://host/?' || "); // WHATWG-divergence: resolution/normalization differs from WHATWG URL Standard
        EXCLUSIONS.add("/some/path || http://user@example.org/smth"); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add(" || http://user:pass@example.org:21/smth"); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("/some/path || http://user:pass@example.org:21/smth"); // WHATWG-divergence: crawler-commons strips userinfo from authority
        EXCLUSIONS.add("http://ho\tst/ || "); // WHATWG-divergence: tab/newline stripped before parsing; Java URI rejects
        EXCLUSIONS.add("http://ho\nst/ || "); // WHATWG-divergence: tab/newline stripped before parsing; Java URI rejects
        EXCLUSIONS.add("http://ho\rst/ || "); // WHATWG-divergence: tab/newline stripped before parsing; Java URI rejects
        EXCLUSIONS.add("http://ho%23st/ || "); // WHATWG-divergence: forbidden host code point (percent-encoded) not rejected
        EXCLUSIONS.add("http://ho%2Fst/ || "); // WHATWG-divergence: forbidden host code point (percent-encoded) not rejected
        EXCLUSIONS.add("http://ho%3Fst/ || "); // WHATWG-divergence: forbidden host code point (percent-encoded) not rejected
        EXCLUSIONS.add("http://ho%40st/ || "); // WHATWG-divergence: forbidden host code point (percent-encoded) not rejected
        EXCLUSIONS.add("http://!\"$&'()*+,-.;=_`{}~/ || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http: || http://example.org/foo/bar"); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://foo.bar/baz?qux#foo\u0008bar || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://foo.bar/baz?qux#foo\"bar || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://foo.bar/baz?qux#foo<bar || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://foo.bar/baz?qux#foo>bar || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://foo.bar/baz?qux#foo`bar || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://192.168.257 || http://other.com/"); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("http://192.168.257. || http://other.com/"); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("http://256 || http://other.com/"); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("http://999999999 || http://other.com/"); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("http://999999999. || http://other.com/"); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("http://10000000000 || http://other.com/"); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://4294967295 || http://other.com/"); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("http://4294967296 || http://other.com/"); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://18446744073709551616 || http://other.com/"); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://18446744075840258049 || http://other.com/"); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://0xffffffff || http://other.com/"); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("http://0xffffffff1 || http://other.com/"); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://256.256.256.256 || http://other.com/"); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("https://0x.0x.0 || "); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("https://0x.0x.0x.0x || "); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("https://00.00.00.00 || "); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("https://0000000000000000000000000000000000000000177.0.0.1 || "); // WHATWG-divergence: WHATWG canonicalizes host to dotted-decimal IPv4; cc leaves as-is
        EXCLUSIONS.add("https://0x100000000/test || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("https://256.0.0.1/test || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://[1:0::] || http://example.net/"); // WHATWG-divergence: IPv6 literal parsing/compression; Java URI rejects
        EXCLUSIONS.add("http://? || "); // WHATWG-divergence: empty host not rejected
        EXCLUSIONS.add("http://# || "); // WHATWG-divergence: empty host not rejected
        EXCLUSIONS.add("http://[0:1:0:1:0:1:0:1] || "); // WHATWG-divergence: IPv6 literal parsing/compression; Java URI rejects
        EXCLUSIONS.add("http://[1:0:1:0:1:0:1:0] || "); // WHATWG-divergence: IPv6 literal parsing/compression; Java URI rejects
        EXCLUSIONS.add("http://example.org/test?\" || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://example.org/test?# || "); // WHATWG-divergence: crawler-commons drops the fragment component
        EXCLUSIONS.add("http://example.org/test?< || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://example.org/test?> || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://example.org/test?%GH || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://example.org/test?a#%EF || "); // WHATWG-divergence: crawler-commons drops the fragment component
        EXCLUSIONS.add("http://example.org/test?a#%GH || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("http://example.org/test?a#b\u0000c || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("#link || https://example.org/##link"); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("https://www.example.com/path{\u007fpath.html?query'\u007f=query#fragment<\u007ffragment || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("https://user:pass[\u007f@foo/bar || http://example.org"); // WHATWG-divergence: IPv6 literal parsing/compression; Java URI rejects
        EXCLUSIONS.add("http://1.2.3.4.5 || http://other.com/"); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://1.2.3.4.5. || http://other.com/"); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://0..0x300/ || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://0..0x300./ || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://256.256.256.256.256 || http://other.com/"); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://256.256.256.256.256. || http://other.com/"); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://1.2.3.08 || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://1.2.3.08. || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://1.2.3.09 || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://09.2.3.4 || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://09.2.3.4. || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://01.2.3.4.5 || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://01.2.3.4.5. || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://0x100.2.3.4 || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://0x100.2.3.4. || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://0x1.2.3.4.5 || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://0x1.2.3.4.5. || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://foo.1.2.3.4 || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://foo.1.2.3.4. || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://foo.2.3.4 || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://foo.2.3.4. || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://foo.09 || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://foo.09. || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://foo.0x4 || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://foo.0x4. || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://foo.09.. || "); // WHATWG-divergence: resolution/normalization differs from WHATWG URL Standard
        EXCLUSIONS.add("http://0999999999999999999/ || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://foo.0x || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://foo.0XFfFfFfFfFfFfFfFfFfAcE123 || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("http://\ud83d\udca9.123/ || "); // WHATWG-divergence: WHATWG rejects invalid IPv4/host; Java URI accepts
        EXCLUSIONS.add("https://x/\u0000y || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("https://x/?\u0000y || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("https://x/?#\u0000y || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("https://x/?#\uffffy || "); // WHATWG-divergence: crawler-commons drops the fragment component
        EXCLUSIONS.add("https://example.com/\"quoted\" || "); // WHATWG-divergence: WHATWG percent-encodes forbidden chars in path/query/fragment; Java URI rejects
        EXCLUSIONS.add("///test || http://example.org/"); // WHATWG-divergence: resolution/normalization differs from WHATWG URL Standard
        EXCLUSIONS.add("///\\//\\//test || http://example.org/"); // WHATWG-divergence: backslash treated as path separator for special schemes; Java URI rejects
        EXCLUSIONS.add("///example.org/path || http://example.org/"); // WHATWG-divergence: resolution/normalization differs from WHATWG URL Standard
        EXCLUSIONS.add("/\\/\\//example.org/../path || http://example.org/"); // WHATWG-divergence: backslash treated as path separator for special schemes; Java URI rejects

        // cc-gap: plausible crawler-commons normalization bugs worth a
        // #535 follow-up (dot-segment / empty-segment path handling).
        EXCLUSIONS.add("http://example.com/foo/%2e./%2e%2e/.%2e/%2e.bar || "); // cc-gap: dot-segment removal corrupts authority/path
        EXCLUSIONS.add("http://example.com////../.. || "); // cc-gap: empty path segments collapsed differently during dot-removal
        EXCLUSIONS.add("http://example.com/foo/bar//../.. || "); // cc-gap: empty path segments collapsed differently during dot-removal
        EXCLUSIONS.add("http://example.com/foo/bar//.. || "); // cc-gap: empty path segments collapsed differently during dot-removal
    }

    /**
     * Build the stable key used both for {@link #EXCLUSIONS} membership and for
     * test reporting.
     *
     * @param input
     *            the WPT input URL string
     * @param base
     *            the WPT base URL string, or {@code null} when absent
     * @return {@code input + " || " + (base == null ? "" : base)}
     */
    private static String key(String input, String base) {
        return input + " || " + (base == null ? "" : base);
    }

    static Stream<Arguments> cases() throws IOException {
        String json;
        try (InputStream in = WptUrlNormalizationTest.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing test resource: " + RESOURCE);
            }
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<Object> elements = SimpleJsonArrayParser.parse(json);
        List<Arguments> out = new ArrayList<>();
        for (Object element : elements) {
            // Skip free-standing comment strings.
            if (!(element instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> entry = (Map<String, Object>) element;

            String input = (String) entry.get("input");
            String base = (String) entry.get("base");
            String relativeTo = (String) entry.get("relativeTo");

            // Skip opaque-path bases.
            if ("non-opaque-path-base".equals(relativeTo)) {
                continue;
            }
            // Keep only the http/https subset.
            if (!inScope(input, base)) {
                continue;
            }
            // Skip known divergences recorded for the #535 follow-up.
            if (EXCLUSIONS.contains(key(input, base))) {
                continue;
            }

            boolean failure = Boolean.TRUE.equals(entry.get("failure"));
            String href = (String) entry.get("href");
            out.add(Arguments.of(input, base, failure, href));
        }
        return out.stream();
    }

    @ParameterizedTest(name = "[{index}] input=[{0}] base=[{1}]")
    @MethodSource("cases")
    void wpt(String input, String base, boolean failure, String expectedHref) throws Exception {
        URL resolved = null;
        boolean rejected = false;
        try {
            URL baseUrl = (base == null) ? null : new URI(base).toURL();
            resolved = URLUtils.resolve(baseUrl, input);
        } catch (MalformedURLException | URISyntaxException e) {
            rejected = true;
        }

        String result = (resolved == null) ? null : new BasicURLNormalizer().filter(resolved.toString());

        String message = "input=[" + input + "] base=[" + base + "]";
        if (failure) {
            assertTrue(rejected || result == null, "expected rejection for " + message);
        } else {
            assertEquals(expectedHref, result, message);
        }
    }

    /**
     * An entry is in scope when it exercises only the http/https schemes: the
     * base (if present) must be http/https, and the input must be either an
     * absolute http/https URL, or a scheme-relative/relative reference resolved
     * against an http/https base.
     */
    private static boolean inScope(String input, String base) {
        if (base != null && !isHttpScheme(absoluteScheme(base))) {
            return false;
        }
        String inputScheme = absoluteScheme(input);
        if (inputScheme != null) {
            // Absolute input: keep only when its scheme is http/https.
            return isHttpScheme(inputScheme);
        }
        // Scheme-relative ("//host/..") or relative input: needs an http(s)
        // base (already validated above) to be in scope.
        return base != null;
    }

    private static boolean isHttpScheme(String scheme) {
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    /**
     * Extract the scheme of an absolute URL reference, or {@code null} when the
     * reference is relative or scheme-relative. Leading C0 controls and spaces
     * (which URL parsers strip) are ignored.
     */
    private static String absoluteScheme(String ref) {
        if (ref == null) {
            return null;
        }
        int n = ref.length();
        int i = 0;
        while (i < n && ref.charAt(i) <= ' ') {
            i++;
        }
        if (i >= n || !isAsciiAlpha(ref.charAt(i))) {
            return null;
        }
        int start = i;
        i++;
        while (i < n) {
            char c = ref.charAt(i);
            if (isAsciiAlpha(c) || (c >= '0' && c <= '9') || c == '+' || c == '-' || c == '.') {
                i++;
            } else {
                break;
            }
        }
        if (i < n && ref.charAt(i) == ':') {
            return ref.substring(start, i);
        }
        return null;
    }

    private static boolean isAsciiAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
}
