package crawlercommons.robots_tag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static crawlercommons.robots_tag.KnownSimpleDirectives.FOLLOW;
import static crawlercommons.robots_tag.KnownSimpleDirectives.INDEX;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("RobotsTagParser")
class RobotsTagParserTest {
    static final Directive<String> MAX_IMAGE_PREVIEW = new Directive<>("max-image-preview", "large");
    static final Directive<LocalDate> UNAVAILABLE_AFTER = new Directive<>("unavailable_after", LocalDate.of(2025, 12, 31));

    @Test
    @DisplayName("should initialize and reset properly")
    void initializeAndReset() {
        var parser = new RobotsTagParser();
        assertTrue(parser.getCollectedDirectives().isEmpty());

        parser.parse("index, follow");
        assertFalse(parser.getCollectedDirectives().isEmpty());

        parser.reset();
        assertTrue(parser.getCollectedDirectives().isEmpty());
    }

    @Test
    @DisplayName("should work with empty input")
    void emptyInput() {
        var parser = new RobotsTagParser();
        parser.parse("");
        assertTrue(parser.getCollectedDirectives().isEmpty());
    }

    @Test
    @DisplayName("should parse individual directives")
    void parseIndividualDirectives() {
        var parser = new RobotsTagParser();

        parser.parse("index");
        assertEquals(Set.of(INDEX), parser.getCollectedDirectives().withoutUserAgent().toSet());

        parser.parse("max-image-preview: large");
        assertEquals(Set.of(INDEX, MAX_IMAGE_PREVIEW), parser.getCollectedDirectives().withoutUserAgent().toSet());
    }

    @Test
    @DisplayName("should parse multiple directives")
    void parseMultipleDirectives() {
        var parser = new RobotsTagParser();

        parser.parse("index, follow");
        assertEquals(Set.of(INDEX, FOLLOW), parser.getCollectedDirectives().withoutUserAgent().toSet());

        parser.parse("max-image-preview: large, unavailable_after: 2025-12-31");
        assertEquals(Set.of(INDEX, FOLLOW, MAX_IMAGE_PREVIEW, UNAVAILABLE_AFTER), parser.getCollectedDirectives().withoutUserAgent().toSet());

        parser.reset();
        parser.parse("max-image-preview: large, index, unavailable_after: 2025-12-31, follow");
        assertEquals(Set.of(INDEX, FOLLOW, MAX_IMAGE_PREVIEW, UNAVAILABLE_AFTER), parser.getCollectedDirectives().withoutUserAgent().toSet());
    }

    @ParameterizedTest
    @DisplayName("should parse unknown simple directives under certain conditions")
    @ValueSource(strings = {
        //Unambiguous directive strings:
        "foo-123",
        "foo-123, follow", //First
        "index, foo-123, follow", //Middle
        "index, foo-123", //Last
        //Ambiguous directive strings:
        "foo-123, unavailable_after: 2025-12-31", //First
        "max-image-preview: large, foo-123, unavailable_after: 2025-12-31", //Middle
        "max-image-preview: large, foo-123" //Last
    })
    void parseSimpleDirectives(String input) {
        var parser = new RobotsTagParser();
        parser.parse(input);
        assertTrue(parser.getCollectedDirectives().withoutUserAgent().toSet().contains(new Directive<>("foo-123")));
    }

    @Test
    @DisplayName("should trim and lowercase directive names")
    void normalizeDirectiveNames() {
        var parser = new RobotsTagParser();
        parser.parse(" INDEX, FOLLOW "); //Unambiguous directive string
        parser.parse(" Max-Image-Preview : large "); //Ambiguous directive string
        assertEquals(Set.of(INDEX, FOLLOW, MAX_IMAGE_PREVIEW), parser.getCollectedDirectives().withoutUserAgent().toSet());
    }

