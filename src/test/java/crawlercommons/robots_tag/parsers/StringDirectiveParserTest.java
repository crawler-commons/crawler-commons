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

import crawlercommons.robots_tag.DirectiveParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("StringDirectiveParser")
class StringDirectiveParserTest implements KeyValueDirectiveParserTest<String> {
    @Override
    public DirectiveParser<String> provideDirectiveParser() {
        return StringDirectiveParser.getSingleton();
    }

    @Override
    public Stream<Arguments> provideTestArguments() {
        return Stream.of(arguments("max-image-preview", "none", "none"),
        // The directive value should not be transformed to uppercase or
        // lowercase:
                        arguments("message", "Hello World!", "Hello World!"));
    }
}
