/*
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

package crawlercommons.robots_tag.parsers;

import crawlercommons.robots_tag.*;

/**
 * Parses directives with {@link String} values (e.g.
 * {@code max-image-preview: none}).
 * <p>
 * Everything up to the first comma is considered to be part of the directive
 * value.
 * <p>
 * The directive value is trimmed, but not modified otherwise (i.e. it is not
 * transformed to uppercase or lowercase).
 */
public final class StringDirectiveParser implements DirectiveParser<String> {
    private static final StringDirectiveParser SINGLETON = new StringDirectiveParser();

    @Override
    public ParserResult<Directive<String>> parse(PreprocessedString input) {
        return input.getTail().map(tail -> {
            int endIndex = ParserUtils.findFirstComma(tail);
            String string = tail.substring(0, endIndex).trim();

            if (string.isEmpty()) {
                throw new ParserException("Failed to parse key-value directive due to missing value");
            }

            var directive = new Directive<>(input.getFirstToken(), string);
            String remainder = tail.substring(endIndex);

            return new ParserResult<>(directive, remainder);
        }).orElseThrow(() -> new ParserException("Failed to parse key-value directive due to missing value"));
    }

    public static StringDirectiveParser getSingleton() {
        return SINGLETON;
    }
}
