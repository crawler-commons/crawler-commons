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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class SimpleJsonArrayParserTest {

    @Test
    public void testCommentStringElement() {
        List<Object> result = SimpleJsonArrayParser.parse("[ \"a top-level comment\" ]");
        assertEquals(1, result.size());
        assertInstanceOf(String.class, result.get(0));
        assertEquals("a top-level comment", result.get(0));
    }

    @Test
    public void testObjectWithStringBooleanNullValues() {
        List<Object> result = SimpleJsonArrayParser.parse("[ { \"input\": \"http://x\", \"failure\": true, \"flag\": false, \"base\": null } ]");
        assertEquals(1, result.size());
        assertInstanceOf(Map.class, result.get(0));
        @SuppressWarnings("unchecked")
        Map<String, Object> entry = (Map<String, Object>) result.get(0);
        assertEquals("http://x", entry.get("input"));
        assertEquals(Boolean.TRUE, entry.get("failure"));
        assertEquals(Boolean.FALSE, entry.get("flag"));
        assertTrue(entry.containsKey("base"));
        assertNull(entry.get("base"));
    }

    @Test
    public void testStringEscapes() {
        // Backslash-u-XXXX (here 0041 == 'A'), \/ and \" plus the simple
        // escapes. The backslash-u token is assembled at runtime so that the
        // Java compiler's own Unicode-escape preprocessing does not consume it.
        String bsu = "\\" + "u0041";
        List<Object> result = SimpleJsonArrayParser.parse("[ { \"v\": \"tab\\tquote\\\"slash\\/u" + bsu + "end\\n\" } ]");
        @SuppressWarnings("unchecked")
        Map<String, Object> entry = (Map<String, Object>) result.get(0);
        assertEquals("tab\tquote\"slash/uAend\n", entry.get("v"));
    }

    @Test
    public void testEmptyArray() {
        List<Object> result = SimpleJsonArrayParser.parse("[]");
        assertEquals(0, result.size());
    }

    @Test
    public void testMixedArray() {
        List<Object> result = SimpleJsonArrayParser.parse("[ \"comment\", { \"k\": \"v\" }, \"comment2\" ]");
        assertEquals(3, result.size());
        assertEquals("comment", result.get(0));
        assertInstanceOf(Map.class, result.get(1));
        assertEquals("comment2", result.get(2));
    }
}
