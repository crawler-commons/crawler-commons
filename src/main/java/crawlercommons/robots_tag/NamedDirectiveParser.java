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

/**
 * Combines a {@link DirectiveParser} with the name of a directive that it can
 * parse.
 */
public final class NamedDirectiveParser<T> {
    private final String directiveName;
    private final DirectiveParser<T> directiveParser;

    public NamedDirectiveParser(String directiveName, DirectiveParser<T> directiveParser) {
        this.directiveName = directiveName;
        this.directiveParser = directiveParser;
    }

    public String getDirectiveName() {
        return directiveName;
    }

    public DirectiveParser<T> getDirectiveParser() {
        return directiveParser;
    }
}
