package crawlercommons.robots_tag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("UnambiguousStringParser")
class UnambiguousStringParserTest {
    static final Directive<Void> FOO = new Directive<>("foo");
    static final Directive<Void> BAR = new Directive<>("bar");
    static final Directive<Void> BAZ = new Directive<>("baz");

    @Test
    @DisplayName("should work with empty input")
    void emptyInput() {
        assertTrue(UnambiguousStringParser.parse("").isEmpty());
    }

    @ParameterizedTest
    @DisplayName("should work with input that only contains a single directive")
    @ValueSource(strings = {
        "foo",
        "foo-123",
        "foo_123"
    })
    void singleDirective(String input) {
        var expected = List.of(new Directive<>(input));
        assertEquals(expected, UnambiguousStringParser.parse(input));
    }

    @Test
    @DisplayName("should work with input that contains multiple directives")
    void multipleDirectives() {
        String input = "foo, bar, baz";
        var expected = List.of(FOO, BAR, BAZ);
        assertEquals(expected, UnambiguousStringParser.parse(input));
    }

    @Test
    @DisplayName("should trim and lowercase directive names")
    void normalizeDirectiveNames() {
        String input = " FOO , Bar , baz ";
        var expected = List.of(FOO, BAR, BAZ);
        assertEquals(expected, UnambiguousStringParser.parse(input));
    }

    @ParameterizedTest
    @DisplayName("should work with input that contains excess commas")
    @MethodSource("excessCommasArgs")
    void excessCommas(String input, List<Directive<?>> expected) {
        assertEquals(expected, UnambiguousStringParser.parse(input));
    }

    static Stream<Arguments> excessCommasArgs() {
        return Stream.of(
            arguments(",", Collections.emptyList()),
            arguments(",,,", Collections.emptyList()),
            arguments("foo,", List.of(FOO)),
            arguments(", foo", List.of(FOO)),
            arguments("foo, , bar,", List.of(FOO, BAR))
        );
    }
}
