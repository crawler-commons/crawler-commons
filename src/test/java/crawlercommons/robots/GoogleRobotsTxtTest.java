/**
 * Copyright 2022 Crawler-Commons, 2019 Google LLC
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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Port of the unit tests of
 * <a href="https://github.com/google/robotstxt">Google Robots.txt Parser and
 * Matcher Library</a>
 * 
 * <blockquote> This file tests the robots.txt parsing and matching code found
 * in robots.cc against the current Robots Exclusion Protocol (REP) RFC.
 * https://www.rfc-editor.org/rfc/rfc9309.html </blockquote>
 * 
 * Line numbers refer to <a href=
 * "https://github.com/google/robotstxt/blob/455b1583103d13ad88fe526bc058d6b9f3309215/robots_test.cc"
 * >robots_test.cc (455b158)</a>.
 */
public class GoogleRobotsTxtTest {

    boolean isUserAgentAllowed(String robotstxt, String userAgent, String url) {
        Collection<String> agentNames = new ArrayList<>();
        agentNames.add(userAgent.toLowerCase());
        BaseRobotRules rules = SimpleRobotRulesParserTest.createRobotRules(agentNames, robotstxt, true);
        return rules.isAllowed(url);
    }

    boolean isValidUserAgentToObey(String userAgent) {
        return SimpleRobotRulesParser.isValidUserAgentToObey(userAgent);
    }

    /** Google-specific: system test. */
    @Test
    void GoogleOnlySystemTest() {
        String robotstxt = "user-agent: FooBot\ndisallow: /\n";

        /*
         * robots_test.cc line 42
         * 
         * EXPECT_TRUE(IsUserAgentAllowed("", "FooBot", ""))
         * 
         * Empty robots.txt: everything allowed.
         */
        assertTrue(isUserAgentAllowed("", "FooBot", ""));

        /*
         * robots_test.cc line 45
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "", ""))
         * 
         * Empty user-agent to be matched: everything allowed.
         */
        assertTrue(isUserAgentAllowed(robotstxt, "", ""));

        /*
         * robots_test.cc line 49
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot", ""))
         * 
         * Empty url: implicitly disallowed, see method comment for
         * GetPathParamsQuery in robots.cc.
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", ""));

        /*
         * robots_test.cc line 52
         * 
         * EXPECT_TRUE(IsUserAgentAllowed("", "", ""))
         * 
         * All params empty: same as robots.txt empty, everything allowed.
         */
        assertTrue(isUserAgentAllowed("", "", ""));
    }

