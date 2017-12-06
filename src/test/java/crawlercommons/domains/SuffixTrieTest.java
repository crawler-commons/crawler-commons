/**
 * Copyright 2017 Crawler-Commons
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

import java.util.List;

import org.junit.Test;

public class SuffixTrieTest {

    @Test
    public final void testSuffixTrie() throws Exception {
        SuffixTrie<Boolean> trie = new SuffixTrie<>();
        String[] strings = { "www.example.com", "subdomain.example.com", "example.com", "co.uk", "com.ac", "com" };
        for (String s : strings) {
            assertFalse(trie.contains(s));
            trie.put(s, true);
            assertTrue(trie.contains(s));
        }
        for (String s : strings) {
            assertTrue(trie.contains(s));
        }
        assertFalse(trie.contains(""));
        assertEquals(4, trie.getLongestSuffix("www.subdomain.example.com").offset);
        // insert empty string and test again
        trie.put("", true);
        assertTrue(trie.contains(""));
        assertEquals(0, trie.getLongestSuffix("").offset);
        // test whether all suffixes contained in string and trie are found
        List<SuffixTrie.LookupResult<Boolean>> suffixes = trie.getSuffixes("www.subdomain.example.com");
        assertEquals(4, suffixes.size());
        assertEquals(25, suffixes.get(0).offset);
        assertEquals(22, suffixes.get(1).offset);
        assertEquals(14, suffixes.get(2).offset);
        assertEquals(4, suffixes.get(3).offset);
    }
}