    @Test
    @DisplayName("should eliminate duplicate directives")
    void eliminateDuplicateDirectives() {
        var parser = new RobotsTagParser();

        //Unambiguous directive strings:
        parser.parse("index, follow, index");
        assertEquals(Set.of(INDEX, FOLLOW), parser.getCollectedDirectives().withoutUserAgent().toSet());

        parser.parse("follow");
        assertEquals(Set.of(INDEX, FOLLOW), parser.getCollectedDirectives().withoutUserAgent().toSet());

        //Ambiguous directive strings:
        parser.reset();
        parser.parse("index, max-image-preview: large, index");
        assertEquals(Set.of(INDEX, MAX_IMAGE_PREVIEW), parser.getCollectedDirectives().withoutUserAgent().toSet());

        parser.parse("max-image-preview: large");
        assertEquals(Set.of(INDEX, MAX_IMAGE_PREVIEW), parser.getCollectedDirectives().withoutUserAgent().toSet());
    }

    @ParameterizedTest
    @DisplayName("should work with input that contains excess commas")
    @MethodSource("excessCommasArgs")
    void excessCommas(String input, Set<Directive<?>> expected) {
        var parser = new RobotsTagParser();
        parser.parse(input);
        assertEquals(expected, parser.getCollectedDirectives().withoutUserAgent().toSet());
        assertTrue(parser.getCollectedDirectives().withUserAgent().toMap().isEmpty());
    }

    static Stream<Arguments> excessCommasArgs() {
        return Stream.of(
            //No directives:
            arguments(",", Collections.emptySet()),
            arguments(",,,", Collections.emptySet()),
            //Unambiguous directive strings:
            arguments("index,", Set.of(INDEX)),
            arguments(", index", Set.of(INDEX)),
            arguments("index, , follow,", Set.of(INDEX, FOLLOW)),
            //Ambiguous directive strings:
            arguments("max-image-preview: large,", Set.of(MAX_IMAGE_PREVIEW)),
            arguments(", max-image-preview: large", Set.of(MAX_IMAGE_PREVIEW)),
            arguments("max-image-preview: large, , unavailable_after: 2025-12-31,", Set.of(MAX_IMAGE_PREVIEW, UNAVAILABLE_AFTER))
        );
    }

    @ParameterizedTest
    @DisplayName("should throw exceptions if configured to do so")
    @ValueSource(strings = {
        "foo: bar, index", //It is unclear whether "foo" is a directive name or a user agent.
        "max-snippet: <invalid value>, follow"
    })
    void throwExceptions(String input) {
        var parser = new RobotsTagParser(Collections.emptySet(), KnownDirectiveParsers.PARSERS_BY_NAME, ExceptionHandlers::throwing);
        assertThrows(ParserException.class, () -> parser.parse(input));
        assertTrue(parser.getCollectedDirectives().isEmpty()); //The "index" and "follow" directives were not parsed because the exception handler threw the exceptions.
    }

    @ParameterizedTest
    @DisplayName("should recover from parsing failures")
    @MethodSource("recoverFromParsingFailuresArgs")
    void recoverFromParsingFailures(String input, Set<Directive<?>> expectedWithoutUserAgent, Set<Directive<?>> expectedWithUserAgent) {
        var parser = new RobotsTagParser(Set.of("MyBot"));
        parser.parse(input);
        assertEquals(expectedWithoutUserAgent, parser.getCollectedDirectives().withoutUserAgent().toSet());
        assertEquals(expectedWithUserAgent, parser.getCollectedDirectives().withUserAgent().toSet());
    }

