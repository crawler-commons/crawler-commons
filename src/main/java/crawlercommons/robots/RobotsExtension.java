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

package crawlercommons.robots;

/**
 * Known robots.txt extension directives that can be optionally enabled in
 * {@link SimpleRobotRulesParser}.
 * 
 * <p>
 * Each constant carries:
 * </p>
 * <ul>
 * <li>{@link #getDirectiveName()} &ndash; the canonical lower-case directive
 * name as it appears in a robots.txt file (e.g. {@code "llm-policy"})</li>
 * <li>{@link #isPerGroup()} &ndash; whether the directive is scoped to a
 * specific user-agent group ({@code true}) or applies globally to the entire
 * robots.txt file ({@code false})</li>
 * <li>{@link #getAliases()} &ndash; optional alternative spellings that should
 * also be recognized</li>
 * </ul>
 * 
 * <p>
 * Extensions are opt-in: they are only parsed and stored when explicitly
 * enabled via {@link SimpleRobotRulesParser#enableExtension(RobotsExtension)},
 * {@link SimpleRobotRulesParser#enableExtensions(java.util.Collection)}, or
 * {@link SimpleRobotRulesParser#enableAllExtensions()}.
 * </p>
 * 
 * @see SimpleRobotRulesParser
 * @see BaseRobotRules#getExtensionData(RobotsExtension)
 */
public enum RobotsExtension {

    /**
     * LLM-Policy directive, used to point to an llms.txt file or similar policy
     * document for AI/LLM crawlers.
     */
    LLM_POLICY("llm-policy", false),

    /**
     * Yandex Clean-Param directive, used to indicate URL parameters that should
     * be ignored when determining page uniqueness. See <a href=
     * "https://yandex.com/support/webmaster/en/robot-workings/clean-param">
     * Yandex documentation</a>.
     * 
     * Example:
     * 
     * <pre>
     * User-agent: Yandex
     * Clean-param: utm
     * 
     * Clean-param: sid&amp;sort /forum/*.php
     * </pre>
     */
    CLEAN_PARAM("clean-param", true),

    /**
     * Cloudflare Content Signals policy directive, an extension for content
     * governance rules (e.g. AI training data usage, attribution). See <a
     * href="https://blog.cloudflare.com/content-signals-policy">Cloudflare blog
     * post</a>.
     * 
     * Example:
     * 
     * <pre>
     * Content-Signal: search=yes, ai-train=no
     * </pre>
     */
    CONTENT_SIGNALS("content-signal", false, "content-signals"),

    /**
     * The &quot;Host&quot; directive was used by Yandex to indicate the main or
     * canonical host of a set of mirrored web sites. Not supported anymore. See
     * <a href=
     * "https://web.archive.org/web/20130509230548/http://help.yandex.com/webmaster/?id=1113851#1113856"
     * >Yandex.Help: webmasters, Using robots.txt, Host directive (archived
     * 2013)</a>.
     */
    HOST("host", false),

    /**
     * Google extension: Noindex directive to prevent indexing of specific URLs.
     * 
     * 2019 Google announced that "we're retiring all code that handles
     * unsupported and unpublished rules (such as noindex) on September 1,
     * 2019." (<a href=
     * "https://web.archive.org/web/20190801003327/https://webmasters.googleblog.com/2019/07/a-note-on-unsupported-rules-in-robotstxt.html"
     * >A note on unsupported rules in robots.txt</a>)
     */
    NO_INDEX("no-index", true, "noindex"),

    /**
     * Request-Rate directive, used to specify the maximum crawl rate. Supported
     * by <code>SeznamBot</code>, see <a href=
     * "https://o-seznam.cz/napoveda/vyhledavani/en/crawling-control/"
     * >Seznam.cz, Crawling control</a>.
     * 
     * Example:
     * 
     * <pre>
     * Request-rate: 1/10s            # one request every 10 seconds
     * Request-rate: 1/10s 1800-1900  # but only between 18:00 and 19:00 UTC
     * </pre>
     */
    REQUEST_RATE("request-rate", true),

    /**
     * Visit-Time directive, used to specify preferred crawling time windows.
     * 
     * Proposed 2002 by Sean Conner in <a
     * href="https://www.conman.org/people/spc/robots2.html">An Extended
     * Standard for Robot Exclusion</a>.
     * 
     * Example:
     * 
     * <pre>
     * Visit-time: 0600-0845
     * </pre>
     */
    VISIT_TIME("visit-time", true),

    /**
     * Robot-Version directive, used to indicate the robots.txt specification
     * version.
     * 
     * Proposed 2002 by Sean Conner in <a
     * href="https://www.conman.org/people/spc/robots2.html">An Extended
     * Standard for Robot Exclusion</a>.
     */
    ROBOT_VERSION("robot-version", true),

    /**
     * Comment directive, used to provide human-readable comments about the
     * rules in a user-agent group.
     * 
     * Proposed 2002 by Sean Conner in <a
     * href="https://www.conman.org/people/spc/robots2.html">An Extended
     * Standard for Robot Exclusion</a>.
     */
    COMMENT("comment", true);

    private final String _directiveName;
    private final boolean _perGroup;
    private final String[] _aliases;

    RobotsExtension(String directiveName, boolean perGroup, String... aliases) {
        _directiveName = directiveName;
        _perGroup = perGroup;
        _aliases = aliases;
    }

    /**
     * @return the canonical lower-case directive name as it appears in a
     *         robots.txt file (e.g. {@code "llm-policy"})
     */
    public String getDirectiveName() {
        return _directiveName;
    }

    /**
     * @return {@code true} if this directive is scoped to a specific user-agent
     *         group, {@code false} if it applies globally to the entire
     *         robots.txt file
     */
    public boolean isPerGroup() {
        return _perGroup;
    }

    /**
     * @return {@code true} if this directive applies globally, not scoped to
     *         any particular user-agent group
     */
    public boolean isGlobal() {
        return !_perGroup;
    }

    /**
     * @return alternative spellings / aliases for this directive (may be empty)
     */
    public String[] getAliases() {
        return _aliases;
    }
}
