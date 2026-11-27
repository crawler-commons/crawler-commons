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

public interface DirectiveParser<T> {
    /**
     * Parses the first directive (and its value, if applicable) from a
     * {@link PreprocessedString}.
     * <p>
     * This method may throw a {@link RuntimeException} if any exception is
     * thrown while parsing.
     * 
     * @param input
     *            the {@link PreprocessedString} to process
     * @return the parsed directive and the input string without the parsed
     *         directive and its value
     */
    ParserResult<Directive<T>> parse(PreprocessedString input);
}