    /**
     * Rules are colon separated name-value pairs. The following names are
     * provisioned:
     * 
     * <pre>
     * user-agent: &lt;value>
     * allow: <value>
     * disallow: <value>
     * </pre>
     * 
     * See <a
     * href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.1">REP RFC
     * section "Protocol Definition".</a>
     * 
     * Google specific: webmasters sometimes miss the colon separator, but it's
     * obvious what they mean by "disallow /", so we assume the colon if it's
     * missing.
     */
    @Test
    void ID_LineSyntax_Line() {
        String robotstxt = "user-agent: FooBot\ndisallow: /\n";
        /*
         * robots_test.cc line 77
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt_correct, "FooBot", url))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/x/y"));

        /*
         * robots_test.cc line 78
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt_incorrect, "FooBot", url))
         */
        robotstxt = "foo: FooBot\nbar: /\n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/x/y"));

        /*
         * robots_test.cc line 79
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt_incorrect_accepted,
         * "FooBot", url))
         */
        robotstxt = "user-agent FooBot\ndisallow /\n";
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/x/y"));
    }

    /**
     * A group is one or more user-agent line followed by rules, and terminated
     * by a another user-agent line. Rules for same user-agents are combined
     * opaquely into one group. Rules outside groups are ignored. See <a
     * href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.1">REP RFC
     * section "Protocol Definition".</a>
     */
    @Test
    void ID_LineSyntax_Groups() {
        String robotstxt = "allow: /foo/bar/\n\n" //
                        + "user-agent: FooBot\n" //
                        + "disallow: /\n" //
                        + "allow: /x/\n" //
                        + "user-agent: BarBot\n" //
                        + "disallow: /\n" //
                        + "allow: /y/\n\n\n" //
                        + "allow: /w/\n" //
                        + "user-agent: BazBot\n\n" //
                        + "user-agent: FooBot\n" //
                        + "allow: /z/\n" //
                        + "disallow: /\n";

        String url_w = "http://foo.bar/w/a";
        String url_x = "http://foo.bar/x/b";
        String url_y = "http://foo.bar/y/c";
        String url_z = "http://foo.bar/z/d";
        String url_foo = "http://foo.bar/foo/bar/";

        /*
         * robots_test.cc line 112
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot", url_x))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", url_x));

        /*
         * robots_test.cc line 113
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot", url_z))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", url_z));

        /*
         * robots_test.cc line 114
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot", url_y))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", url_y));

        /*
         * robots_test.cc line 115
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "BarBot", url_y))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "BarBot", url_y));

        /*
         * robots_test.cc line 116
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "BarBot", url_w))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "BarBot", url_w));

        /*
         * robots_test.cc line 117
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "BarBot", url_z))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "BarBot", url_z));

        /*
         * robots_test.cc line 118
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "BazBot", url_z))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "BazBot", url_z));

        /*
         * robots_test.cc line 121
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot", url_foo))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", url_foo));

        /*
         * robots_test.cc line 122
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "BarBot", url_foo))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "BarBot", url_foo));

        /*
         * robots_test.cc line 123
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "BazBot", url_foo))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "BazBot", url_foo));
    }

    /**
     * Group must not be closed by rules not explicitly defined in the REP RFC.
     * See <a
     * href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.1">REP RFC
     * section "Protocol Definition".</a>
     */
    @Test
    void ID_LineSyntax_Groups_OtherRules() {
        String robotstxt = "User-agent: BarBot\n" //
                        + "Sitemap: https://foo.bar/sitemap\n" //
                        + "User-agent: *\n" //
                        + "Disallow: /\n";
        String url = "http://foo.bar/";

        /*
         * robots_test.cc line 137
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot", url))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", url));

        /*
         * robots_test.cc line 138
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "BarBot", url))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "BarBot", url));

        /*
         * robots_test.cc line 147
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot", url))
         */
        robotstxt = "User-agent: FooBot\n" //
                        + "Invalid-Unknown-Line: unknown\n" //
                        + "User-agent: *\n" //
                        + "Disallow: /\n";
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", url));

        /*
         * robots_test.cc line 148
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "BarBot", url))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "BarBot", url));
    }

    /**
     * REP lines are case insensitive. See <a
     * href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.1">REP RFC
     * section "Protocol Definition".</a>
     * 
     * @see {@link #ID_UserAgentValueCaseInsensitive()}
     */
    @Test
    void ID_REPLineNamesCaseInsensitive() {
        String robotstxt_upper, robotstxt_lower, robotstxt_camel;
        robotstxt_upper = "USER-AGENT: FooBot\n" //
                        + "ALLOW: /x/\n" //
                        + "DISALLOW: /\n";
        robotstxt_lower = "user-agent: FooBot\n" //
                        + "allow: /x/\n" //
                        + "disallow: /\n";
        robotstxt_camel = "uSeR-aGeNt: FooBot\n" //
                        + "AlLoW: /x/\n" //
                        + "dIsAlLoW: /\n";
        String url_allowed = "http://foo.bar/x/y";
        String url_disallowed = "http://foo.bar/a/b";

        /*
         * robots_test.cc line 170
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt_upper, "FooBot",
         * url_allowed))
         */
        assertTrue(isUserAgentAllowed(robotstxt_upper, "FooBot", url_allowed));

        /*
         * robots_test.cc line 171
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt_lower, "FooBot",
         * url_allowed))
         */
        assertTrue(isUserAgentAllowed(robotstxt_lower, "FooBot", url_allowed));

        /*
         * robots_test.cc line 172
         * 
         * String robotstxt = "uSeR-aGeNt: FooBot\nAlLoW: /x/\ndIsAlLoW: /\n";
         * assertTrue(isUserAgentAllowed(robotstxt_camel, "FooBot",
         * url_allowed));
         * 
         * /* robots_test.cc line 173
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt_upper, "FooBot",
         * url_disallowed))
         */
        assertFalse(isUserAgentAllowed(robotstxt_upper, "FooBot", url_disallowed));

        /*
         * robots_test.cc line 174
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt_lower, "FooBot",
         * url_disallowed))
         */
        assertFalse(isUserAgentAllowed(robotstxt_lower, "FooBot", url_disallowed));

        /*
         * robots_test.cc line 175
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt_camel, "FooBot",
         * url_disallowed))
         */
        assertFalse(isUserAgentAllowed(robotstxt_camel, "FooBot", url_disallowed));
    }

    /**
     * A user-agent line is expected to contain only [a-zA-Z_-] characters and
     * must not be empty. See <a
     * href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.2.1">REP RFC
     * section "The user-agent line".</a>
     */
    @Test
    public void ID_VerifyValidUserAgentsToObey() {
        /*
         * robots_test.cc line 182
         * 
         * EXPECT_TRUE(RobotsMatcher::IsValidUserAgentToObey("Foobot"))
         */
        assertTrue(isValidUserAgentToObey("Foobot"));

        /*
         * robots_test.cc line 183
         * 
         * EXPECT_TRUE(RobotsMatcher::IsValidUserAgentToObey("Foobot-Bar"))
         */
        assertTrue(isValidUserAgentToObey("Foobot-Bar"));

        /*
         * robots_test.cc line 184
         * 
         * EXPECT_TRUE(RobotsMatcher::IsValidUserAgentToObey("Foo_Bar"))
         */
        assertTrue(isValidUserAgentToObey("Foo_Bar"));

        /*
         * robots_test.cc line 186
         * 
         * EXPECT_FALSE(RobotsMatcher::IsValidUserAgentToObey(absl::string_view(
         * )))
         */
        assertFalse(isValidUserAgentToObey(null));

        /*
         * robots_test.cc line 187
         * 
         * EXPECT_FALSE(RobotsMatcher::IsValidUserAgentToObey(""))
         */
        assertFalse(isValidUserAgentToObey(""));

        /*
         * robots_test.cc line 188
         * 
         * EXPECT_FALSE(RobotsMatcher::IsValidUserAgentToObey("ツ"))
         */
        assertFalse(isValidUserAgentToObey("ツ"));

        /*
         * robots_test.cc line 190
         * 
         * EXPECT_FALSE(RobotsMatcher::IsValidUserAgentToObey("Foobot*"))
         */
        assertFalse(isValidUserAgentToObey("Foobot*"));

        /*
         * robots_test.cc line 191
         * 
         * EXPECT_FALSE(RobotsMatcher::IsValidUserAgentToObey(" Foobot "))
         */
        assertFalse(isValidUserAgentToObey(" Foobot "));

        /*
         * robots_test.cc line 192
         * 
         * EXPECT_FALSE(RobotsMatcher::IsValidUserAgentToObey("Foobot/2.1"))
         */
        assertFalse(isValidUserAgentToObey("Foobot/2.1"));

        /*
         * robots_test.cc line 194
         * 
         * EXPECT_FALSE(RobotsMatcher::IsValidUserAgentToObey("Foobot Bar"))
         */
        assertFalse(isValidUserAgentToObey("Foobot Bar"));
    }

    /**
     * User-agent line values are case insensitive. See <a
     * href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.2.1">REP RFC
     * section "The user-agent line".</a>
     * 
     * @see {@link #ID_REPLineNamesCaseInsensitive()}
     */
    @Test
    public void ID_UserAgentValueCaseInsensitive() {
        String robotstxt_upper, robotstxt_lower, robotstxt_camel;
        robotstxt_upper = "User-Agent: FOO BAR\n" //
                        + "Allow: /x/\n" //
                        + "Disallow: /\n";
        robotstxt_lower = "user-agent: foo bar\n" //
                        + "Allow: /x/\n" //
                        + "Disallow: /\n: /\n";
        robotstxt_camel = "uSeR-aGeNt: FoO bAr\n" //
                        + "Allow: /x/\n" //
                        + "Disallow: /\n";
        String url_allowed = "http://foo.bar/x/y";
        String url_disallowed = "http://foo.bar/a/b";
        /*
         * robots_test.cc line 216
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt_upper, "Foo", url_allowed))
         */
        assertTrue(isUserAgentAllowed(robotstxt_upper, "Foo", url_allowed));

        /*
         * robots_test.cc line 217
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt_lower, "Foo", url_allowed))
         */
        assertTrue(isUserAgentAllowed(robotstxt_lower, "Foo", url_allowed));

        /*
         * robots_test.cc line 218
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt_camel, "Foo", url_allowed))
         */
        assertTrue(isUserAgentAllowed(robotstxt_camel, "Foo", url_allowed));

        /*
         * robots_test.cc line 219
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt_upper, "Foo",
         * url_disallowed))
         */
        assertFalse(isUserAgentAllowed(robotstxt_upper, "Foo", url_disallowed));

        /*
         * robots_test.cc line 220
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt_lower, "Foo",
         * url_disallowed))
         */
        assertFalse(isUserAgentAllowed(robotstxt_lower, "Foo", url_disallowed));

        /*
         * robots_test.cc line 221
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt_camel, "Foo",
         * url_disallowed))
         */
        assertFalse(isUserAgentAllowed(robotstxt_camel, "Foo", url_disallowed));

        /*
         * robots_test.cc line 222
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt_upper, "foo", url_allowed))
         */
        assertTrue(isUserAgentAllowed(robotstxt_upper, "foo", url_allowed));

        /*
         * robots_test.cc line 223
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt_lower, "foo", url_allowed))
         */
        assertTrue(isUserAgentAllowed(robotstxt_lower, "foo", url_allowed));

        /*
         * robots_test.cc line 224
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt_camel, "foo", url_allowed))
         */
        assertTrue(isUserAgentAllowed(robotstxt_camel, "foo", url_allowed));

        /*
         * robots_test.cc line 225
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt_upper, "foo",
         * url_disallowed))
         */
        assertFalse(isUserAgentAllowed(robotstxt_upper, "foo", url_disallowed));

        /*
         * robots_test.cc line 226
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt_lower, "foo",
         * url_disallowed))
         */
        assertFalse(isUserAgentAllowed(robotstxt_lower, "foo", url_disallowed));

        /*
         * robots_test.cc line 227
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt_camel, "foo",
         * url_disallowed))
         */
        assertFalse(isUserAgentAllowed(robotstxt_camel, "foo", url_disallowed));
    }

    /**
     * Google specific: accept user-agent value up to the first space. Space is
     * not allowed in user-agent values, but that doesn't stop webmasters from
     * using them. This is more restrictive than the RFC, since in case of the
     * bad value "Googlebot Images" we'd still obey the rules with "Googlebot".
     * Extends <a
     * href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.2.1">REP RFC
     * section "The user-agent line"</a>
     */
    @Test
    public void GoogleOnly_AcceptUserAgentUpToFirstSpace() {
        /*
         * robots_test.cc line 237
         * 
         * EXPECT_FALSE(RobotsMatcher::IsValidUserAgentToObey("Foobot Bar"))
         */
        assertFalse(isValidUserAgentToObey("Foobot Bar"));

        String robotstxt = "User-Agent: *\n" //
                        + "Disallow: /\n" //
                        + "User-Agent: Foo Bar\n" //
                        + "Allow: /x/\n" //
                        + "Disallow: /\n";
        String url = "http://foo.bar/x/y";

        /*
         * robots_test.cc line 246
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "Foo", url))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "Foo", url));

        /*
         * robots_test.cc line 247
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "Foo Bar", url))
         * 
         * Note: The expected test result was changed because
         * SimpleRobotRulesParser behaves differently and would even match the
         * full (but invalid) user-agent product token "Foo Bar". It would also
         * match "Foo" alone (see above or original line 246).
         */
        // assertFalse(isUserAgentAllowed(robotstxt, "Foo Bar", url));
        assertTrue(isUserAgentAllowed(robotstxt, "Foo Bar", url));
    }

    /**
     * If no group matches the user-agent, crawlers must obey the first group
     * with a user-agent line with a "*" value, if present. If no group
     * satisfies either condition, or no groups are present at all, no rules
     * apply. See <a
     * href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.2.1">REP RFC
     * section "The user-agent line".</a>
     */
    @Test
    public void ID_GlobalGroups_Secondary() {
        String robotstxt_empty = "";
        String robotstxt_global = "user-agent: *\n" //
                        + "allow: /\n" //
                        + "user-agent: FooBot\n" //
                        + "disallow: /\n";
        String robotstxt_only_specific = "user-agent: FooBot\n" //
                        + "allow: /\n" //
                        + "user-agent: BarBot\n" //
                        + "disallow: /\n" //
                        + "user-agent: BazBot\n" //
                        + "disallow: /\n";
        String url = "http://foo.bar/x/y";

        /*
         * robots_test.cc line 271
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt_empty, "FooBot", url))
         */
        assertTrue(isUserAgentAllowed(robotstxt_empty, "FooBot", url));

        /*
         * robots_test.cc line 272
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt_global, "FooBot", url))
         */
        assertFalse(isUserAgentAllowed(robotstxt_global, "FooBot", url));

        /*
         * robots_test.cc line 273
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt_global, "BarBot", url))
         */
        assertTrue(isUserAgentAllowed(robotstxt_global, "BarBot", url));

        /*
         * robots_test.cc line 274
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt_only_specific, "QuxBot",
         * url))
         */
        assertTrue(isUserAgentAllowed(robotstxt_only_specific, "QuxBot", url));

    }

    /**
     * Matching rules against URIs is case sensitive. See <a
     * href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.2.2">REP RFC
     * section "The Allow and Disallow lines".</a>
     */
    @Test
    public void ID_AllowDisallow_Value_CaseSensitive() {
        String robotstxt_lowercase_url = "user-agent: FooBot\ndisallow: /x/\n";
        String robotstxt_uppercase_url = "user-agent: FooBot\ndisallow: /X/\n";
        String url = "http://foo.bar/x/y";

        /*
         * robots_test.cc line 289
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt_lowercase_url, "FooBot",
         * url))
         */
        assertFalse(isUserAgentAllowed(robotstxt_lowercase_url, "FooBot", url));

        /*
         * robots_test.cc line 290
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt_uppercase_url, "FooBot",
         * url))
         */
        assertTrue(isUserAgentAllowed(robotstxt_uppercase_url, "FooBot", url));
    }

    /**
     * The most specific match found MUST be used. The most specific match is
     * the match that has the most octets. In case of multiple rules with the
     * same length, the least strict rule must be used. See <a
     * href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.2.2">REP RFC
     * section "The Allow and Disallow lines".</a>
     */
    @Test
    public void ID_LongestMatch() {
        String url = "http://foo.bar/x/page.html";

        /*
         * robots_test.cc line 306
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot", url))
         */
        String robotstxt = "user-agent: FooBot\n" //
                        + "disallow: /x/page.html\n" //
                        + "allow: /x/\n";
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", url));

        /*
         * robots_test.cc line 314
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot", url))
         */
        robotstxt = "user-agent: FooBot\n" //
                        + "allow: /x/page.html\n" //
                        + "disallow: /x/\n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", url));

        /*
         * robots_test.cc line 315
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/x/"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/x/"));

        /*
         * robots_test.cc line 324
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot", url))
         * 
         * In case of equivalent disallow and allow patterns for the same
         * user-agent, allow is used.
         */
        robotstxt = "user-agent: FooBot\n" //
                        + "disallow: \n" //
                        + "allow: \n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", url));

        /*
         * robots_test.cc line 333
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot", url))
         * 
         * In case of equivalent disallow and allow patterns for the same
         * user-agent, allow is used.
         */
        robotstxt = "user-agent: FooBot\n" //
                        + "disallow: /\n" //
                        + "allow: /\n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", url));

        /*
         * robots_test.cc line 342
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot", url_a))
         */
        robotstxt = "user-agent: FooBot\n" //
                        + "disallow: /x\n" //
                        + "allow: /x/\n";
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/x"));

        /*
         * robots_test.cc line 343
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot", url_b))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/x/"));

        /*
         * robots_test.cc line 353
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot", url))
         * 
         * In case of equivalent disallow and allow patterns for the same
         * user-agent, allow is used.
         */
        robotstxt = "user-agent: FooBot\n" //
                        + "disallow: /x/page.html\n" //
                        + "allow: /x/page.html\n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", url));

        /*
         * robots_test.cc line 361
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/page.html"))
         * 
         * Longest match wins.
         */
        robotstxt = "user-agent: FooBot\n" //
                        + "allow: /page\n" //
                        + "disallow: /*.html\n";
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/page.html"));

        /*
         * robots_test.cc line 363
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/page"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/page"));

        /*
         * robots_test.cc line 372
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot", url))
         * 
         * Longest match wins.
         */
        robotstxt = "user-agent: FooBot\n" //
                        + "allow: /x/page.\n" //
                        + "disallow: /*.html\n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", url));

        /*
         * robots_test.cc line 373
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/x/y.html"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/x/y.html"));

        /*
         * robots_test.cc line 383
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/x/page"))
         * 
         * Most specific group for FooBot allows implicitly /x/page.
         */
        robotstxt = "User-agent: *\n" //
                        + "Disallow: /x/\n" //
                        + "User-agent: FooBot\n" //
                        + "Disallow: /y/\n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/x/page"));

        /*
         * robots_test.cc line 385
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/y/page"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/y/page"));
    }

    /**
     * Octets in the URI and robots.txt paths outside the range of the US-ASCII
     * coded character set, and those in the reserved range defined by RFC3986,
     * MUST be percent-encoded as defined by RFC3986 prior to comparison. See <a
     * href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.2.2">REP RFC
     * section "The Allow and Disallow lines".</a>
     * 
     * NOTE: It's up to the caller to percent encode a URL before passing it to
     * the parser. Percent encoding URIs in the rules is unnecessary.
     */
    @Test
    public void ID_Encoding() {
        /*
         * robots_test.cc line 405
         * 
         * EXPECT_TRUE(IsUserAgentAllowed( robotstxt, "FooBot",
         * "http://foo.bar/foo/bar?qux=taz&baz=http://foo.bar?tar&par"))
         * 
         * /foo/bar?baz=http://foo.bar stays unencoded.
         */
        String robotstxt = "User-agent: FooBot\n" //
                        + "Disallow: /\n" //
                        + "Allow: /foo/bar?qux=taz&baz=http://foo.bar?tar&par\n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar?qux=taz&baz=http://foo.bar?tar&par"));

        /*
         * robots_test.cc line 416
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/bar/%E3%83%84"))
         * 
         * 3 byte character: /foo/bar/ツ -> /foo/bar/%E3%83%84
         */
        robotstxt = "User-agent: FooBot\n" //
                        + "Disallow: /\n" //
                        + "Allow: /foo/bar/ツ\n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar/%E3%83%84"));

        /*
         * robots_test.cc line 419
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/bar/ツ"))
         * 
         * The parser encodes the 3-byte character, but the URL is not
         * %-encoded.
         * 
         * Note: The expected test results were changed. Actually, it's an
         * improvement if SimpleRobotRulesParser can handle Unicode characters
         * in the URL which are not percent-encoded.
         */
        // assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar/ツ"));
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar/ツ"));

        /*
         * robots_test.cc line 428
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/bar/%E3%83%84"))
         * 
         * Percent encoded 3 byte character: /foo/bar/%E3%83%84 ->
         * /foo/bar/%E3%83%84
         */
        robotstxt = "User-agent: FooBot\nDisallow: /\nAllow: /foo/bar/%E3%83%84\n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar/%E3%83%84"));

        /*
         * robots_test.cc line 430
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/bar/ツ"))
         * 
         * Note: The expected test results were changed. Actually, it's an
         * improvement if SimpleRobotRulesParser can handle Unicode characters
         * in the URL which are not percent-encoded.
         */
        // assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar/ツ"));
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar/ツ"));

        /*
         * robots_test.cc line 441
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/bar/baz"))
         * 
         * Percent encoded unreserved US-ASCII: /foo/bar/%62%61%7A -> NULL This
         * is illegal according to RFC3986 and while it may work here due to
         * simple string matching, it should not be relied on.
         * 
         * Note: The expected test results were changed. Actually, it's an
         * improvement if SimpleRobotRulesParser handles percent-encoded
         * characters without special meaning equivalently.
         */
        robotstxt = "User-agent: FooBot\n" //
                        + "Disallow: /\n" //
                        + "Allow: /foo/bar/%62%61%7A\n";
        // assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar/baz"));
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar/baz"));

        /*
         * robots_test.cc line 443
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/bar/%62%61%7A"))
         */
        robotstxt = "User-agent: FooBot\n" //
                        + "Disallow: /\n" //
                        + "Allow: /foo/bar/%62%61%7A\n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar/%62%61%7A"));

    }

    /**
     * The REP RFC defines the following characters that have special meaning in
     * robots.txt:
     * 
     * <pre>
     *      # - inline comment.
     *      $ - end of pattern.
     * - any number of characters.
     * </pre>
     * 
     * See <a
     * href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.2.3">REP RFC
     * section "Special Characters".</a>
     */
    @Test
    public void ID_SpecialCharacters() {
        /*
         * robots_test.cc line 461
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/bar/quz"))
         */
        String robotstxt = "User-agent: FooBot\n" //
                        + "Disallow: /foo/bar/quz\n" //
                        + "Allow: /foo/*/qux\n";
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar/quz"));

        /*
         * robots_test.cc line 463
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/quz"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/quz"));

        /*
         * robots_test.cc line 465
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo//quz"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo//quz"));

        /*
         * robots_test.cc line 467
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/bax/quz"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bax/quz"));

        /*
         * robots_test.cc line 475
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/bar"))
         */
        robotstxt = "User-agent: FooBot\n" //
                        + "Disallow: /foo/bar$\n" //
                        + "Allow: /foo/bar/qux\n";
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar"));

        /*
         * robots_test.cc line 477
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/bar/qux"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar/qux"));

        /*
         * robots_test.cc line 479
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/bar/"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar/"));

        /*
         * robots_test.cc line 481
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/bar/baz"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar/baz"));

        /*
         * robots_test.cc line 490
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/bar"))
         */
        robotstxt = "User-agent: FooBot\n" //
                        + "# Disallow: /\n" //
                        + "Disallow: /foo/quz#qux\n" //
                        + "Allow: /\n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/bar"));

        /*
         * robots_test.cc line 492
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/foo/quz"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/foo/quz"));
    }

    /**
     * Google-specific: "index.html" (and only that) at the end of a pattern is
     * equivalent to "/".
     */
    @Test
    public void GoogleOnly_IndexHTMLisDirectory() {
        String robotstxt = "User-Agent: *\n" //
                        + "Allow: /allowed-slash/index.html\n" //
                        + "Disallow: /\n";
        /*
         * robots_test.cc line 505
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "foobot",
         * "http://foo.com/allowed-slash/"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "foobot", "http://foo.com/allowed-slash/"));

        /*
         * robots_test.cc line 508
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "foobot",
         * "http://foo.com/allowed-slash/index.htm"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "foobot", "http://foo.com/allowed-slash/index.htm"));

        /*
         * robots_test.cc line 511
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "foobot",
         * "http://foo.com/allowed-slash/index.html"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "foobot", "http://foo.com/allowed-slash/index.html"));

        /*
         * robots_test.cc line 513
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "foobot",
         * "http://foo.com/anyother-url"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "foobot", "http://foo.com/anyother-url"));
    }

    /**
     * Google-specific: long lines are ignored after 8 * 2083 bytes. See comment
     * in RobotsTxtParser::Parse().
     */
    @Test
    void GoogleOnly_LineTooLong() {
        int kEOLLen = "\n".length();
        int kMaxLineLen = 2083 * 8;
        String allow = "allow: ";
        String disallow = "disallow: ";

        StringBuilder sb = new StringBuilder();
        sb.append("/x/");
        int maxLength = kMaxLineLen - "/x/".length() - disallow.length() - kEOLLen;
        while (sb.length() < maxLength) {
            sb.append('a');
        }
        String longline = sb.toString();

        /*
         * Disallow rule pattern matches the URL after being cut off at
         * kMaxLineLen.
         */
        sb = new StringBuilder();
        sb.append("user-agent: FooBot\n");
        sb.append(disallow);
        sb.append(longline);
        sb.append("/qux\n");
        String robotstxt = sb.toString();

        /*
         * robots_test.cc line 537
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fux"))
         * 
         * Matches nothing, so URL is allowed.
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fux"));

        /*
         * robots_test.cc line 539
         * 
         * EXPECT_FALSE(IsUserAgentAllowed( robotstxt, "FooBot",
         * absl::StrCat("http://foo.bar", longline, "/fux")))
         * 
         * Matches cut off disallow rule.
         * 
         * Note: The expected test results were changed because
         * SimpleRobotRulesParser behaves differently and does not cut off the
         * overlong line.
         */
        String url = new StringBuilder().append("http://foo.bar").append(longline).append("/fux").toString();
        // assertFalse(isUserAgentAllowed(robotstxt, "FooBot", url));
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", url));

        sb = new StringBuilder();
        sb.append("/x/");
        maxLength = kMaxLineLen - "/x/".length() - allow.length() - kEOLLen;
        while (sb.length() < maxLength) {
            sb.append('a');
        }
        String longlineA = sb.toString();

        sb = new StringBuilder();
        sb.append("/x/");
        maxLength = kMaxLineLen - "/x/".length() - allow.length() - kEOLLen;
        while (sb.length() < maxLength) {
            sb.append('b');
        }
        String longlineB = longlineA.replaceAll("a", "b");

        sb = new StringBuilder();
        sb.append("user-agent: FooBot\n");
        sb.append("disallow: /\n");
        sb.append(allow).append(longlineA).append("/qux\n");
        sb.append(allow).append(longlineB).append("/qux\n");
        robotstxt = sb.toString();

        /*
         * robots_test.cc line 559
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/"))
         * 
         * URL matches the disallow rule.
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/"));

        /*
         * robots_test.cc line 561
         * 
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * absl::StrCat("http://foo.bar", longline_a, "/qux")))
         * 
         * Matches the allow rule exactly.
         */
        // assertTrue(isUserAgentAllowed(robotstxt, "FooBot", url));
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", url));

        /*
         * robots_test.cc line 565
         * 
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * absl::StrCat("http://foo.bar", longline_b, "/fux")))
         * 
         * Matches cut off allow rule.
         * 
         * Note: The expected test results were changed because
         * SimpleRobotRulesParser behaves differently and does not cut off the
         * overlong line.
         */
        url = new StringBuilder().append("http://foo.bar").append(longlineB).append("/fux").toString();
        // assertTrue(isUserAgentAllowed(robotstxt, "FooBot", url));
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", url));
    }

    @Test
    public void GoogleOnly_DocumentationChecks() {
        String robotstxt = "user-agent: FooBot\n" //
                        + "disallow: /\n" //
                        + "allow: /fish\n";

        /*
         * robots_test.cc line 580
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/bar"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/bar"));

        /*
         * robots_test.cc line 582
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish"));

        /*
         * robots_test.cc line 583
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish.html"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish.html"));

        /*
         * robots_test.cc line 585
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish/salmon.html"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish/salmon.html"));

        /*
         * robots_test.cc line 587
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fishheads"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fishheads"));

        /*
         * robots_test.cc line 589
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fishheads/yummy.html"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fishheads/yummy.html"));

        /*
         * robots_test.cc line 591
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish.html?id=anything"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish.html?id=anything"));

        /*
         * robots_test.cc line 594
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/Fish.asp"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/Fish.asp"));

        /*
         * robots_test.cc line 596
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/catfish"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/catfish"));

        /*
         * robots_test.cc line 598
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/?id=fish"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/?id=fish"));

        /* "/fish*" equals "/fish" */
        robotstxt = "user-agent: FooBot\ndisallow: /\nallow: /fish*\n";

        /*
         * robots_test.cc line 607
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/bar"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/bar"));

        /*
         * robots_test.cc line 610
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish"));

        /*
         * robots_test.cc line 611
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish.html"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish.html"));

        /*
         * robots_test.cc line 613
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish/salmon.html"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish/salmon.html"));

        /*
         * robots_test.cc line 615
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fishheads"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fishheads"));

        /*
         * robots_test.cc line 617
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fishheads/yummy.html"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fishheads/yummy.html"));

        /*
         * robots_test.cc line 619
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish.html?id=anything"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish.html?id=anything"));

        /*
         * robots_test.cc line 622
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/Fish.bar"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/Fish.bar"));

        /*
         * robots_test.cc line 624
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/catfish"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/catfish"));

        /*
         * robots_test.cc line 626
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/?id=fish"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/?id=fish"));

        /* "/fish/" does not equal "/fish" */
        robotstxt = "user-agent: FooBot\n" //
                        + "disallow: /\n" //
                        + "allow: /fish/\n";

        /*
         * robots_test.cc line 635
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/bar"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/bar"));

        /*
         * robots_test.cc line 637
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish/"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish/"));

        /*
         * robots_test.cc line 639
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish/salmon"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish/salmon"));

        /*
         * robots_test.cc line 641
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish/?salmon"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish/?salmon"));

        /*
         * robots_test.cc line 643
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish/salmon.html"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish/salmon.html"));

        /*
         * robots_test.cc line 645
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish/?id=anything"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish/?id=anything"));

        /*
         * robots_test.cc line 648
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish"));

        /*
         * robots_test.cc line 650
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish.html"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish.html"));

        /*
         * robots_test.cc line 652
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/Fish/Salmon.html"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/Fish/Salmon.html"));

        /* "/*.php" */
        robotstxt = "user-agent: FooBot\n" //
                        + "disallow: /\n" //
                        + "allow: /*.php\n";

        /*
         * robots_test.cc line 661
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/bar"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/bar"));

        /*
         * robots_test.cc line 663
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/filename.php"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/filename.php"));

        /*
         * robots_test.cc line 665
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/folder/filename.php"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/folder/filename.php"));

        /*
         * robots_test.cc line 667
         * 
         * EXPECT_TRUE(IsUserAgentAllowed( robotstxt, "FooBot",
         * "http://foo.bar/folder/filename.php?parameters"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/folder/filename.php?parameters"));

        /*
         * robots_test.cc line 669
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar//folder/any.php.file.html"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar//folder/any.php.file.html"));

        /*
         * robots_test.cc line 671
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/filename.php/"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/filename.php/"));

        /*
         * robots_test.cc line 673
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/index?f=filename.php/"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/index?f=filename.php/"));

        /*
         * robots_test.cc line 675
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/php/"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/php/"));

        /*
         * robots_test.cc line 677
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/index?php"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/index?php"));

        /*
         * robots_test.cc line 680
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/windows.PHP"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/windows.PHP"));

        /* "/*.php$" */
        robotstxt = "user-agent: FooBot\n" //
                        + "disallow: /\n" //
                        + "allow: /*.php$\n";

        /*
         * robots_test.cc line 689
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/bar"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/bar"));

        /*
         * robots_test.cc line 691
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/filename.php"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/filename.php"));

        /*
         * robots_test.cc line 693
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/folder/filename.php"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/folder/filename.php"));

        /*
         * robots_test.cc line 696
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/filename.php?parameters"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/filename.php?parameters"));

        /*
         * robots_test.cc line 698
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/filename.php/"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/filename.php/"));

        /*
         * robots_test.cc line 700
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/filename.php5"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/filename.php5"));

        /*
         * robots_test.cc line 702
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/php/"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/php/"));

        /*
         * robots_test.cc line 704
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/filename?php"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/filename?php"));

        /*
         * robots_test.cc line 706
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/aaaphpaaa"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/aaaphpaaa"));

        /*
         * robots_test.cc line 708
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar//windows.PHP"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar//windows.PHP"));

        /* "/fish*.php" */
        robotstxt = "user-agent: FooBot\n" //
                        + "disallow: /\n" //
                        + "allow: /fish*.php\n";

        /*
         * robots_test.cc line 717
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/bar"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/bar"));

        /*
         * robots_test.cc line 719
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fish.php"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fish.php"));

        /*
         * robots_test.cc line 721
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/fishheads/catfish.php?parameters"))
         */
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/fishheads/catfish.php?parameters"));

        /*
         * robots_test.cc line 725
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot",
         * "http://foo.bar/Fish.PHP"))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://foo.bar/Fish.PHP"));

        /* Section "Order of precedence for group-member records". */

        /*
         * robots_test.cc line 735
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot", url))
         */
        robotstxt = "user-agent: FooBot\n" //
                        + "allow: /p\n" //
                        + "disallow: /\n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://example.com/page"));

        /*
         * robots_test.cc line 743
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot", url))
         */
        robotstxt = "user-agent: FooBot\n" //
                        + "allow: /folder\n" //
                        + "disallow: /folder\n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://example.com/folder/page"));

        /*
         * robots_test.cc line 751
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot", url))
         */
        robotstxt = "user-agent: FooBot\n" //
                        + "allow: /page\n" //
                        + "disallow: /*.htm\n";
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://example.com/page.htm"));

        /*
         * robots_test.cc line 760
         * 
         * EXPECT_TRUE(IsUserAgentAllowed(robotstxt, "FooBot", url))
         */
        robotstxt = "user-agent: FooBot\n" //
                        + "allow: /$\n" //
                        + "disallow: /\n";
        assertTrue(isUserAgentAllowed(robotstxt, "FooBot", "http://example.com/"));

        /*
         * robots_test.cc line 761
         * 
         * EXPECT_FALSE(IsUserAgentAllowed(robotstxt, "FooBot", url_page))
         */
        assertFalse(isUserAgentAllowed(robotstxt, "FooBot", "http://example.com/page.html"));

    }

    /**
     * Different kinds of line endings are all supported: %x0D / %x0A / %x0D.0A
     */
    @Test
    public void ID_LinesNumbersAreCountedCorrectly() {
        // TODO: uses the report function of googlebot robots.txt parser
    }

    /**
     * BOM characters are unparseable and thus skipped. The rules following the
     * line are used.
     */
    @Test
    public void ID_UTF8ByteOrderMarkIsSkipped() {
        // TODO: uses the report function of googlebot robots.txt parser
    }

    /*
     * Google specific: the I-D allows any line that crawlers might need, such
     * as sitemaps, which Google supports. See <a
     * href="https://www.rfc-editor.org/rfc/rfc9309.html#section-2.2.4">REP RFC
     * section "Other records".</a>
     */
    @Test
    public void ID_NonStandardLineExample_Sitemap() {
        String sitemap_loc = "http://foo.bar/sitemap.xml";
        String robotstxt = "User-Agent: foo\n" //
                        + "Allow: /some/path\n" //
                        + "User-Agent: bar\n\n\n";

        /*
         * robots_test.cc line 946
         * 
         * EXPECT_EQ(sitemap_loc, report.sitemap());
         */
        String robotstxtSitemap = robotstxt + "Sitemap: " + sitemap_loc + "\n";
        BaseRobotRules rules = SimpleRobotRulesParserTest.createRobotRules("*", robotstxtSitemap, true);
        List<String> sitemaps = rules.getSitemaps();
        assertNotNull(sitemaps);
        assertEquals(1, sitemaps.size());
        assertEquals(sitemap_loc, rules.getSitemaps().get(0));

        /*
         * robots_test.cc line 961
         * 
         * EXPECT_EQ(sitemap_loc, report.sitemap());
         * 
         * A sitemap line may appear anywhere in the file.
         */
        robotstxtSitemap = "Sitemap: " + sitemap_loc + "\n" + robotstxt;
        rules = SimpleRobotRulesParserTest.createRobotRules("*", robotstxtSitemap, true);
        sitemaps = rules.getSitemaps();
        assertNotNull(sitemaps);
        assertEquals(1, sitemaps.size());
        assertEquals(sitemap_loc, rules.getSitemaps().get(0));
    }

    /*
     * Integrity tests. These functions are available to the linker, but not in
     * the header, because they should only be used for testing.
     */

    /** Only testing URLs that are already correctly escaped here. */
    @Test
    public void TestGetPathParamsQuery() {
        // TODO: do we need tests to test the tests?
    }

    @Test
    public void TestMaybeEscapePattern() {
        // TODO: do we need tests to test the tests?
    }

}
