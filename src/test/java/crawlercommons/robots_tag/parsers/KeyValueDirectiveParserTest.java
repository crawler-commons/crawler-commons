package crawlercommons.robots_tag.parsers;

import crawlercommons.robots_tag.Directive;
import crawlercommons.robots_tag.DirectiveParser;
import crawlercommons.robots_tag.ParserResult;
import crawlercommons.robots_tag.PreprocessedString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Common tests for key-value directive parsers.
 *
 * @param <T> the type of directive value produced by the {@link DirectiveParser} under test
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //Required for @MethodSource.
interface KeyValueDirectiveParserTest<T> {
    /**
     * Returns a {@link DirectiveParser} instance to test.
     */
    DirectiveParser<T> provideDirectiveParser();

    /**
     * Returns the arguments for the tests. Each {@link Arguments} instance must consist of three elements:
     * <ol>
     *     <li>{@link String} Directive Name</li>
     *     <li>{@link String} Directive Value</li>
     *     <li>{@link T} Expected Value</li>
     * </ol>
     * <p>
     * The input for the parser is constructed from the first two elements of the {@link Arguments} instance.
     */
    Stream<Arguments> provideTestArguments();

    @ParameterizedTest
    @DisplayName("should work with input that only contains a single directive")
    @MethodSource("provideTestArguments")
    default void singleDirective(String key, String value, T expectedValue) {
        DirectiveParser<T> parser = provideDirectiveParser();
        String input = key + ": " + value;
        var expected = new ParserResult<>(new Directive<>(key, expectedValue), "");
        var actual = parser.parse(new PreprocessedString(input));
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @DisplayName("should work with input that contains multiple directives")
    @MethodSource("provideTestArguments")
    default void multipleDirectives(String key, String value, T expectedValue) {
        DirectiveParser<T> parser = provideDirectiveParser();

        List<String> suffixes = List.of(
            "foo",
            "foo, bar, baz",
            "foo: bar, baz",
            "foo, bar: baz",
            key + ": " + value //The input ends up containing the same directive twice.
        );

        suffixes.forEach(suffix -> {
            String input = key + ": " + value + ", " + suffix;
            var expected = new ParserResult<>(new Directive<>(key, expectedValue), ", " + suffix);
            var actual = parser.parse(new PreprocessedString(input));
            assertEquals(expected, actual);
        });
    }

    @ParameterizedTest
    @DisplayName("should not be affected by whitespace")
    @MethodSource("provideTestArguments")
    default void whitespace(String key, String value, T expectedValue) {
        DirectiveParser<T> parser = provideDirectiveParser();

        List<String> inputs = List.of(
            key + ':' + value, //No whitespace
            "   " + key + "   :   " + value + "   " //Too much whitespace
        );

        inputs.forEach(input -> {
            var expected = new ParserResult<>(new Directive<>(key, expectedValue), "");
            var actual = parser.parse(new PreprocessedString(input));
            assertEquals(expected, actual);
        });
    }

    @ParameterizedTest
    @DisplayName("should throw an exception if the value is missing")
    @ValueSource(strings = {
        "foo:",
        "foo: , bar"
    })
    default void missingValue(String input) {
        DirectiveParser<T> parser = provideDirectiveParser();
        assertThrows(Exception.class, () -> parser.parse(new PreprocessedString(input)));
    }
}
