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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.junit.Ignore;
import org.junit.Test;

import crawlercommons.domains.PaidLevelDomain;

public class PaidLevelDomainTest {

    @Test
    public final void testIPv4() throws MalformedURLException {
        assertEquals("1.2.3.4", PaidLevelDomain.getPLD("1.2.3.4"));

        URL url = new URL("http://1.2.3.4:8080/a/b/c?_queue=1");
        assertEquals("1.2.3.4", PaidLevelDomain.getPLD(url));
    }

    @Test
    public void testInvalidFQDN() {
    	assertEquals("blah", PaidLevelDomain.getPLD("blah"));
    	assertEquals("1.2.3", PaidLevelDomain.getPLD("1.2.3"));
    	assertEquals("me.i", PaidLevelDomain.getPLD("me.i"));
    }
    
    @Test
    public final void testIPv6() throws MalformedURLException, UnknownHostException {
        InetAddress inet = InetAddress.getByName("1080:0:0:0:8:800:200c:417a");
        URL url = new URL("http", inet.getHostAddress(), 8080, "a/b/c");
        assertEquals("[1080:0:0:0:8:800:200c:417a]", PaidLevelDomain.getPLD(url));
    }

    @Test
    public final void testStandardDomains() throws MalformedURLException {
        assertEquals("domain.com", PaidLevelDomain.getPLD("domain.com"));
        assertEquals("domain.com", PaidLevelDomain.getPLD("www.domain.com"));
        assertEquals("domain.com", PaidLevelDomain.getPLD("www.zzz.domain.com"));
        assertEquals("domain.com", PaidLevelDomain.getPLD(new URL("https://www.zzz.domain.com:9000/a/b?c=d")));
    }

    @Test
    public final void testBizDomains() {
        assertEquals("xxx.biz", PaidLevelDomain.getPLD("xxx.biz"));
        assertEquals("xxx.biz", PaidLevelDomain.getPLD("www.xxx.biz"));
    }

    // Japan (and uk) have shortened gTLDs before the country code.
    @Test
    public final void testJapaneseDomains() {
        assertEquals("xxx.co.jp", PaidLevelDomain.getPLD("xxx.co.jp"));
        assertEquals("xxx.co.jp", PaidLevelDomain.getPLD("www.xxx.co.jp"));
        assertEquals("xxx.ne.jp", PaidLevelDomain.getPLD("www.xxx.ne.jp"));
    }

    // In Germany you can have xxx.de.com
    @Test
    public final void testGermanDomains() {
        assertEquals("xxx.de.com", PaidLevelDomain.getPLD("xxx.de.com"));
        assertEquals("xxx.de.com", PaidLevelDomain.getPLD("www.xxx.de.com"));
    }

    // Typical international domains look like xxx.it. So xxx.com.it is
    // actually the xxx subdomain for com.it
    @Test
    public final void testItalianDomains() {
        assertEquals("xxx.it", PaidLevelDomain.getPLD("xxx.it"));
        assertEquals("xxx.it", PaidLevelDomain.getPLD("www.xxx.it"));
        assertEquals("com.it", PaidLevelDomain.getPLD("xxx.com.it"));
    }
    
    @Test
    public final void testFinnishDomains() {
        assertEquals("fi.com", PaidLevelDomain.getPLD("www.fi.com"));
    }
    
    // TODO enable this test when getPLD uses new TLD support to exclude
    // private domains (See https://github.com/crawler-commons/crawler-commons/pull/186) 
    @Ignore
    @Test
    public final void testPrivateDomains() {
    	assertEquals("blogspot.com", PaidLevelDomain.getPLD("myblog.blogspot.com"));
    }

}
