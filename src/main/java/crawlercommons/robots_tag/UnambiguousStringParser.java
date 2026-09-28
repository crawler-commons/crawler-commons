package crawlercommons.robots_tag;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Parses unambiguous directive strings.
 * <p>
 * A directive string is unambiguous if it does not contain any colons. As such,
 * unambiguous strings can neither contain any key-value directives, nor can
 * they contain directives that only apply to specific robots.
 * <p>
 * An unambiguous string can be treated as a string of comma-separated
 * directives.
 */
public final class UnambiguousStringParser {
    /**
     * Parses unambiguous directive strings.
     * <p>
     * Nothing is done to ensure that the input is unambiguous.
     * <p>
     * The result may contain duplicates.
     */
    public static List<Directive<?>> parse(String input) {
        String[] strings = input.toLowerCase(Locale.ROOT) //"foo, bar, "
            .split(","); //["foo", " bar", " "]

        return Arrays.stream(strings)
            .map(String::trim) //["foo", "bar", ""]
            .filter(string -> !string.isEmpty()) //["foo", "bar"]
            .map(Directive::new)
            .collect(Collectors.toList());
    }
}
