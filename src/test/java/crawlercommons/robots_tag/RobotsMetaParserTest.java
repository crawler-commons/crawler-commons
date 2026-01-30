package crawlercommons.robots_tag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static crawlercommons.robots_tag.KnownSimpleDirectives.FOLLOW;
import static crawlercommons.robots_tag.KnownSimpleDirectives.INDEX;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("RobotsMetaParser")
class RobotsMetaParserTest {
    static final Directive<String> MAX_IMAGE_PREVIEW = new Directive<>("max-image-preview", "large");
    static final Directive<LocalDate> UNAVAILABLE_AFTER = new Directive<>("unavailable_after", LocalDate.of(2025, 12, 31));

    @Test
    @DisplayName("should initialize and reset properly")
    void initializeAndReset() {
        var parser = new RobotsMetaParser();
        assertTrue(parser.getCollectedDirectives().isEmpty());

        parser.parse("<meta name='robots' content='index, follow'>");
        assertFalse(parser.getCollectedDirectives().isEmpty());

        parser.reset();
        assertTrue(parser.getCollectedDirectives().isEmpty());
    }

    @ParameterizedTest
    @DisplayName("should work with empty inputs")
    @ValueSource(strings = {
        "",
        "<meta>",
        "<meta name='' content='index, follow'>",
        "<meta name='robots' content=''>"
    })
    void emptyInputs(String input) {
        var parser = new RobotsMetaParser();
        parser.parse(input);
        assertTrue(parser.getCollectedDirectives().isEmpty());
    }

    @Test
    @DisplayName("should parse individual directives")
    void parseIndividualDirectives() {
        var parser = new RobotsMetaParser();

        parser.parse("<meta name='robots' content='index'>");
        assertEquals(Set.of(INDEX), parser.getCollectedDirectives().withoutUserAgent().toSet());

        parser.parse("<meta name='robots' content='max-image-preview: large'>");
        assertEquals(Set.of(INDEX, MAX_IMAGE_PREVIEW), parser.getCollectedDirectives().withoutUserAgent().toSet());
    }

    @Test
    @DisplayName("should parse multiple directives")
    void parseMultipleDirectives() {
        var parser = new RobotsMetaParser();

        parser.parse("<meta name='robots' content='index, follow'>");
        assertEquals(Set.of(INDEX, FOLLOW), parser.getCollectedDirectives().withoutUserAgent().toSet());

        parser.parse("<meta name='robots' content='max-image-preview: large, unavailable_after: 2025-12-31'>");
        assertEquals(Set.of(INDEX, FOLLOW, MAX_IMAGE_PREVIEW, UNAVAILABLE_AFTER), parser.getCollectedDirectives().withoutUserAgent().toSet());

        parser.reset();
        parser.parse("<meta name='robots' content='max-image-preview: large, index, unavailable_after: 2025-12-31, follow'>");
        assertEquals(Set.of(INDEX, FOLLOW, MAX_IMAGE_PREVIEW, UNAVAILABLE_AFTER), parser.getCollectedDirectives().withoutUserAgent().toSet());
    }

    @ParameterizedTest
    @DisplayName("should parse unknown simple directives under certain conditions")
    @ValueSource(strings = {
        //Unambiguous directive strings:
        "<meta name='robots' content='foo-123'>",
        "<meta name='robots' content='foo-123, follow'>", //First
        "<meta name='robots' content='index, foo-123, follow'>", //Middle
        "<meta name='robots' content='index, foo-123'>", //Last
        //Ambiguous directive strings:
        "<meta name='robots' content='foo-123, unavailable_after: 2025-12-31'>", //First
        "<meta name='robots' content='max-image-preview: large, foo-123, unavailable_after: 2025-12-31'>", //Middle
        "<meta name='robots' content='max-image-preview: large, foo-123'>" //Last
    })
    void parseSimpleDirectives(String input) {
        var parser = new RobotsMetaParser();
        parser.parse(input);
        assertTrue(parser.getCollectedDirectives().withoutUserAgent().toSet().contains(new Directive<>("foo-123")));
    }

    @Test
    @DisplayName("should trim and lowercase directive names")
    void normalizeDirectiveNames() {
        var parser = new RobotsMetaParser();
        parser.parse("<meta name='robots' content=' Index, FOLLOW '>");
        assertEquals(Set.of(INDEX, FOLLOW), parser.getCollectedDirectives().withoutUserAgent().toSet());
    }

