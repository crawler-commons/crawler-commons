/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import crawlercommons.url.EffectiveTldFinder.EffectiveTLD;
import org.junit.Test;

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
        assertTrue(etld.isWild());
        etld = EffectiveTldFinder.getEffectiveTLD("bbc.co.uk");
        assertFalse(etld.isException());
        assertFalse(etld.isWild());
        assertEquals("co.uk", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("www.bbc.co.uk");
        assertFalse(etld.isException());
        assertFalse(etld.isWild());
        assertEquals("co.uk", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("anything.uk");
        assertEquals("anything.uk", etld.getDomain());
    }

    @Test
    public final void testExceptionEtld() throws Exception {
        EffectiveTLD etld = null;
        etld = EffectiveTldFinder.getEffectiveTLD("parliament.uk");
        assertTrue(etld.isException());
        assertFalse(etld.isWild());
        assertEquals("parliament.uk", etld.getDomain());
        etld = EffectiveTldFinder.getEffectiveTLD("www.parliament.uk");
        assertTrue(etld.isException());
        assertFalse(etld.isWild());
        assertEquals("parliament.uk", etld.getDomain());
        // domains can be assigned under hokkaido.jp except for a
        // a prescribed set of prefects (I think)
        etld = EffectiveTldFinder.getEffectiveTLD("hokkaido.jp");
        assertFalse(etld.isException());
        assertTrue(etld.isWild());
        assertEquals("hokkaido.jp", etld.getDomain());
        // test a Japanese prefect
        etld = EffectiveTldFinder.getEffectiveTLD("www.pref.hokkaido.jp");
        assertTrue(etld.isException());
        assertFalse(etld.isWild());
        assertEquals("pref.hokkaido.jp", etld.getDomain());
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
    public final void testUkDomain() throws Exception {
        String ad = null;
        ad = EffectiveTldFinder.getAssignedDomain("bbc.co.uk");
        assertEquals("bbc.co.uk", ad);
        ad = EffectiveTldFinder.getAssignedDomain("www.bbc.co.uk");
        assertEquals("bbc.co.uk", ad);
    }
}
