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

package crawlercommons.domains;

import crawlercommons.domains.EffectiveTldFinder.EffectiveTLD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class EffectiveTldFinderTest {

    @Test
    public final void testDotComEtld() throws Exception {
        EffectiveTLD etld = null;
        etld = EffectiveTldFinder.getEffectiveTLD("com");
        assertEquals("com", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("technorati.com");
        assertEquals("com", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("www.salon.com");
        assertEquals("com", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("blogs.finance.yahoo.com");
        assertEquals("com", etld.getDomain());
    }

    @Test
    public final void testDotNetEtld() throws Exception {
        EffectiveTLD etld = null;
        etld = EffectiveTldFinder.getEffectiveTLD("net");
        assertEquals("net", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("covalent.net");
        assertEquals("net", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("www.openid.net");
        assertEquals("net", etld.getDomain());
    }

    @Test
    public final void testCoJpEtld() throws Exception {
        EffectiveTLD etld = null;
        etld = EffectiveTldFinder.getEffectiveTLD("blogs.yahoo.co.jp");
        assertFalse(etld.isException());
        assertFalse(etld.isWildcard());
        assertEquals("co.jp", etld.getDomain());
    }

    @Test
    public final void testWildcardEtld() throws Exception {
        EffectiveTLD etld = null;
        etld = EffectiveTldFinder.getEffectiveTLD("uk");
        assertFalse(etld.isException());
        assertFalse(etld.isWildcard());
        etld = EffectiveTldFinder.getEffectiveTLD("bbc.co.uk");
        assertFalse(etld.isException());
        assertFalse(etld.isWildcard());
        assertEquals("co.uk", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("www.bbc.co.uk");
        assertFalse(etld.isException());
        assertFalse(etld.isWildcard());
        assertEquals("co.uk", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("anything.uk");
        assertEquals("uk", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("northleamingtonschool.warwickshire.sch.uk");
        assertEquals("warwickshire.sch.uk", etld.getDomain());
        assertTrue(etld.isWildcard());
        assertEquals("*.sch.uk", etld.getSuffix());
        etld = EffectiveTldFinder.getEffectiveTLD("sch.uk");
        assertEquals("uk", etld.getDomain());
        assertFalse(etld.isWildcard());
        assertEquals("uk", etld.getSuffix());
        etld = EffectiveTldFinder.getEffectiveTLD("abc.def.bd");
        assertEquals("def.bd", etld.getDomain());
        assertTrue(etld.isWildcard());
        assertEquals("*.bd", etld.getSuffix());
    }

    @Test
    public final void testExceptionEtld() throws Exception {
        EffectiveTLD etld = null;
        etld = EffectiveTldFinder.getEffectiveTLD("parliament.uk");
        assertFalse(etld.isException());
        assertFalse(etld.isWildcard());
        // should be parliament.uk
        assertEquals("uk", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("www.parliament.uk");
        assertFalse(etld.isException());
        assertFalse(etld.isWildcard());
        // should be parliament.uk
        assertEquals("uk", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("hokkaido.jp");
        assertFalse(etld.isException());
        assertFalse(etld.isWildcard());
        assertEquals("hokkaido.jp", etld.getDomain());
        // test a Japanese city
        etld = EffectiveTldFinder.getEffectiveTLD("www.city.kawasaki.jp");
        assertTrue(etld.isException());
        assertFalse(etld.isWildcard());
        assertEquals("city.kawasaki.jp", etld.getDomain());
        assertEquals("!city.kawasaki.jp", etld.getSuffix());
        assertEquals("city.kawasaki.jp", EffectiveTldFinder.getAssignedDomain("www.city.kawasaki.jp"));
        assertEquals(".kawasaki.jp", EffectiveTldFinder.getAssignedDomain(".kawasaki.jp"));
        assertNull(EffectiveTldFinder.getAssignedDomain(".kawasaki.jp", true));
        assertEquals("city.kawasaki.jp", EffectiveTldFinder.getAssignedDomain("city.kawasaki.jp", true));
        // a wildcard eTLD itself is not a valid domain
        assertNull(EffectiveTldFinder.getAssignedDomain("jp", true));
        // and also items below (matching *.kawasaki.jp) are not valid domains
        assertNull(EffectiveTldFinder.getAssignedDomain("nakahara.kawasaki.jp", true));
        // valid domains are two levels below a wildcard eTLD
        assertEquals("lions.nakahara.kawasaki.jp", EffectiveTldFinder.getAssignedDomain("lions.nakahara.kawasaki.jp", true));
    }

    @Test
    public final void testDotComDomain() throws Exception {
        String ad = null;
        ad = EffectiveTldFinder.getAssignedDomain("com");
        assertEquals("com", ad);
        ad = EffectiveTldFinder.getAssignedDomain("technorati.com");
        assertEquals("technorati.com", ad);
        ad = EffectiveTldFinder.getAssignedDomain("www.salon.com");
        assertEquals("salon.com", ad);
        ad = EffectiveTldFinder.getAssignedDomain("blogs.finance.yahoo.com");
        assertEquals("yahoo.com", ad);
    }

    @Test
    public final void testDotNetDomain() throws Exception {
        String ad = null;
        ad = EffectiveTldFinder.getAssignedDomain("net");
        assertEquals("net", ad);
        ad = EffectiveTldFinder.getAssignedDomain("covalent.net");
        assertEquals("covalent.net", ad);
        ad = EffectiveTldFinder.getAssignedDomain("www.att.net");
        assertEquals("att.net", ad);
        ad = EffectiveTldFinder.getAssignedDomain("ec2-0010.aws.crawlercommons.net");
        assertEquals("crawlercommons.net", ad);
    }

    @Test
    public final void testCoJpDomain() throws Exception {
        String ad = null;
        ad = EffectiveTldFinder.getAssignedDomain("blogs.yahoo.co.jp");
        assertEquals("yahoo.co.jp", ad);
    }

    @Test
    public final void testAcZaDomain() throws Exception {
        // .za isn't a TLD while .ac.za, .alt.za, .co.za, etc. are TLDs
        // according to (as of 2017-08-03)
        // https://www.zadna.org.za/content/page/domain-information/
        EffectiveTLD etld = EffectiveTldFinder.getEffectiveTLD("blogs.uct.ac.za");
        assertNotNull(etld);
        assertEquals("ac.za", etld.getDomain());
        String ad = null;
        ad = EffectiveTldFinder.getAssignedDomain("blogs.uct.ac.za");
        assertEquals("uct.ac.za", ad);
    }

    @Test
    public final void testUkDomain() throws Exception {
        String ad = null;
        ad = EffectiveTldFinder.getAssignedDomain("bbc.co.uk");
        assertEquals("bbc.co.uk", ad);
        ad = EffectiveTldFinder.getAssignedDomain("www.bbc.co.uk");
        assertEquals("bbc.co.uk", ad);
    }

    @Test
    public final void testMixedCaseHostNames() throws Exception {
        String ad = null;
        ad = EffectiveTldFinder.getAssignedDomain("www.PayPal.COM");
        assertEquals("paypal.com", ad);
    }

    @Test
    public final void testIDNDomain() throws Exception {
        String ad = null;
        ad = EffectiveTldFinder.getAssignedDomain("спб.бесплатныеобъявления.рф", true);
        assertEquals("бесплатныеобъявления.рф", ad);
        ad = EffectiveTldFinder.getAssignedDomain("xn--90a1af.xn--80abbembcyvesfij3at4loa4ff.xn--p1ai", true);
        assertEquals("xn--80abbembcyvesfij3at4loa4ff.xn--p1ai", ad);
        // rare but possible mixed use of Unicode and ASCII/Punycode
        ad = EffectiveTldFinder.getAssignedDomain("xn--90a1af.бесплатныеобъявления.рф", true);
        assertEquals("бесплатныеобъявления.рф", ad);
        ad = EffectiveTldFinder.getAssignedDomain("xn--90a1af.xn--80abbembcyvesfij3at4loa4ff.рф", true);
        assertEquals("xn--80abbembcyvesfij3at4loa4ff.рф", ad);
    }

    @Test
    public final void testIDNEtld() throws Exception {
        EffectiveTLD etld = null;
        etld = EffectiveTldFinder.getEffectiveTLD("спб.бесплатныеобъявления.рф", true);
        assertEquals("рф", etld.getDomain());
        assertEquals("рф", etld.getUnicodeDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("xn--90a1af.xn--80abbembcyvesfij3at4loa4ff.xn--p1ai", true);
        assertEquals("xn--p1ai", etld.getDomain());
        assertEquals("рф", etld.getUnicodeDomain());
        // rare but possible mixed use of Unicode and ASCII/Punycode
        etld = EffectiveTldFinder.getEffectiveTLD("xn--90a1af.бесплатныеобъявления.рф", true);
        assertEquals("рф", etld.getDomain());
        assertEquals("рф", etld.getUnicodeDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("xn--90a1af.xn--80abbembcyvesfij3at4loa4ff.рф", true);
        assertEquals("рф", etld.getDomain());
        assertEquals("рф", etld.getUnicodeDomain());

        etld = EffectiveTldFinder.getEffectiveTLD("中国政府网.政务", true);
        assertEquals("政务", etld.getDomain());
        assertEquals("政务", etld.getUnicodeDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("xn--fiqs8sirgfmhq98a.xn--zfr164b", true);
        assertEquals("xn--zfr164b", etld.getDomain());
        assertEquals("政务", etld.getUnicodeDomain());
    }

    @Test
    public final void testStrictDomain() throws Exception {
        String ad = null;
        ad = EffectiveTldFinder.getAssignedDomain("com", false);
        assertNotNull(ad);
        ad = EffectiveTldFinder.getAssignedDomain("com", true);
        assertNull(ad);
    }

    @Test
    public final void testPrivateDomain() throws Exception {
        String ad = null;
        ad = EffectiveTldFinder.getAssignedDomain("myblog.blogspot.com", true, false);
        assertEquals("myblog.blogspot.com", ad);
        ad = EffectiveTldFinder.getAssignedDomain("myblog.blogspot.com", true, true);
        assertEquals("blogspot.com", ad);
    }

    @Test
    public final void testInvalidHostname() throws Exception {
        // in strict mode: there should nothing be returned for invalid
        // hostnames
        assertNull(EffectiveTldFinder.getAssignedDomain("www..example..com", true, false));
        // prohibited Unicode characters in internationalized domain name (IDN),
        // test for #231): � (U+FFFD REPLACEMENT CHARACTER) is prohibited by
        // RFC3490
        // (1) in public suffix
        assertNull(EffectiveTldFinder.getAssignedDomain("www.example.c\ufffdm", true, false));
        // (1a) in wildcard part of a public suffix
        assertNull(EffectiveTldFinder.getAssignedDomain("\ufffd.kawasaki.jp", true, false));
        // (2) in dot-separated segment immediately before public suffix
        // => fail / null : there is no valid domain name
        assertNull(EffectiveTldFinder.getAssignedDomain("www.ex\ufffdmple.com", true, false));
        // (3) in dot-separated segment not part of the domain name
        // => "example.com" is a valid domain name, no check whether the input
        // host name is valid
        assertEquals("example.com", EffectiveTldFinder.getAssignedDomain("\ufffd.example.com", true, false));
        // (4) in strict mode, domain names (or dot-separated segments) should
        // be checked for length restrictions
        StringBuilder domain = new StringBuilder();
        String part63chars = "123456789-abcdefghijklmnopqrstuvwxyz-abcdefghijklmnopqrstuvwxyz";
        domain.append("www.");
        domain.append(part63chars);
        domain.append(".com");
        assertEquals(domain.toString().substring(4), EffectiveTldFinder.getAssignedDomain(domain.toString(), true, false));
        domain = new StringBuilder();
        domain.append("www.");
        domain.append('0');
        domain.append(part63chars);
        domain.append(".com");
        // first part has 64 characters => not a valid domain name
        assertNull(EffectiveTldFinder.getAssignedDomain(domain.toString(), true, false));
        // IDNs: length limits apply (also) to the ASCII/Punycode representation
        domain = new StringBuilder();
        domain.append("www.");
        domain.append(part63chars);
        domain.setCharAt(4, '\u00e0'); // replace `1` by `à`
        domain.append(".com");
        assertNull(EffectiveTldFinder.getAssignedDomain(domain.toString(), true, false));
    }

    @ParameterizedTest
    @CsvSource({ /*
                  * Semantics of first column, in case it's not a a spelled-out
                  * domain name:
                  * 
                  * - `=` the domain name is expected to be equal to hostname
                  * 
                  * - `*` hostname is a wildcard public suffix
                  * 
                  * - `.` hostname is a public suffix (not wildcard)
                  */
                    // test wildcard suffixes
                    "=, foo.bar.gateway.dev", //
                    "*, bar.gateway.dev", //
                    "=, gateway.dev", //
                    "., de", //
                    "=, myspace.nyc3.digitaloceanspaces.com", //
                    "*, nyc3.digitaloceanspaces.com", //
                    "=, digitaloceanspaces.com", //
                    "., com", //
                    "=, foo.us-3.platformsh.site", //
                    "*, us-3.platformsh.site", //
                    "=, platformsh.site", //
                    "., site", //
                    // non-wildcard suffixes
                    "=, nt.global.ssl.fastly.net", //
                    "., global.ssl.fastly.net", //
                    "fastly.net, ssl.fastly.net", //
                    "=, fastly.net", //
                    "., net", //
                    // double-checking counter-examples with only a single
                    // domain under public suffix
                    "=, oneproject.vercel.app", //
                    "=, anotherproject.vercel.app", //
                    "oneproject.vercel.app, subdomain.oneproject.vercel.app", //
                    "., vercel.app", //
                    "., app", //
    })
    public final void testMatchAllSuffixes(String expectedDomain, String hostName) throws Exception {
        String ad = EffectiveTldFinder.getAssignedDomain(hostName, true, false);
        if (expectedDomain == null) {
            assertNull(ad, "Expected null (not a valid domain name under public suffix)");
        } else if ("=".equals(expectedDomain)) {
            assertEquals(hostName, ad, "Domain under public suffix is expected to be same as host");
        } else if (".".equals(expectedDomain)) {
            assertNull(ad, "Host name is a public suffix");
            EffectiveTLD etld = EffectiveTldFinder.getEffectiveTLD(hostName);
            assertEquals(hostName, etld.getSuffix());
        } else if ("*".equals(expectedDomain)) {
            assertNull(ad, "Host name is a wildcard public suffix");
            EffectiveTLD etld = EffectiveTldFinder.getEffectiveTLD(hostName);
            assertTrue(etld.isWildcard());
            assertEquals(hostName, etld.getDomain());
        } else {
            assertEquals(expectedDomain, ad, "Domain under public suffix does not match");
        }
    }
}
