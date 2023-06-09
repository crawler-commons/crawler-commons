/**
 * Copyright 2023 Crawler-Commons
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
package crawlercommons.sitemaps.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class PageMapsTest {

    @Test
    public void testPageMapAttributesEquals() {
        PageMap a = new PageMap();
        PageMapDataObject da = new PageMapDataObject("test", "a");
        da.addAttribute("foo", "bar");
        a.addDataObject(da);
        assertEquals(a, a);
        assertNotNull(a.toString());
        assertEquals(1, a.getPageMapDataObjects().size());
        assertEquals("test", a.getPageMapDataObjects().get(0).getType());
        assertEquals("a", a.getPageMapDataObjects().get(0).getId());
        assertEquals("bar", a.getPageMapDataObjects().get(0).getAttribute("foo"));

        PageMap b = new PageMap();
        PageMapDataObject db = new PageMapDataObject("test", "a");
        db.addAttribute("foo", "bar");
        b.addDataObject(db);
        assertEquals(da, db);
        assertEquals(a, b);
        db.addAttribute("hello", "world");
        assertNotEquals(da, db);
        assertNotEquals(a, b);
        assertEquals(b, b);
        assertNotNull(a.toString());
        assertEquals(1, b.getPageMapDataObjects().size());
        assertEquals("test", b.getPageMapDataObjects().get(0).getType());
        assertEquals("a", b.getPageMapDataObjects().get(0).getId());
        assertEquals("bar", b.getPageMapDataObjects().get(0).getAttribute("foo"));
        assertEquals("world", b.getPageMapDataObjects().get(0).getAttribute("hello"));
        assertEquals(1, b.asMap().size());
        assertNotNull(b.asMap().get("test::a"));
        assertEquals(2, b.asMap().get("test::a").length);

        PageMap c = new PageMap();
        PageMapDataObject dc = new PageMapDataObject("test", "c");
        dc.addAttribute("abc", "xyz");
        c.addDataObject(dc);
        assertEquals(c, c);
        assertNotEquals(a, c);
        assertNotNull(a.toString());
        assertEquals(1, c.getPageMapDataObjects().size());
        assertEquals("test", c.getPageMapDataObjects().get(0).getType());
        assertEquals("c", c.getPageMapDataObjects().get(0).getId());
        assertEquals("xyz", c.getPageMapDataObjects().get(0).getAttribute("abc"));
    }
}