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

package crawlercommons.robots_tag;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Parses unambiguous directive strings.
 * <p>
 * A directive string is unambiguous if it does not contain any colons. As such,
 * unambiguous strings can neither contain any key-value directives, nor can
 * they contain directives that only apply to specific robots.
 * <p>
 * An unambiguous string can be treated as a string of comma-separated
 * directives.
 */
public final class UnambiguousStringParser {
    /**
     * Parses unambiguous directive strings.
     * <p>
     * Nothing is done to ensure that the input is unambiguous.
     * <p>
     * The result may contain duplicates.
     */
    public static List<Directive<?>> parse(String input) {
        String[] strings = input.toLowerCase(Locale.ROOT) //"foo, bar, "
            .split(","); //["foo", " bar", " "]

        return Arrays.stream(strings)
            .map(String::trim) //["foo", "bar", ""]
            .filter(string -> !string.isEmpty()) //["foo", "bar"]
            .map(Directive::new)
            .collect(Collectors.toList());
    }
}