    static Stream<Arguments> recoverFromParsingFailuresArgs() {
        return Stream.of(
            //The first token is part of an unknown key-value pair (skip to the next known user agent):
            arguments("foo: bar, index", Collections.emptySet(), Collections.emptySet()), //It is unclear whether "foo" is a directive name or a user agent.
            arguments("foo: bar, max-image-preview: large", Collections.emptySet(), Collections.emptySet()),
            arguments("foo: bar, MyBot: index", Collections.emptySet(), Set.of(INDEX)),
            arguments("foo: bar, MyBot: max-image-preview: large", Collections.emptySet(), Set.of(MAX_IMAGE_PREVIEW)),
            //A DirectiveParser throws (skip to the next known directive name):
            arguments("max-snippet: <invalid value>, index", Set.of(INDEX), Collections.emptySet()),
            arguments("max-snippet: <invalid value>, max-image-preview: large", Set.of(MAX_IMAGE_PREVIEW), Collections.emptySet()),
            arguments("max-snippet: <invalid value>, index, UnknownBot: max-image-preview: large", Set.of(INDEX), Collections.emptySet()),
            arguments("max-snippet: <invalid value>, max-image-preview: large, UnknownBot: index", Set.of(MAX_IMAGE_PREVIEW), Collections.emptySet()),
            //A DirectiveParser throws (skip to the next known user agent):
            arguments("max-snippet: <invalid value>, UnknownBot: follow", Collections.emptySet(), Collections.emptySet()),
            arguments("max-snippet: <invalid value>, UnknownBot: max-image-preview: large", Collections.emptySet(), Collections.emptySet()),
            arguments("max-snippet: <invalid value>, UnknownBot: follow, MyBot: index", Collections.emptySet(), Set.of(INDEX)),
            arguments("max-snippet: <invalid value>, UnknownBot: max-image-preview: large, MyBot: index", Collections.emptySet(), Set.of(INDEX)),
            arguments("unavailable_after: 24:00:00, index", Collections.emptySet(), Collections.emptySet()), //The value of the first directive is invalid and contains colons.
            arguments("unavailable_after: 24:00:00, MyBot: index", Collections.emptySet(), Set.of(INDEX))
        );
    }

    @Nested
    @DisplayName("without target user agents")
    class WithoutTargetUserAgents {
        @ParameterizedTest
        @DisplayName("should collect directives that apply to all user agents")
        @ValueSource(strings = {
            "index",
            "index, UnknownBot: follow",
            "index, UnknownBot: follow, nocache"
        })
        void collectForAllUserAgents(String input) {
            var parser = new RobotsTagParser();
            parser.parse(input);
            assertEquals(Set.of(INDEX), parser.getCollectedDirectives().withoutUserAgent().toSet());
        }

        @ParameterizedTest
        @DisplayName("should not collect directives that only apply to specific user agents")
        @ValueSource(strings = {
            "UnknownBot: index",
            "UnknownBot-1: index, UnknownBot-2: index"
        })
        void ignoreForSpecificUserAgents(String input) {
            var parser = new RobotsTagParser();
            parser.parse(input);
            assertTrue(parser.getCollectedDirectives().isEmpty());
        }
    }