    @Test
    @DisplayName("should eliminate duplicate directives")
    void eliminateDuplicateDirectives() {
        var parser = new RobotsMetaParser();

        //Unambiguous directive strings:
        parser.parse("<meta name='robots' content='index, follow, index'>");
        assertEquals(Set.of(INDEX, FOLLOW), parser.getCollectedDirectives().withoutUserAgent().toSet());

        parser.parse("<meta name='robots' content='follow'>");
        assertEquals(Set.of(INDEX, FOLLOW), parser.getCollectedDirectives().withoutUserAgent().toSet());

        //Ambiguous directive strings:
        parser.reset();
        parser.parse("<meta name='robots' content='index, max-image-preview: large, index'>");
        assertEquals(Set.of(INDEX, MAX_IMAGE_PREVIEW), parser.getCollectedDirectives().withoutUserAgent().toSet());

        parser.parse("<meta name='robots' content='max-image-preview: large'>");
        assertEquals(Set.of(INDEX, MAX_IMAGE_PREVIEW), parser.getCollectedDirectives().withoutUserAgent().toSet());
    }

    @ParameterizedTest
    @DisplayName("should work with input that contains excess commas")
    @MethodSource("excessCommasArgs")
    void excessCommas(String input, Set<Directive<?>> expected) {
        var parser = new RobotsMetaParser();
        parser.parse(input);
        assertEquals(expected, parser.getCollectedDirectives().withoutUserAgent().toSet());
        assertTrue(parser.getCollectedDirectives().withUserAgent().toMap().isEmpty());
    }

    static Stream<Arguments> excessCommasArgs() {
        return Stream.of(
            //No directives:
            arguments("<meta name='robots' content=','>", Collections.emptySet()),
            arguments("<meta name='robots' content=',,,'>", Collections.emptySet()),
            //Unambiguous directive strings:
            arguments("<meta name='robots' content='index,'>", Set.of(INDEX)),
            arguments("<meta name='robots' content=', index'>", Set.of(INDEX)),
            arguments("<meta name='robots' content='index, , follow,'>", Set.of(INDEX, FOLLOW)),
            //Ambiguous directive strings:
            arguments("<meta name='robots' content='max-image-preview: large,'>", Set.of(MAX_IMAGE_PREVIEW)),
            arguments("<meta name='robots' content=', max-image-preview: large'>", Set.of(MAX_IMAGE_PREVIEW)),
            arguments("<meta name='robots' content='max-image-preview: large, , unavailable_after: 2025-12-31,'>", Set.of(MAX_IMAGE_PREVIEW, UNAVAILABLE_AFTER))
        );
    }

    @ParameterizedTest
    @DisplayName("should work with different HTML attribute syntaxes")
    @ValueSource(strings = {
        //Double-quoted attribute values:
        "<meta name=\"robots\" content=\"index, follow\">",
        "<meta name = \"robots\" content=\"index, follow\">",
        //Single-quoted attribute values:
        "<meta name='robots' content='index, follow'>",
        "<meta name = 'robots' content='index, follow'>",
        //Unquoted attribute values:
        "<meta name=robots content=index,follow>",
        "<meta name = robots content = index,follow>"
    })
    void htmlAttributeSyntaxes(String input) {
        var parser = new RobotsMetaParser();
        parser.parse(input);
        assertEquals(Set.of(INDEX, FOLLOW), parser.getCollectedDirectives().withoutUserAgent().toSet());
    }

    @Test
    @DisplayName("should work with unusual HTML attribute orders")
    void htmlAttributeOrders() {
        var parser = new RobotsMetaParser();
        parser.parse("<meta content='index, follow' name='robots'>");
        assertEquals(Set.of(INDEX, FOLLOW), parser.getCollectedDirectives().withoutUserAgent().toSet());
    }

    @Test
    @DisplayName("should parse HTML in a case-insensitive manner")
    void htmlCaseInsensitive() {
        var parser = new RobotsMetaParser();
        parser.parse("<META Name='robots' Content='index'>");
        assertEquals(Set.of(INDEX), parser.getCollectedDirectives().withoutUserAgent().toSet());
    }

    @Test
    @DisplayName("should ignore unrelated HTML attributes")
    void ignoreUnrelatedAttributes() {
        var parser = new RobotsMetaParser();

        parser.parse("<meta some-name='robots' some-content='index, follow'>"); //"name" and "content" are just suffixes of "some-name" and "some-content", so the parser should ignore them.
        parser.parse("<meta foo='bar' baz>");
        assertTrue(parser.getCollectedDirectives().isEmpty());

        parser.parse("<meta foo='bar' name='robots' baz content='index, follow'>");
        assertEquals(Set.of(INDEX, FOLLOW), parser.getCollectedDirectives().withoutUserAgent().toSet());
    }

