package crawlercommons.robots_tag;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents (part of) a string that is currently being processed by a
 * {@link RobotsMetaParser} or a {@link RobotsTagParser}, along with additional
 * information that is required by various parsing components.
 */
public final class PreprocessedString {
    /**
     * The entire string.
     */
    private final String string;

    /**
     * The first token in the string (i.e. everything up to the first
     * delimiter), all lowercase and trimmed. It can be ambiguous whether this
     * token is a directive name or a product token.
     */
    private final String firstToken;

    /**
     * The index of the first delimiting character in the string, or
     * {@code string.length()} if the string contains no delimiters.
     */
    private final int delimiterIndex;

    /**
     * The first delimiting character in the string. Can be a comma, a colon or
     * {@link Optional#empty()}.
     */
    private final Optional<Character> delimiter;

    /**
     * Everything after the first delimiter (trimmed), or
     * {@link Optional#empty()} if there is no delimiter or if there are no
     * characters after the delimiter.
     */
    private final Optional<String> tail;

    public PreprocessedString(String string, String firstToken, int delimiterIndex, Optional<Character> delimiter, Optional<String> tail) {
        this.string = string;
        this.firstToken = firstToken;
        this.delimiterIndex = delimiterIndex;
        this.delimiter = delimiter;
        this.tail = tail;
    }

    public PreprocessedString(String string, String firstToken, int delimiterIndex, Character delimiter, String tail) {
        this.string = string;
        this.firstToken = firstToken;
        this.delimiterIndex = delimiterIndex;
        this.delimiter = Optional.ofNullable(delimiter);
        this.tail = Optional.ofNullable(tail);
    }

    /**
     * Preprocesses a string to create a new {@link PreprocessedString}.
     */
    public PreprocessedString(String string) {
        // Find the first comma or colon:
        Optional<Character> delimiter = Optional.empty();
        int delimiterIndex = 0;

        while (delimiter.isEmpty() && delimiterIndex < string.length()) {
            char character = string.charAt(delimiterIndex);

            if (character == ',' || character == ':') {
                delimiter = Optional.of(character);
            } else {
                delimiterIndex++;
            }
        }

        // Retrieve and normalize the first token:
        String firstToken = string.substring(0, delimiterIndex).trim().toLowerCase(Locale.ROOT);

        // Retrieve and normalize the tail:
        Optional<String> tail = Optional.empty();

        if (delimiterIndex < string.length()) {
            String substring = string.substring(delimiterIndex + 1).trim();

            if (!substring.isEmpty()) {
                tail = Optional.of(substring);
            }
        }

        this.string = string;
        this.firstToken = firstToken;
        this.delimiterIndex = delimiterIndex;
        this.delimiter = delimiter;
        this.tail = tail;
    }

    public String getString() {
        return string;
    }

    public String getFirstToken() {
        return firstToken;
    }

    public int getDelimiterIndex() {
        return delimiterIndex;
    }

    public Optional<Character> getDelimiter() {
        return delimiter;
    }

    public Optional<String> getTail() {
        return tail;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PreprocessedString))
            return false;
        PreprocessedString other = (PreprocessedString) object;

        return Objects.equals(string, other.string) && Objects.equals(firstToken, other.firstToken) && Objects.equals(delimiterIndex, other.delimiterIndex)
                        && Objects.equals(delimiter, other.delimiter) && Objects.equals(tail, other.tail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string, firstToken, delimiterIndex, delimiter, tail);
    }

    @Override
    public String toString() {
        return "PreprocessedString(" + '"' + string + "\", " + '"' + firstToken + "\", " + delimiterIndex + ", " + delimiter + ", " + tail + ')';
    }
}
