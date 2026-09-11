package crawlercommons.robots_tag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("PreprocessedString")
class PreprocessedStringTest {
    static void test(String input, PreprocessedString expected) {
        PreprocessedString actual = new PreprocessedString(input);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("should work with input that only contains a single token")
    void singleToken() {
        String input = "token";
        var expected = new PreprocessedString(input, "token", 5, Optional.empty(), Optional.empty());
        test(input, expected);
    }

    @Test
    @DisplayName("should work with input that contains multiple comma-separated tokens (but no colons)")
    void multipleTokensComma() {
        String input = "token-1, token-2, token-3";
        var expected = new PreprocessedString(input, "token-1", 7, ',', "token-2, token-3");
        test(input, expected);
    }

    @Test
    @DisplayName("should work with input that contains two colon-separated tokens (but no commas)")
    void multipleTokensColon() {
        String input = "key: value";
        var expected = new PreprocessedString(input, "key", 3, ':', "value");
        test(input, expected);
    }

    @Test
    @DisplayName("should work with input that contains both comma- and colon-separated tokens (comma first)")
    void multipleTokensMixedCommaFirst() {
        String input = "first-token, second-token: third-token";
        var expected = new PreprocessedString(input, "first-token", 11, ',', "second-token: third-token");
        test(input, expected);
    }

    @Test
    @DisplayName("should work with input that contains both comma- and colon-separated tokens (colon first)")
    void multipleTokensMixedColonFirst() {
        String input = "first-token: second-token, third-token";
        var expected = new PreprocessedString(input, "first-token", 11, ':', "second-token, third-token");
        test(input, expected);
    }

    @Test
    @DisplayName("should trim and lowercase the first token")
    void normalizeFirstToken() {
        String input = " TOKEN_1 , TOKEN_2 ";
        var expected = new PreprocessedString(input, "token_1", 9, ',', "TOKEN_2");
        test(input, expected);
    }

    @Test
    @DisplayName("should trim the tail")
    void trimTail() {
        String input = " message : Hello World! ";
        var expected = new PreprocessedString(input, "message", 9, ':', "Hello World!");
        test(input, expected);
    }

    @ParameterizedTest
    @DisplayName("should not be affected by the absence of whitespace")
    @MethodSource("whitespaceArgs")
    void whitespace(String input, PreprocessedString expected) {
        test(input, expected);
    }

    static Stream<Arguments> whitespaceArgs() {
        return Stream.of(arguments("a,b,c", new PreprocessedString("a,b,c", "a", 1, ',', "b,c")), arguments("a:b:c", new PreprocessedString("a:b:c", "a", 1, ':', "b:c")));
    }

    @ParameterizedTest
    @DisplayName("should handle orphan delimiters")
    @MethodSource("orphanDelimitersArgs")
    void orphanDelimiters(String input, PreprocessedString expected) {
        test(input, expected);
    }

    static Stream<Arguments> orphanDelimitersArgs() {
        return Stream.of(arguments("token,", new PreprocessedString("token,", "token", 5, Optional.of(','), Optional.empty())),
                        arguments("token:", new PreprocessedString("token:", "token", 5, Optional.of(':'), Optional.empty())),
                        arguments("a: , b, c", new PreprocessedString("a: , b, c", "a", 1, ':', ", b, c")));
    }
}
