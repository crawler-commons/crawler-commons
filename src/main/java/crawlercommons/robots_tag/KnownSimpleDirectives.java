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
 * Common simple directives without values.
 */
public final class KnownSimpleDirectives {
    /*
        Sources and references are listed in KnownDirectiveParsers.
    */

    public static final Directive<Void> ALL = new Directive<>("all");
    public static final Directive<Void> ARCHIVE = new Directive<>("archive");
    public static final Directive<Void> FOLLOW = new Directive<>("follow");
    public static final Directive<Void> INDEX = new Directive<>("index");
    public static final Directive<Void> INDEX_IF_EMBEDDED = new Directive<>("indexifembedded");
    public static final Directive<Void> NONE = new Directive<>("none");
    public static final Directive<Void> NO_AI = new Directive<>("noai");
    public static final Directive<Void> NO_ARCHIVE = new Directive<>("noarchive");
    public static final Directive<Void> NO_CACHE = new Directive<>("nocache");
    public static final Directive<Void> NO_FOLLOW = new Directive<>("nofollow");
    public static final Directive<Void> NO_IMAGE_AI = new Directive<>("noimageai");
    public static final Directive<Void> NO_IMAGE_INDEX = new Directive<>("noimageindex");
    public static final Directive<Void> NO_INDEX = new Directive<>("noindex");
    public static final Directive<Void> NO_SITELINKS_SEARCH_BOX = new Directive<>("nositelinkssearchbox");
    public static final Directive<Void> NO_SNIPPET = new Directive<>("nosnippet");
    public static final Directive<Void> NO_TRANSLATE = new Directive<>("notranslate");
}
