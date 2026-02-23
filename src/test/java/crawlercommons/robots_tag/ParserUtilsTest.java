package crawlercommons.robots_tag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("ParserUtils")
class ParserUtilsTest {
    @Nested
    @DisplayName("createAttributeGetter()")
    class CreateAttributeGetter {
        Function<String, Optional<String>> getBarFunction = ParserUtils.createAttributeGetter("bar");

        @ParameterizedTest
        @DisplayName("should work with different HTML attribute syntaxes")
        @ValueSource(strings = {
                        // Double-quoted attribute values:
                        "<foo bar=\"baz\">", "<foo bar = \"baz\">",
                        // Single-quoted attribute values:
                        "<foo bar='baz'>", "<foo bar = 'baz'>",
                        // Unquoted attribute values:
                        "<foo bar=baz>", "<foo bar = baz>" })
        void syntaxes(String input) {
            assertEquals("baz", getBarFunction.apply(input).orElseThrow());
        }

        @ParameterizedTest
        @DisplayName("should ignore matching substrings of longer attribute names")
        @ValueSource(strings = {
                        // "bar" is just a substring of a longer attribute name
                        // here, so it should be ignored:
                        "<foo barber='baz'>", "<foo embark='baz'>", "<foo sidebar='baz'>", "<foo x-bar='baz'>", "<foo x-bar-x='baz'>", "<foo bar-x='baz'>" })
        void substrings(String input) {
            assertTrue(getBarFunction.apply(input).isEmpty());
        }
    }

    @Nested
    @DisplayName("dropUntilFirstMatch()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    // @TestInstance is required for @MethodSource because Java 11 does not
    // support static methods in inner classes.
    class DropUntilFirstMatch {
        final Pattern regex = Pattern.compile("bar");

        @ParameterizedTest
        @DisplayName("should remove everything up to the first regex match")
        @MethodSource("testArgs")
        void test(String input, String expected) {
            assertEquals(expected, ParserUtils.dropUntilFirstMatch(regex, new PreprocessedString(input)));
        }

        Stream<Arguments> testArgs() {
            return Stream.of(
            // Input with regex matches:
                            arguments("foo, bar", "bar"), arguments("foo, bar, baz", "bar, baz"), arguments("foo, bar, baz, bar, baz", "bar, baz, bar, baz"),
                            // Input without regex matches:
                            arguments("foo", ""), arguments("foo, baz", ""));
        }
    }

    @Nested
    @DisplayName("findFirstComma()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class FindFirstComma {
        @ParameterizedTest
        @DisplayName("should find the index of the first comma")
        @MethodSource("testArgs")
        void test(String input, int expected) {
            assertEquals(expected, ParserUtils.findFirstComma(input));
        }

        Stream<Arguments> testArgs() {
            return Stream.of(
            // Input without commas:
                            arguments("", 0), arguments("foo", 3),
                            // Input with commas:
                            arguments(",", 0), arguments("foo, bar", 3), arguments("foo, bar, baz", 3));
        }
    }

    @Nested
    @DisplayName("normalizeProductToken()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class NormalizeProductToken {
        @ParameterizedTest
        @DisplayName("should normalize product tokens")
        @MethodSource("testArgs")
        void test(String input, String expected) {
            assertEquals(expected, ParserUtils.normalizeProductToken(input));
        }

        Stream<Arguments> testArgs() {
            return Stream.of(arguments("foo", "foo"), arguments("Bar", "bar"), arguments(" baz ", "baz"));
        }
    }

    @Nested
    @DisplayName("regexForCollectionElements()")
    class RegexForCollectionElements {
        @Test
        @DisplayName("should create a regex that matches all collection elements")
        void test() {
            assertFalse(ParserUtils.regexForCollectionElements(Collections.emptyList()).matcher("foo bar baz").find());

            List<String> list = List.of("foo", "bar", "baz");
            Pattern regex = ParserUtils.regexForCollectionElements(list);

            List<String> matches = regex.matcher("abc foo def bar ghi baz jkl").results()
                .map(MatchResult::group)
                .collect(Collectors.toList());

            assertEquals(list, matches);
            assertFalse(regex.matcher("abc def ghi jkl").find());

            list.forEach(element -> {
                assertTrue(regex.matcher(element).matches());
                assertTrue(regex.matcher(element.toUpperCase(Locale.ROOT)).matches());
            });
        }
    }

    @Nested
    @DisplayName("removeUnnecessaryLeadingCharacters()")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class RemoveUnnecessaryLeadingCharacters {
        @ParameterizedTest
        @DisplayName("should remove leading leading commas and whitespace characters")
        @MethodSource("testArgs")
        void test(String input, String expected) {
            assertEquals(expected, ParserUtils.removeUnnecessaryLeadingCharacters(input));
        }

        Stream<Arguments> testArgs() {
            return Stream.of(
            // Input without leading clutter:
                            arguments("", ""), arguments("foo", "foo"), arguments("foo, bar", "foo, bar"), arguments("foo, bar, ", "foo, bar, "),
                            // Input with leading clutter:
                            arguments(" , ", ""), arguments(", foo", "foo"), arguments(", foo, bar", "foo, bar"), arguments(", foo, bar, ", "foo, bar, "));
        }
    }
}
