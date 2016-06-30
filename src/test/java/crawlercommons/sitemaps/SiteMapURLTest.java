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

package crawlercommons.sitemaps;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created on 13/10/2014.
 * 
 */
@RunWith(JUnit4.class)
public class SiteMapURLTest {
    SiteMapURL siteMapURL;

    @Before
    public void setUp() throws Exception {
        siteMapURL = new SiteMapURL("http://example.com", true);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSetPriority() {
        assertEquals(SiteMapURL.defaultPriority, siteMapURL.getPriority(), 0);
        siteMapURL.setPriority(0.6);
        assertEquals(0.6, siteMapURL.getPriority(), 0);

        siteMapURL.setPriority(1.1);
        assertEquals(SiteMapURL.defaultPriority, siteMapURL.getPriority(), 0);

        siteMapURL.setPriority("0.6");
        assertEquals(0.6, siteMapURL.getPriority(), 0);

        siteMapURL.setPriority("BAD VALUE");
        assertEquals(SiteMapURL.defaultPriority, siteMapURL.getPriority(), 0);

        siteMapURL.setPriority("0.6");
        assertEquals(0.6, siteMapURL.getPriority(), 0);
        siteMapURL.setPriority("1.1");
        assertEquals(SiteMapURL.defaultPriority, siteMapURL.getPriority(), 0);
    }
}