    @ParameterizedTest
    @DisplayName("should throw exceptions if configured to do so")
    @ValueSource(strings = {
        "<meta name='robots' content='foo: bar, index'>", //"foo" is an unknown key-value directive name.
        "<meta name='robots' content='max-snippet: <invalid value>, follow'>"
    })
    void throwExceptions(String input) {
        var parser = new RobotsMetaParser(Collections.emptySet(), KnownDirectiveParsers.PARSERS_BY_NAME, ExceptionHandlers::throwing);
        assertThrows(ParserException.class, () -> parser.parse(input));
        assertTrue(parser.getCollectedDirectives().isEmpty()); //The "index" and "follow" directives were not parsed because the exception handler threw the exceptions.
    }

    @ParameterizedTest
    @DisplayName("should recover from parsing failures")
    @MethodSource("recoverFromParsingFailuresArgs")
    void recoverFromParsingFailures(String input, Set<Directive<?>> expected) {
        var parser = new RobotsMetaParser();
        parser.parse(input);
        assertEquals(expected, parser.getCollectedDirectives().withoutUserAgent().toSet());
        assertTrue(parser.getCollectedDirectives().withUserAgent().toMap().isEmpty());
    }

    static Stream<Arguments> recoverFromParsingFailuresArgs() {
        return Stream.of(
            //The first token is part of an unknown key-value directive (skip to the next known directive name):
            arguments("<meta name='robots' content='foo: bar, index'>", Set.of(INDEX)),
            arguments("<meta name='robots' content='foo: bar, max-image-preview: large'>", Set.of(MAX_IMAGE_PREVIEW)),
            //A DirectiveParser throws (skip to the next known directive name):
            arguments("<meta name='robots' content='max-snippet: <invalid value>, index'>", Set.of(INDEX)),
            arguments("<meta name='robots' content='max-snippet: <invalid value>, max-image-preview: large'>", Set.of(MAX_IMAGE_PREVIEW))
        );
    }

    @Nested
    @DisplayName("without target user agents")
    class WithoutTargetUserAgents {
        @Test
        @DisplayName("should collect directives that apply to all user agents")
        void collectForAllUserAgents() {
            var parser = new RobotsMetaParser();
            parser.parse("<meta name='robots' content='index'>");
            assertEquals(Set.of(INDEX), parser.getCollectedDirectives().withoutUserAgent().toSet());
        }

        @Test
        @DisplayName("should not collect directives that only apply to specific user agents")
        void ignoreForSpecificUserAgents() {
            var parser = new RobotsMetaParser();
            parser.parse("<meta name='UnknownBot' content='index'>");
            assertTrue(parser.getCollectedDirectives().isEmpty());
        }
    }

    @Nested
    @DisplayName("with target user agents")
    class WithTargetUserAgents {
        @Test
        @DisplayName("should collect directives that apply to all user agents")
        void collectForAllUserAgents() {
            var parser = new RobotsMetaParser(Set.of("MyBot-1", "MyBot-2"));
            parser.parse("<meta name='robots' content='index'>");
            assertEquals(Set.of(INDEX), parser.getCollectedDirectives().withoutUserAgent().toSet());
        }

        @Test
        @DisplayName("should collect directives that apply to the target user agents")
        void collectForTargetUserAgents() {
            var parser = new RobotsMetaParser(Set.of("MyBot-1", "MyBot-2"));

            parser.parse("<meta name='MyBot-1' content='index, follow'>");
            assertEquals(Set.of(INDEX, FOLLOW), parser.getCollectedDirectives().withUserAgent().toSet());

            parser.parse("<meta name='MyBot-2' content='max-image-preview: large'>");
            assertEquals(Set.of(INDEX, FOLLOW, MAX_IMAGE_PREVIEW), parser.getCollectedDirectives().withUserAgent().toSet());

            var expectedUserAgentGroups = Map.of(
                "mybot-1", Set.of(INDEX, FOLLOW),
                "mybot-2", Set.of(MAX_IMAGE_PREVIEW)
            );

            assertEquals(expectedUserAgentGroups, parser.getCollectedDirectives().withUserAgent().toMap());
        }

        @Test
        @DisplayName("should not collect directives that only apply to other user agents")
        void ignoreForOtherUserAgents() {
            var parser = new RobotsMetaParser(Set.of("MyBot"));
            parser.parse("<meta name='UnknownBot' content='index'>");
            assertTrue(parser.getCollectedDirectives().isEmpty());
        }

        @Test
        @DisplayName("should perform case-insensitive user agent matching")
        void caseInsensitiveUserAgents() {
            var parser = new RobotsMetaParser(Set.of("MyBot"));
            parser.parse("<meta name='mybot' content='index'>");
            assertEquals(Set.of(INDEX), parser.getCollectedDirectives().withUserAgent().toSet());
        }
    }
}
