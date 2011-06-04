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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.junit.Test;

public class PaidLevelDomainTest {
    
    @Test
    public final void testIPv4() throws MalformedURLException {
        assertEquals("1.2.3.4", PaidLevelDomain.getPLD("1.2.3.4"));

        URL url = new URL("http://1.2.3.4:8080/a/b/c?_queue=1");
        assertEquals("1.2.3.4", PaidLevelDomain.getPLD(url));
    }

    public final void testIPv6() throws MalformedURLException, UnknownHostException {
        InetAddress inet = InetAddress.getByName("1080:0:0:0:8:800:200c:417a");
        URL url = new URL("http", inet.getHostAddress(), 8080, "a/b/c");
        assertEquals("[1080:0:0:0:8:800:200c:417a]", PaidLevelDomain.getPLD(url));
    }

    public final void testStandardDomains() throws MalformedURLException {
        assertEquals("xxx.com", PaidLevelDomain.getPLD("xxx.com"));
        assertEquals("xxx.com", PaidLevelDomain.getPLD("www.xxx.com"));
        assertEquals("xxx.com", PaidLevelDomain.getPLD("www.zzz.xxx.com"));
        assertEquals("xxx.com", PaidLevelDomain.getPLD(new URL("https://www.zzz.xxx.com:9000/a/b?c=d")));
    }

    public final void testBizDomains() {
        assertEquals("xxx.biz", PaidLevelDomain.getPLD("xxx.biz"));
        assertEquals("xxx.biz", PaidLevelDomain.getPLD("www.xxx.biz"));
    }

    // Japan (and uk) have shortened gTLDs before the country code.
    public final void testJapaneseDomains() {
        assertEquals("xxx.co.jp", PaidLevelDomain.getPLD("xxx.co.jp"));
        assertEquals("xxx.co.jp", PaidLevelDomain.getPLD("www.xxx.co.jp"));
        assertEquals("xxx.ne.jp", PaidLevelDomain.getPLD("www.xxx.ne.jp"));
    }

    // In Germany you can have xxx.de.com
    public final void testGermanDomains() {
        assertEquals("xxx.de.com", PaidLevelDomain.getPLD("xxx.de.com"));
        assertEquals("xxx.de.com", PaidLevelDomain.getPLD("www.xxx.de.com"));
    }

    // Typical international domains look like xxx.com.it
    public final void testItalianDomains() {
        assertEquals("xxx.com.it", PaidLevelDomain.getPLD("xxx.com.it"));
        assertEquals("xxx.com.it", PaidLevelDomain.getPLD("www.xxx.com.it"));
    }

}
