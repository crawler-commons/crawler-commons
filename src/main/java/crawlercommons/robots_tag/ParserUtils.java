package crawlercommons.robots_tag;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ParserUtils {
    /**
     * Returns a function that extracts the value of a desired attribute from an
     * HTML element.
     */
    public static Function<String, Optional<String>> createAttributeGetter(String attributeName) {
        /*
            The regular expression compiled below matches the desired HTML attribute.
            The first capturing group contains the attribute value.

            Things to consider:
                - There is always at least one whitespace character before the attribute name.
                  The regular expression checks for this whitespace character to avoid matching suffixes of longer attribute names.
                - Attribute names are case-insensitive.
                - There may be whitespace characters around the equals sign that separates the attribute name from the attribute value.
                - Attribute values can be double-quoted, single-quoted or unquoted.

            Note: The regular expression does not quote the attribute name (see Pattern.quote()), so it might not work with attribute names that contain regular expression metacharacters.

            Reference: WHATWG HTML Standard: https://html.spec.whatwg.org/multipage/syntax.html (→ 13.1.2.3 Attributes)
         */
        Pattern attributeRegex = Pattern.compile("(?i)\\s" + attributeName + "\\s*=\\s*(\"[^\"]+\"|'[^']+'|[^\\s\"'=<>`]+)");

        return (String htmlElement) -> {
            Matcher matcher = attributeRegex.matcher(htmlElement);

            if (matcher.find()) { //Side effect!
                String value = matcher.group(1); //The group at index 0 contains the entire match. The first capturing group is at index 1.
                char firstChar = value.charAt(0);

                if (firstChar == '"' || firstChar == '\'') {
                    return Optional.of(value.substring(1, value.length() - 1)); //"'foo'" → "foo"
                } else {
                    return Optional.of(value);
                }
            } else {
                return Optional.empty();
            }
        };
    }

    /**
     * Removes the first token and everything up to the first regular expression
     * match from a {@link PreprocessedString}.
     * <p>
     * Returns an empty string if the regular expression finds no match.
     */
    public static String dropUntilFirstMatch(Pattern regex, PreprocessedString preprocessed) {
        return preprocessed.getTail()
            .map(tail -> {
                Matcher matcher = regex.matcher(tail);

                if (matcher.find()) { //Side effect!
                    return tail.substring(matcher.start());
                } else {
                    return "";
                }
            })
            .orElse("");
    }

    /**
     * Returns the index of the first comma in a string, or
     * {@code string.length()} if the string contains no commas.
     */
    public static int findFirstComma(String string) {
        int index = string.indexOf(',');

        if (index == -1) { // The string does not contain any commas.
            return string.length();
        } else {
            return index;
        }
    }

    /**
     * Normalizes a product token by trimming and lowercasing it.
     */
    public static String normalizeProductToken(String productToken) {
        return productToken.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns a case-insensitive regular expression that matches all elements
     * of a collection.
     * <p>
     * If the collection is empty, the resulting regular expression matches
     * nothing.
     * <p>
     * Nothing is done to eliminate duplicates in the collection.
     */
    public static Pattern regexForCollectionElements(Collection<String> collection) {
        if (collection.isEmpty()) {
            return Pattern.compile("(?!)"); //This regular expression matches nothing (not even the empty string).
        } else {
            String group = collection.stream() //["foo-bar", "baz", ...]
                .map(Pattern::quote) //["\Qfoo-bar\E", "\Qbaz\E", ...]
                .collect(Collectors.joining("|", "(?:", ")")); //"(?:\Qfoo-bar\E|\Qbaz\E|...)"

            return Pattern.compile("(?i)" + group);
        }
    }

    /**
     * Removes any leading commas and whitespace characters from a string.
     */
    public static String removeUnnecessaryLeadingCharacters(String input) {
        if (!input.isEmpty() && isUnnecessaryCharacter(input.charAt(0))) {
            var index = 0;

            while (index < input.length() && isUnnecessaryCharacter(input.charAt(index))) {
                index++;
            }

            return input.substring(index);
        } else {
            return input;
        }
    }

    private static boolean isUnnecessaryCharacter(char character) {
        return Character.isWhitespace(character) || character == ',';
    }
}
