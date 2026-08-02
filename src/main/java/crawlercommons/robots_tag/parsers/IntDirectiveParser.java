package crawlercommons.robots_tag.parsers;

import crawlercommons.robots_tag.*;

/**
 * Parses directives with {@link Integer} values (e.g. {@code max-snippet: 123}
 * ).
 */
public final class IntDirectiveParser implements DirectiveParser<Integer> {
    private static final IntDirectiveParser SINGLETON = new IntDirectiveParser();

    @Override
    public ParserResult<Directive<Integer>> parse(PreprocessedString input) {
        return input.getTail().map(tail -> {
            int endIndex = ParserUtils.findFirstComma(tail);
            String value = tail.substring(0, endIndex).trim(); //Integer.parseInt() throws an exception if it encounters whitespace. There may be whitespace between the directive value and the first comma, so the string must be trimmed.

            if (value.isEmpty()) {
                throw new ParserException("Failed to parse key-value directive due to missing value");
            }

            int number = Integer.parseInt(value, 10);
            var directive = new Directive<>(input.getFirstToken(), number);
            String remainder = tail.substring(endIndex);

            return new ParserResult<>(directive, remainder);
        }).orElseThrow(() -> new ParserException("Failed to parse key-value directive due to missing value"));
    }

    public static IntDirectiveParser getSingleton() {
        return SINGLETON;
    }
}
