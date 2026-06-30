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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tiny, dependency-free JSON reader sufficient only for the WPT URL test corpus
 * vendored under {@code src/test/resources/url-utils/wpt-urltestdata.json}.
 * <p>
 * The corpus is a single top-level array whose elements are either:
 * <ul>
 * <li>a JSON string (a free-standing comment), or</li>
 * <li>a flat JSON object whose values are only string, boolean or {@code null}
 * (no nested objects or arrays).</li>
 * </ul>
 * This parser is intentionally minimal: it is strict enough to parse the real
 * file but makes no attempt to gracefully handle malformed JSON.
 */
public final class SimpleJsonArrayParser {

    private final String src;
    private int pos;

    private SimpleJsonArrayParser(String src) {
        this.src = src;
    }

    /**
     * Parse the given JSON text representing a top-level array.
     * 
     * @param json
     *            the JSON document text
     * @return a list whose elements are either {@link String} (top-level
     *         comment) or {@link Map}{@code <String, Object>} (an entry, with
     *         values of type {@link String}, {@link Boolean} or {@code null})
     */
    public static List<Object> parse(String json) {
        SimpleJsonArrayParser p = new SimpleJsonArrayParser(json);
        p.skipWhitespace();
        List<Object> result = p.parseArray();
        p.skipWhitespace();
        if (p.pos != p.src.length()) {
            throw new IllegalStateException("Trailing content at position " + p.pos);
        }
        return result;
    }

    private List<Object> parseArray() {
        expect('[');
        List<Object> list = new ArrayList<>();
        skipWhitespace();
        if (peek() == ']') {
            pos++;
            return list;
        }
        while (true) {
            skipWhitespace();
            list.add(parseValue());
            skipWhitespace();
            char c = next();
            if (c == ']') {
                break;
            }
            if (c != ',') {
                throw new IllegalStateException("Expected ',' or ']' at position " + (pos - 1));
            }
        }
        return list;
    }

    private Map<String, Object> parseObject() {
        expect('{');
        Map<String, Object> map = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return map;
        }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            map.put(key, parseValue());
            skipWhitespace();
            char c = next();
            if (c == '}') {
                break;
            }
            if (c != ',') {
                throw new IllegalStateException("Expected ',' or '}' at position " + (pos - 1));
            }
        }
        return map;
    }

    private Object parseValue() {
        char c = peek();
        switch (c) {
            case '"':
            return parseString();
            case '{':
            return parseObject();
            case '[':
            return parseArray();
            case 't':
            expectLiteral("true");
            return Boolean.TRUE;
            case 'f':
            expectLiteral("false");
            return Boolean.FALSE;
            case 'n':
            expectLiteral("null");
            return null;
            default:
            throw new IllegalStateException("Unexpected character '" + c + "' at position " + pos);
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = next();
            if (c == '"') {
                break;
            }
            if (c == '\\') {
                char e = next();
                switch (e) {
                    case '"':
                    sb.append('"');
                        break;
                    case '\\':
                    sb.append('\\');
                        break;
                    case '/':
                    sb.append('/');
                        break;
                    case 'b':
                    sb.append('\b');
                        break;
                    case 'f':
                    sb.append('\f');
                        break;
                    case 'n':
                    sb.append('\n');
                        break;
                    case 'r':
                    sb.append('\r');
                        break;
                    case 't':
                    sb.append('\t');
                        break;
                    case 'u':
                    String hex = src.substring(pos, pos + 4);
                    sb.append((char) Integer.parseInt(hex, 16));
                    pos += 4;
                        break;
                    default:
                    throw new IllegalStateException("Invalid escape '\\" + e + "' at position " + (pos - 1));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private void expectLiteral(String literal) {
        if (!src.regionMatches(pos, literal, 0, literal.length())) {
            throw new IllegalStateException("Expected '" + literal + "' at position " + pos);
        }
        pos += literal.length();
    }

    private void expect(char c) {
        char actual = next();
        if (actual != c) {
            throw new IllegalStateException("Expected '" + c + "' but found '" + actual + "' at position " + (pos - 1));
        }
    }

    private char peek() {
        return src.charAt(pos);
    }

    private char next() {
        return src.charAt(pos++);
    }

    private void skipWhitespace() {
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                pos++;
            } else {
                break;
            }
        }
    }
}
