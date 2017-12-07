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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuffixTrie<V> {

    protected Node<V> root;

    protected static class Node<V> {
        /** sorted list of characters leading to children */
        char[] chars = {};
        @SuppressWarnings("unchecked")
        Node<V>[] children = new Node[0];
        V value = null;

        public Node<V> getChild(char c) {
            int pos = Arrays.binarySearch(chars, c);
            if (pos >= 0) {
                return children[pos];
            }
            return null;
        }

        public Node<V> addChild(char c, V value) {
            int pos = Arrays.binarySearch(chars, c);
            Node<V> child;
            if (pos >= 0) {
                child = children[pos];
            } else {
                child = new Node<V>();
                char[] new_chars = new char[chars.length + 1];
                @SuppressWarnings("unchecked")
                Node<V>[] new_children = new Node[children.length + 1];
                pos = -pos - 1;
                new_chars[pos] = c;
                new_children[pos] = child;
                for (int i = 0; i < chars.length; i++) {
                    if (i < pos) {
                        new_chars[i] = chars[i];
                        new_children[i] = children[i];
                    } else if (i >= pos) {
                        new_chars[i + 1] = chars[i];
                        new_children[i + 1] = children[i];
                    }
                }
                this.chars = new_chars;
                this.children = new_children;
            }
            if (value != null) {
                child.value = value;
            }
            return child;
        }
    }

    public SuffixTrie() {
        root = new Node<V>();
    }

    /**
     * Insert a string and an associated value into the trie. If the string is
     * already present in the trie, the existing value is replaced by the new
     * value.
     * 
     * @param suffix
     *            suffix string inserted into trie
     * @param value
     *            value associated with string
     * @return previous value associated with string or null if there was no
     *         previous value
     */
    public V put(String suffix, V value) {
        V res = null;
        if (suffix.isEmpty()) {
            res = (V) root.value;
            root.value = value;
            return res;
        }
        Node<V> node = root;
        for (int i = suffix.length() - 1; i > 0; i--) {
            node = node.addChild(suffix.charAt(i), null);
        }
        Node<V> resNode = node.getChild(suffix.charAt(0));
        if (resNode == null) {
            node.addChild(suffix.charAt(0), value);
            return null;
        } else {
            node.addChild(suffix.charAt(0), value);
            return resNode.value;
        }
    }

    /**
     * Get value associated with suffix string in trie.
     * 
     * @param suffix
     *            suffix string searched in trie
     * @return value if suffix is found in trie, null otherwise
     */
    public V get(String suffix) {
        if (suffix.isEmpty()) {
            return root.value;
        }
        ;
        Node<V> node = root;
        for (int i = suffix.length() - 1; i >= 0; i--) {
            node = node.getChild(suffix.charAt(i));
            if (node == null) {
                return null;
            }
        }
        return node.value;
    }

    /**
     * Checks whether trie contains a suffix string.
     * 
     * @param suffix
     *            suffix string searched in trie
     * @return true if suffix is found in trie
     */
    public boolean contains(String suffix) {
        return get(suffix) != null;
    }

    /**
     * Wrapper for results when a string is checked for suffixes contained in
     * the suffix trie.
     */
    public static class LookupResult<V> {
        /**
         * Offset from beginning of lookup string, the matched suffix is the
         * substring from offset until end of lookup string.
         */
        int offset;
        /** Value associated with suffix string. */
        V value;

        public LookupResult(int offset, V value) {
            this.offset = offset;
            this.value = value;
        }
    }

    /**
     * Match the longest suffix of a string contained in trie.
     * 
     * @param string
     *            to be checked for a contained (longest) suffix
     * @return lookup result or null if no suffix is found
     */
    protected LookupResult<V> getLongestSuffix(String string) {
      Node<V> node = root;
      V resValue = null;
      int offset = -1;
      if (node.value != null) {
          // trie contains empty string
          offset = string.length();
          resValue = node.value;
      }
      for (int i = string.length() - 1; i >= 0; i--) {
          node = node.getChild(string.charAt(i));
          if (node == null) {
              break;
          } else if (node.value != null) {
              offset = i;
              resValue = node.value;
          }
      }
      if (offset != -1) {
          return new LookupResult<>(offset, resValue);
      }
      return null;
    }

    /**
     * Match all suffixes of a string contained in trie.
     * 
     * @param string
     *            string to be checked for suffixes contained in trie
     * @return list of suffix lookup results, from shortest to longest
     */
    protected List<LookupResult<V>> getSuffixes(String string) {
        List<LookupResult<V>> res = new ArrayList<>();
        Node<V> node = root;
        if (node.value != null) {
            res.add(new LookupResult<V>(string.length(), node.value));
        }
        for (int i = string.length() - 1; i >= 0; i--) {
            node = node.getChild(string.charAt(i));
            if (node == null) {
                break;
            } else if (node.value != null) {
                res.add(new LookupResult<V>(i, node.value));
            }
        }
        return res;
    }
}
