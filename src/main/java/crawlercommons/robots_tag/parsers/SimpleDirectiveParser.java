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
import crawlercommons.robots_tag.DirectiveParser;
import crawlercommons.robots_tag.ParserResult;
import crawlercommons.robots_tag.PreprocessedString;

import java.util.Optional;

/**
 * Parses simple directives without a value (e.g. {@code follow} or
 * {@code index}).
 */
public final class SimpleDirectiveParser implements DirectiveParser<Void> {
    private static final SimpleDirectiveParser SINGLETON = new SimpleDirectiveParser();

    @Override
    public ParserResult<Directive<Void>> parse(PreprocessedString input) {
        Directive<Void> directive = new Directive<>(input.getFirstToken(), Optional.empty());
        String remainder = input.getString().substring(input.getDelimiterIndex());
        return new ParserResult<>(directive, remainder);
    }

    public static SimpleDirectiveParser getSingleton() {
        return SINGLETON;
    }
}
