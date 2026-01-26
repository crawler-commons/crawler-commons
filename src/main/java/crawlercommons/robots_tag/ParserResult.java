package crawlercommons.robots_tag;

/**
 * A parsed value and the remainder of the string it was parsed from (i.e. the original string that the value was parsed from, but without the parsed value).
 */
public final class ParserResult<T> {
    private final T value;
    private final String remainder;

    public ParserResult(T value, String remainder) {
        this.value = value;
        this.remainder = remainder;
    }

    public T getValue() {
        return value;
    }

    public String getRemainder() {
        return remainder;
    }
}
