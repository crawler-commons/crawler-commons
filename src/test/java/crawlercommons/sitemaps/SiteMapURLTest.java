/**
 * Copyright 2019 Crawler-Commons
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Created on 13/10/2014. */
public class SiteMapURLTest {
    private SiteMapURL siteMapURL = new SiteMapURL("http://example.com", true);

    @Test
    public void testSetPriority() {
        assertEquals(SiteMapURL.DEFAULT_PRIORITY, siteMapURL.getPriority(), 0);
        siteMapURL.setPriority(0.6);
        assertEquals(0.6, siteMapURL.getPriority(), 0);

        siteMapURL.setPriority(1.1);
        assertEquals(SiteMapURL.DEFAULT_PRIORITY, siteMapURL.getPriority(), 0);

        siteMapURL.setPriority("0.6");
        assertEquals(0.6, siteMapURL.getPriority(), 0);

        siteMapURL.setPriority("BAD VALUE");
        assertEquals(SiteMapURL.DEFAULT_PRIORITY, siteMapURL.getPriority(), 0);

        siteMapURL.setPriority("0.6");
        assertEquals(0.6, siteMapURL.getPriority(), 0);
        siteMapURL.setPriority("1.1");
        assertEquals(SiteMapURL.DEFAULT_PRIORITY, siteMapURL.getPriority(), 0);

        siteMapURL.setPriority("NaN");
        assertEquals(SiteMapURL.DEFAULT_PRIORITY, siteMapURL.getPriority(), 0);
        siteMapURL.setPriority(Double.NaN);
        assertEquals(SiteMapURL.DEFAULT_PRIORITY, siteMapURL.getPriority(), 0);
        siteMapURL.setPriority("Infinity");
        assertEquals(SiteMapURL.DEFAULT_PRIORITY, siteMapURL.getPriority(), 0);
        siteMapURL.setPriority(Double.POSITIVE_INFINITY);
        assertEquals(SiteMapURL.DEFAULT_PRIORITY, siteMapURL.getPriority(), 0);
        siteMapURL.setPriority("-Infinity");
        assertEquals(SiteMapURL.DEFAULT_PRIORITY, siteMapURL.getPriority(), 0);
        siteMapURL.setPriority(Double.NEGATIVE_INFINITY);
        assertEquals(SiteMapURL.DEFAULT_PRIORITY, siteMapURL.getPriority(), 0);
    }
}