    @Nested
    @DisplayName("with target user agents")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS) //Required for @MethodSource because Java 11 does not support static methods in inner classes.
    class WithTargetUserAgents {
        @ParameterizedTest
        @DisplayName("should collect directives that apply to all user agents")
        @ValueSource(strings = {
            "index",
            "index, UnknownBot: follow",
            "index, UnknownBot: follow, nocache",
            "index, MyBot-1: follow"
        })
        void collectForAllUserAgents(String input) {
            var parser = new RobotsTagParser(Set.of("MyBot-1", "MyBot-2"));
            parser.parse(input);
            assertEquals(Set.of(INDEX), parser.getCollectedDirectives().withoutUserAgent().toSet());
        }

        @Test
        @DisplayName("should collect directives that apply to the target user agents")
        void collectForTargetUserAgents() {
            var parser = new RobotsTagParser(Set.of("MyBot-1", "MyBot-2"));

            parser.parse("MyBot-1: index, follow");
            assertEquals(Set.of(INDEX, FOLLOW), parser.getCollectedDirectives().withUserAgent().toSet());

            parser.parse("MyBot-2: max-image-preview: large");
            assertEquals(Set.of(INDEX, FOLLOW, MAX_IMAGE_PREVIEW), parser.getCollectedDirectives().withUserAgent().toSet());

            var expectedUserAgentGroups = Map.of(
                "mybot-1", Set.of(INDEX, FOLLOW),
                "mybot-2", Set.of(MAX_IMAGE_PREVIEW)
            );

            assertEquals(expectedUserAgentGroups, parser.getCollectedDirectives().withUserAgent().toMap());

            List<String> inputs = List.of(
                "MyBot-1: index, follow",
                "MyBot-1: index, MyBot-2: follow",
                "UnknownBot: nocache, MyBot-1: index, follow",
                "MyBot-1: index, UnknownBot: all, MyBot-2: follow, UnknownBot: nocache"
            );

            inputs.forEach(input -> {
                parser.reset();
                parser.parse(input);
                assertEquals(Set.of(INDEX, FOLLOW), parser.getCollectedDirectives().withUserAgent().toSet());
            });
        }

        @ParameterizedTest
        @DisplayName("should not collect directives that only apply to other user agents")
        @ValueSource(strings = {
            "UnknownBot: index",
            "UnknownBot: index, follow",
            "UnknownBot-1: index, UnknownBot-2: index",
            "UnknownBot-1: index, follow, UnknownBot-2: index, follow"
        })
        void ignoreForOtherUserAgents(String input) {
            var parser = new RobotsTagParser(Set.of("MyBot-1", "MyBot-2"));
            parser.parse(input);
            assertTrue(parser.getCollectedDirectives().isEmpty());
        }

        @Test
        @DisplayName("should perform case-insensitive user agent matching")
        void caseInsensitiveUserAgents() {
            var parser = new RobotsTagParser(Set.of("MyBot"));
            parser.parse("mybot: index");
            assertEquals(Set.of(INDEX), parser.getCollectedDirectives().withUserAgent().toSet());
        }

        @ParameterizedTest
        @DisplayName("should work with empty user agent groups")
        @MethodSource("emptyUserAgentGroupsArgs")
        void emptyUserAgentGroups(String input, Set<Directive<?>> expectedWithoutUserAgent, Set<Directive<?>> expectedWithUserAgent) {
            var parser = new RobotsTagParser(Set.of("MyBot"));
            parser.parse(input);
            assertEquals(expectedWithoutUserAgent, parser.getCollectedDirectives().withoutUserAgent().toSet());
            assertEquals(expectedWithUserAgent, parser.getCollectedDirectives().withUserAgent().toSet());
        }

        Stream<Arguments> emptyUserAgentGroupsArgs() {
            return Stream.of(
                arguments("MyBot:", Collections.emptySet(), Collections.emptySet()),
                arguments("UnknownBot:", Collections.emptySet(), Collections.emptySet()),
                arguments("index, MyBot:", Set.of(INDEX), Collections.emptySet()),
                arguments("index, UnknownBot:", Set.of(INDEX), Collections.emptySet()),
                arguments("MyBot: MyBot: index", Collections.emptySet(), Set.of(INDEX)),
                arguments("MyBot: UnknownBot: index", Collections.emptySet(), Collections.emptySet()),
                arguments("UnknownBot: MyBot: index", Collections.emptySet(), Set.of(INDEX))
            );
        }

        @ParameterizedTest
        @DisplayName("should parse complex ambiguous directive strings")
        @MethodSource("complexAmbiguousStringsArgs")
        void complexAmbiguousStrings(String input, Set<Directive<?>> expectedWithoutUserAgent, Set<Directive<?>> expectedWithUserAgent) {
            var parser = new RobotsTagParser(Set.of("MyBot"));
            parser.parse(input);
            assertEquals(expectedWithoutUserAgent, parser.getCollectedDirectives().withoutUserAgent().toSet());
            assertEquals(expectedWithUserAgent, parser.getCollectedDirectives().withUserAgent().toSet());
        }

        Stream<Arguments> complexAmbiguousStringsArgs() {
            var unavailableAfter = new Directive<>("unavailable_after", ZonedDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC).toInstant());

            return Stream.of(
                arguments("max-image-preview: large, unavailable_after: Wed, 31 Dec 2025 23:59:59 GMT, index, follow", Set.of(MAX_IMAGE_PREVIEW, unavailableAfter, INDEX, FOLLOW), Collections.emptySet()),
                arguments("UnknownBot: foo, MyBot: index, UnknownBot: bar: 100, baz: 200, MyBot: max-image-preview: large, UnknownBot: foo, bar, baz, MyBot: unavailable_after: Wed, 31 Dec 2025 23:59:59 GMT", Collections.emptySet(), Set.of(INDEX, MAX_IMAGE_PREVIEW, unavailableAfter)),
                arguments("UnknownBot: MyBot: unavailable_after: Wed, 31 Dec 2025 23:59:59 GMT, max-image-preview: large, index", Collections.emptySet(), Set.of(unavailableAfter, MAX_IMAGE_PREVIEW, INDEX)),
                arguments("index, foo: bar, max-image-preview: large, MyBot: follow", Set.of(INDEX), Set.of(FOLLOW))
            );
        }
    }
}
