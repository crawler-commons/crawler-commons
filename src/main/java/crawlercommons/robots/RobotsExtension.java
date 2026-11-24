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
 * <li>{@link #getScope()} &ndash; whether the directive is scoped to a specific
 * user-agent group ({@link Scope#GROUP}), applies globally to the entire
 * robots.txt file ({@link Scope#GLOBAL}), or is scoped to a user-agent group
 * with a fallback to the values outside of any group
 * ({@link Scope#GROUP_WITH_GLOBAL_FALLBACK})</li>
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
    LLM_POLICY("llm-policy", Scope.GLOBAL),

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
    CLEAN_PARAM("clean-param", Scope.GROUP),

    /**
     * Cloudflare Content Signals policy directive, an extension for content
     * governance rules (e.g. AI training data usage, attribution). See <a
     * href="https://blog.cloudflare.com/content-signals-policy">Cloudflare blog
     * post</a> and <a href="https://contentsignals.org/">contentsignals.org</a>.
     * 
     * <p>
     * The directive is written inside a user-agent group, so that a site may
     * express different content signals for different crawlers, and is scoped
     * to that group (see {@link #isPerGroup()}).
     * </p>
     * 
     * Example:
     * 
     * <pre>
     * User-agent: *
     * Content-Signal: search=yes, ai-train=no
     * Disallow:
     * 
     * User-agent: mybot
     * Content-Signal: search=yes, ai-train=yes
     * Disallow:
     * </pre>
     * 
     * <p>
     * The value is stored unparsed (see {@link RobotsExtensionData}):
     * splitting it into signal/value pairs and handling the optional
     * path-prefix form ({@code Content-Signal: /images/ ai-train=no}) is left
     * to the caller.
     * </p>
     */
    CONTENT_SIGNALS("content-signal", Scope.GROUP, "content-signals"),

    /**
     * The &quot;Host&quot; directive was used by Yandex to indicate the main or
     * canonical host of a set of mirrored web sites. Not supported anymore. See
     * <a href=
     * "https://web.archive.org/web/20130509230548/http://help.yandex.com/webmaster/?id=1113851#1113856"
     * >Yandex.Help: webmasters, Using robots.txt, Host directive (archived
     * 2013)</a>.
     */
    HOST("host", Scope.GLOBAL),

    /**
     * Google extension: Noindex directive to prevent indexing of specific URLs.
     * 
     * 2019 Google announced that "we're retiring all code that handles
     * unsupported and unpublished rules (such as noindex) on September 1,
     * 2019." (<a href=
     * "https://web.archive.org/web/20190801003327/https://webmasters.googleblog.com/2019/07/a-note-on-unsupported-rules-in-robotstxt.html"
     * >A note on unsupported rules in robots.txt</a>)
     */
    NO_INDEX("no-index", Scope.GROUP, "noindex"),

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
    REQUEST_RATE("request-rate", Scope.GROUP),

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
    VISIT_TIME("visit-time", Scope.GROUP),

    /**
     * Robot-Version directive, used to indicate the robots.txt specification
     * version.
     * 
     * Proposed 2002 by Sean Conner in <a
     * href="https://www.conman.org/people/spc/robots2.html">An Extended
     * Standard for Robot Exclusion</a>.
     */
    ROBOT_VERSION("robot-version", Scope.GROUP),

    /**
     * Comment directive, used to provide human-readable comments about the
     * rules in a user-agent group.
     * 
     * Proposed 2002 by Sean Conner in <a
     * href="https://www.conman.org/people/spc/robots2.html">An Extended
     * Standard for Robot Exclusion</a>.
     */
    COMMENT("comment", Scope.GROUP),

    /**
     * License directive of the <a href="https://rslstandard.org/rsl">Really
     * Simple Licensing (RSL)</a> standard, pointing to an RSL license document
     * which governs the use of the site's content. See <a href=
     * "https://rslstandard.org/rsl#_4-4-robots-txt-license-association">RSL,
     * section 4.4 robots.txt License Association</a>.
     * 
     * <p>
     * The directive may be specified globally (outside of any user-agent group,
     * that is above the first {@code User-agent} line), in which case it
     * applies to all clients, or within a user-agent group, in which case it
     * applies only to the clients selecting this group. If the selected group
     * contains {@code License} directives, the global ones are ignored,
     * otherwise the global ones apply (see
     * {@link Scope#GROUP_WITH_GLOBAL_FALLBACK}).
     * </p>
     * 
     * <p>
     * Multiple {@code License} directives are allowed, each providing an
     * additional candidate license. The value must be an absolute URL. The
     * parser stores the value as an absolute URL: relative URLs are resolved
     * against the URL of the robots.txt file, values which cannot be resolved
     * into a URL with a host name are skipped.
     * </p>
     * 
     * Example:
     * 
     * <pre>
     * License: https://example.com/license.xml
     * 
     * User-agent: ExampleBot
     * License: https://example.com/examplebot-license.xml
     * Allow: /
     * 
     * User-agent: *
     * Allow: /
     * </pre>
     */
    LICENSE("license", Scope.GROUP_WITH_GLOBAL_FALLBACK);

    /**
     * Scope of a robots.txt extension directive, defining which occurrences of
     * the directive in a robots.txt file apply to a crawler.
     */
    public enum Scope {

        /**
         * The directive is a statement about the site as a whole and applies to
         * all crawlers. Its values are collected wherever the directive appears
         * in the file, regardless of user-agent groups.
         */
        GLOBAL,

        /**
         * The directive is scoped to a user-agent group: only the values of the
         * group(s) selected for the crawler are collected, following the group
         * matching rules of <a
         * href="https://www.rfc-editor.org/rfc/rfc9309.html">RFC 9309</a>.
         * Values outside of any group, that is before the first
         * {@code User-agent} line, are not collected.
         */
        GROUP,

        /**
         * The directive is scoped to a user-agent group as with {@link #GROUP},
         * but may also be specified outside of any group, that is before the
         * first {@code User-agent} line. If the group(s) selected for the
         * crawler contain the directive, the values of the selected group(s)
         * are collected and the values outside of any group are ignored.
         * Otherwise, the values outside of any group are used as fallback.
         */
        GROUP_WITH_GLOBAL_FALLBACK
    }

    private final String _directiveName;
    private final Scope _scope;
    private final String[] _aliases;

    RobotsExtension(String directiveName, Scope scope, String... aliases) {
        _directiveName = directiveName;
        _scope = scope;
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
     * @return the scope of this directive, see {@link Scope}
     */
    public Scope getScope() {
        return _scope;
    }

    /**
     * Whether this directive is scoped to a user-agent group. This is the case
     * for the scopes {@link Scope#GROUP} and
     * {@link Scope#GROUP_WITH_GLOBAL_FALLBACK}.
     * 
     * <p>
     * The values of a per-group directive are collected following the same
     * group matching rules as the {@code Allow} and {@code Disallow} rules of
     * <a href="https://www.rfc-editor.org/rfc/rfc9309.html">RFC 9309</a>: a
     * group matching the crawler by name supersedes the wildcard group, and
     * the values of all groups matching the crawler are merged. The values of
     * a global directive are a statement about the site as a whole and are
     * collected wherever the directive appears in the file.
     * </p>
     * 
     * <p>
     * Note that a per-group directive placed outside of any group, that is
     * before the first {@code User-agent} line, is not collected, as it is not
     * part of any group &ndash; unless the directive has a global fallback (see
     * {@link #hasGlobalFallback()}).
     * </p>
     * 
     * @return {@code true} if this directive is scoped to a specific user-agent
     *         group, {@code false} if it applies globally to the entire
     *         robots.txt file
     */
    public boolean isPerGroup() {
        return _scope != Scope.GLOBAL;
    }

    /**
     * @return {@code true} if this directive applies globally, not scoped to
     *         any particular user-agent group
     */
    public boolean isGlobal() {
        return _scope == Scope.GLOBAL;
    }

    /**
     * @return {@code true} if this directive is scoped to a user-agent group
     *         but falls back to the values specified outside of any group if
     *         the group(s) selected for the crawler do not contain the
     *         directive, see {@link Scope#GROUP_WITH_GLOBAL_FALLBACK}
     */
    public boolean hasGlobalFallback() {
        return _scope == Scope.GROUP_WITH_GLOBAL_FALLBACK;
    }

    /**
     * @return alternative spellings / aliases for this directive (may be empty)
     */
    public String[] getAliases() {
        return _aliases;
    }
}
