package crawlercommons.robots_tag;

import java.util.Objects;

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

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ParserResult)) return false;
        ParserResult<?> other = (ParserResult<?>) object;
        return Objects.equals(value, other.value) && Objects.equals(remainder, other.remainder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, remainder);
    }

    @Override
    public String toString() {
        return "ParserResult(" + value + ", \"" + remainder + "\")";
    }
}
