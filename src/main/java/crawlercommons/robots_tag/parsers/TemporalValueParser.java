package crawlercommons.robots_tag.parsers;

import crawlercommons.robots_tag.ParserResult;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uses a {@link DateTimeFormatter} to parse temporal values (e.g. {@code 2025-12-31T23:59:59}).
 *
 * @param <T> the type of temporal value produced by this {@link TemporalValueParser} (e.g. {@link LocalDate})
 */
public final class TemporalValueParser<T> {
    /**
     * A regular expression that matches strings that can be parsed by the formatter.
     */
    private final Pattern regex;

    /**
     * The {@link DateTimeFormatter} used for parsing.
     */
    private final DateTimeFormatter formatter;

    /**
     * A function that allows the formatter to produce an instance of {@link T} (e.g. {@link LocalDate#from(TemporalAccessor)}).
     */
    private final TemporalQuery<T> temporalQuery;

    public TemporalValueParser(Pattern regex, DateTimeFormatter formatter, TemporalQuery<T> temporalQuery) {
        this.regex = regex;
        this.formatter = formatter;
        this.temporalQuery = temporalQuery;
    }

    /**
     * Tries to parse the first temporal value from a string.
     *
     * @param input The string to parse. The temporal value must be located at the beginning of the string.
     * @return the parsed value and the input string without the parsed value, or {@link Optional#empty()} if the regular expression did not find a match
     * @throws DateTimeParseException if the {@link DateTimeFormatter} throws one while parsing
     */
    public Optional<ParserResult<T>> parse(String input) {
        Matcher matcher = regex.matcher(input);

        if (matcher.find()) { //Side effect!
            return Optional.ofNullable(matcher.group())
                .map(match -> {
                    T temporal = formatter.parse(match, temporalQuery);
                    String remainder = input.substring(match.length());
                    return new ParserResult<>(temporal, remainder);
                });
        } else {
            return Optional.empty();
        }
    }

