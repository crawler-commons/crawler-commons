package crawlercommons.robots_tag.parsers;

import crawlercommons.robots_tag.*;

/**
 * Parses directives with {@link String} values (e.g. {@code max-image-preview: none}).
 * <p>
 * Everything up to the first comma is considered to be part of the directive value.
 * <p>
 * The directive value is trimmed, but not modified otherwise (i.e. it is not transformed to uppercase or lowercase).
 */
public final class StringDirectiveParser implements DirectiveParser<String> {
    private static final StringDirectiveParser SINGLETON = new StringDirectiveParser();

    @Override
    public ParserResult<Directive<String>> parse(PreprocessedString input) {
        return input.getTail().map(tail -> {
            int endIndex = ParserUtils.findFirstComma(tail);
            String string = tail.substring(0, endIndex).trim();

            if (string.isEmpty()) {
                throw new ParserException("Failed to parse key-value directive due to missing value");
            }

            var directive = new Directive<>(input.getFirstToken(), string);
            String remainder = tail.substring(endIndex);

            return new ParserResult<>(directive, remainder);
        }).orElseThrow(() -> new ParserException("Failed to parse key-value directive due to missing value"));
    }

    public static StringDirectiveParser getSingleton() {
        return SINGLETON;
    }
}
