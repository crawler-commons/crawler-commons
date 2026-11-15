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

import crawlercommons.robots_tag.Directive;
import crawlercommons.robots_tag.ParserResult;
import crawlercommons.robots_tag.PreprocessedString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("SimpleDirectiveParser")
class SimpleDirectiveParserTest {
    static final SimpleDirectiveParser PARSER = SimpleDirectiveParser.getSingleton();

    @ParameterizedTest
    @DisplayName("should work with input that only contains a single directive")
    @ValueSource(strings = {
        "foo",
        "foo-123",
        "foo_123"
    })
    void singleDirective(String input) {
        var expected = new ParserResult<>(new Directive<>(input, Optional.empty()), "");
        var actual = PARSER.parse(new PreprocessedString(input));
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @DisplayName("should work with input that contains multiple directives")
    @ValueSource(strings = {
        "foo",
        "foo, bar, baz",
        "foo: bar, baz",
        "foo, bar: baz",
        "all" //The input ends up containing two "all" directives.
    })
    void multipleDirectives(String suffix) {
        String input = "all, " + suffix;
        var expected = new ParserResult<>(new Directive<>("all", Optional.empty()), ", " + suffix);
        var actual = PARSER.parse(new PreprocessedString(input));
        assertEquals(expected, actual);
    }
}