    public Pattern getRegex() {
        return regex;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public TemporalQuery<T> getTemporalQuery() {
        return temporalQuery;
    }

    /**
     * Parses ISO 8601 dates without offset (e.g. {@code 2025-12-31}).
     */
    public static final TemporalValueParser<LocalDate> ISO_LOCAL_DATE = new TemporalValueParser<>(
        Pattern.compile("(?i)^(?:-?\\d{4}|[+-]\\d{5,9})-\\d{2}-\\d{2}"),
        DateTimeFormatter.ISO_LOCAL_DATE,
        LocalDate::from
    );

    /**
     * Parses ISO 8601 dates with offset (e.g. {@code 2025-12-31+01:00}).
     * <p>
     * Offsets without offset minutes can not be parsed.
     *
     * @implSpec The beginning of the regular expression used by this {@link TemporalValueParser} should be equal to the regular expression used by {@link #ISO_LOCAL_DATE}.
     * @see java.time.ZoneId
     */
    public static final TemporalValueParser<LocalDate> ISO_OFFSET_DATE = new TemporalValueParser<>(
        Pattern.compile("(?i)^(?:-?\\d{4}|[+-]\\d{5,9})-\\d{2}-\\d{2}(?:Z|[+-]\\d{2}:\\d{2}(?::\\d{2})?)"),
        DateTimeFormatter.ISO_OFFSET_DATE, //Combining a date (without a time) and an offset does not make sense; this DateTimeFormatter parses and discards the offset.
        LocalDate::from
    );

    /**
     * Parses ISO 8601 date-times without offset (e.g. {@code 2025-12-31T23:59:59}).
     *
     * @implSpec The beginning of the regular expression used by this {@link TemporalValueParser} should be equal to the regular expression used by {@link #ISO_LOCAL_DATE}.
     */
    public static final TemporalValueParser<LocalDateTime> ISO_LOCAL_DATE_TIME = new TemporalValueParser<>(
        Pattern.compile("(?i)^(?:-?\\d{4}|[+-]\\d{5,9})-\\d{2}-\\d{2}T\\d{2}:\\d{2}(?::\\d{2}(?:\\.\\d{1,9})?)?"),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        LocalDateTime::from
    );

    /**
     * Parses ISO 8601 date-times with offset (e.g. {@code 2025-12-31T23:59:59+01:00}).
     *
     * @implSpec The beginning of the regular expression used by this {@link TemporalValueParser} should be equal to the regular expression used by {@link #ISO_LOCAL_DATE_TIME}.
     * @see java.time.ZoneId
     */
    public static final TemporalValueParser<Instant> ISO_OFFSET_DATE_TIME = new TemporalValueParser<>(
        Pattern.compile("(?i)^(?:-?\\d{4}|[+-]\\d{5,9})-\\d{2}-\\d{2}T\\d{2}:\\d{2}(?::\\d{2}(?:\\.\\d{1,9})?)?(?:Z|[+-]\\d{2}(?::\\d{2}){0,2})"),
        DateTimeFormatter.ISO_OFFSET_DATE_TIME,
        Instant::from
    );

    /**
     * Parses ISO 8601-like date-times with offset and time zone (e.g. {@code 2025-12-31T23:59:59+01:00[Europe/Berlin]}).
     * <p>
     * This {@link TemporalValueParser} is designed to use time zone identifiers from the IANA Time Zone Database.
     *
     * @implSpec The beginning of the regular expression used by this {@link TemporalValueParser} should be equal to the regular expression used by {@link #ISO_OFFSET_DATE_TIME}.
     * @see java.time.ZoneId
     */
    public static final TemporalValueParser<Instant> ISO_ZONED_DATE_TIME = new TemporalValueParser<>(
        Pattern.compile("(?i)^(?:-?\\d{4}|[+-]\\d{5,9})-\\d{2}-\\d{2}T\\d{2}:\\d{2}(?::\\d{2}(?:\\.\\d{1,9})?)?(?:Z|[+-]\\d{2}(?::\\d{2}){0,2})\\[[\\w/+-]+]"),
        DateTimeFormatter.ISO_ZONED_DATE_TIME, //This DateTimeFormatter parses and ignores the offset in favor of the time zone.
        Instant::from
    );

    /**
     * Parses ISO 8601 basic local dates with optional offset (e.g. {@code 20251231} or {@code 20251231+0100}).
     *
     * @see java.time.ZoneId
     */
    public static final TemporalValueParser<LocalDate> ISO_BASIC_LOCAL_DATE = new TemporalValueParser<>(
        Pattern.compile("(?i)^\\d{8}(?:Z|[+-](?:\\d{6}|\\d{4}|\\d{2}))?"),
        DateTimeFormatter.BASIC_ISO_DATE, //Combining a date (without a time) and an offset does not make sense; this DateTimeFormatter parses and discards the offset (if present).
        LocalDate::from
    );

    /**
     * Parses ISO 8601 ordinal dates with optional offset (e.g. {@code 2025-365} or {@code 2025-365+01:00}).
     * <p>
     * Offsets without offset minutes can not be parsed.
     *
     * @see java.time.ZoneId
     */
    public static final TemporalValueParser<LocalDate> ISO_ORDINAL_DATE = new TemporalValueParser<>(
        Pattern.compile("(?i)^(?:-?\\d{4}|[+-]\\d{5,9})-\\d{3}(?:Z|[+-]\\d{2}:\\d{2}(?::\\d{2})?)?"),
        DateTimeFormatter.ISO_ORDINAL_DATE, //Combining a date (without a time) and an offset does not make sense; this DateTimeFormatter parses and discards the offset (if present).
        LocalDate::from
    );

    /**
     * Parses ISO 8601 week-based dates with optional offset (e.g. {@code 2026-W01-3} or {@code 2026-W01-3+01:00}).
     * <p>
     * Offsets without offset minutes can not be parsed.
     *
     * @see java.time.ZoneId
     */
    public static final TemporalValueParser<LocalDate> ISO_WEEK_DATE = new TemporalValueParser<>(
        Pattern.compile("(?i)^(?:-?\\d{4}|[+-]\\d{5,9})-W\\d{2}-\\d(?:Z|[+-]\\d{2}:\\d{2}(?::\\d{2})?)?"),
        DateTimeFormatter.ISO_WEEK_DATE, //Combining a date (without a time) and an offset does not make sense; this DateTimeFormatter parses and discards the offset (if present).
        LocalDate::from
    );

    /**
     * Parses RFC 1123 date-times (e.g. {@code Wed, 31 Dec 2025 23:59:59 GMT}).
     *
     * @see java.time.ZoneId
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc1123">RFC 1123 - Requirements for Internet Hosts - Application and Support</a>
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc822">RFC 822 - Standard for the Format of ARPA Internet Text Messages</a>
     */
    public static final TemporalValueParser<Instant> RFC_1123_DATE_TIME = new TemporalValueParser<>(
        Pattern.compile("(?i)^(?:[A-Za-z]{3}, )?\\d{1,2} [A-Za-z]{3} \\d{4} \\d{2}:\\d{2}(?::\\d{2})? (?:GMT|[+-](?:\\d{4}|\\d{2}))"),
        DateTimeFormatter.RFC_1123_DATE_TIME,
        Instant::from
    );
}
