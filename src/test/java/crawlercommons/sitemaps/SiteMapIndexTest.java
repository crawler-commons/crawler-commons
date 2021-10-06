/**
 * Copyright 2020 Crawler-Commons
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

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SiteMapIndexTest {

    @Test
    public void testDeduplication() {
        SiteMapIndex index = new SiteMapIndex();
        index.addSitemap(new SiteMap("https://example.com/sitemap1.xml", "2020-06-18"));
        index.addSitemap(new SiteMap("https://example.com/sitemap2.xml", "2020-06-19"));
        index.addSitemap(new SiteMap("https://example.com/sitemap1.xml", "2020-06-19"));
        assertEquals(3, index.getSitemaps().size());
        assertEquals(2, index.getSitemaps(true).size());
        assertEquals(3, index.getSitemaps(false).size());
    }

    @Test
    public void testNPE() {
        SiteMapIndex index = new SiteMapIndex();
        index.addSitemap(new SiteMap("INVALID", "2020-06-18"));
        index.addSitemap(new SiteMap("https://example.com/sitemap1.xml", "2020-06-18"));
        try {
            assertNotNull(index.getSitemap(new URL("https://example.com/sitemap1.xml")));
        } catch (MalformedURLException e) {
            // URL is valid
        }
        assertNull(index.getSitemap(null));
    }
}
