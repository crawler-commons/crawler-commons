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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import crawlercommons.domains.EffectiveTldFinder;
import crawlercommons.domains.EffectiveTldFinder.EffectiveTLD;

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
        assertFalse(etld.isWild());
        assertEquals("co.jp", etld.getDomain());
    }

    @Test
    public final void testWildcardEtld() throws Exception {
        EffectiveTLD etld = null;
        etld = EffectiveTldFinder.getEffectiveTLD("uk");
        assertFalse(etld.isException());
        assertFalse(etld.isWild());
        etld = EffectiveTldFinder.getEffectiveTLD("bbc.co.uk");
        assertFalse(etld.isException());
        assertFalse(etld.isWild());
        assertEquals("co.uk", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("www.bbc.co.uk");
        assertFalse(etld.isException());
        assertFalse(etld.isWild());
        assertEquals("co.uk", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("anything.uk");
        assertEquals("uk", etld.getDomain());
    }

    @Test
    public final void testExceptionEtld() throws Exception {
        EffectiveTLD etld = null;
        etld = EffectiveTldFinder.getEffectiveTLD("parliament.uk");
        assertFalse(etld.isException());
        assertFalse(etld.isWild());
        // should be parliament.uk
        assertEquals("uk", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("www.parliament.uk");
        assertFalse(etld.isException());
        assertFalse(etld.isWild());
        // should be parliament.uk
        assertEquals("uk", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("hokkaido.jp");
        assertFalse(etld.isException());
        assertFalse(etld.isWild());
        assertEquals("hokkaido.jp", etld.getDomain());
        // test a Japanese city
        etld = EffectiveTldFinder.getEffectiveTLD("www.city.kawasaki.jp");
        assertTrue(etld.isException());
        assertFalse(etld.isWild());
        assertEquals("city.kawasaki.jp", etld.getDomain());
        assertEquals("city.kawasaki.jp", EffectiveTldFinder.getAssignedDomain("www.city.kawasaki.jp"));
        assertEquals(".kawasaki.jp", EffectiveTldFinder.getAssignedDomain(".kawasaki.jp"));
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
//        assertEquals("paypal.com", ad);
    }

    @Test
    public final void testIDNDomain() throws Exception {
        String ad = null;
        ad = EffectiveTldFinder.getAssignedDomain("спб.бесплатныеобъявления.рф");
//        assertEquals("бесплатныеобъявления.рф", ad);
        ad = EffectiveTldFinder.getAssignedDomain("xn--90a1af.xn--80abbembcyvesfij3at4loa4ff.xn--p1ai");
        assertEquals("xn--80abbembcyvesfij3at4loa4ff.xn--p1ai", ad);
        // rare but possible mixed use of UTF-8 and Punycode
        ad = EffectiveTldFinder.getAssignedDomain("xn--90a1af.бесплатныеобъявления.рф");
//        assertEquals("xn--80abbembcyvesfij3at4loa4ff.xn--p1ai", ad);
    }
}
