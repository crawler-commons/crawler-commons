package crawlercommons.robots_tag.parsers;

import crawlercommons.robots_tag.DirectiveParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.provider.Arguments;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static utils.TemporalUtils.createInstant;

@DisplayName("TemporalDirectiveParser")
class TemporalDirectiveParserTest implements KeyValueDirectiveParserTest<Temporal> {
    /**
     * 2025-01-02
     */
    static final LocalDate EXPECTED_LOCAL_DATE = LocalDate.of(2025, 1, 2);

    /**
     * 2025-01-02 at 03:04:05.0
     */
    static final LocalDateTime EXPECTED_LOCAL_DATE_TIME = LocalDateTime.of(2025, 1, 2, 3, 4, 5);

    @Override
    public DirectiveParser<Temporal> provideDirectiveParser() {
        return TemporalDirectiveParser.getSingleton();
    }

    @Override
    public Stream<Arguments> provideTestArguments() {
        return Stream.of(
            arguments("unavailable_after", "2025-01-02", EXPECTED_LOCAL_DATE), //TemporalValueParser.ISO_LOCAL_DATE
            arguments("unavailable_after", "2025-01-02+03:00", EXPECTED_LOCAL_DATE), //TemporalValueParser.ISO_OFFSET_DATE
            arguments("unavailable_after", "2025-01-02T03:04:05", EXPECTED_LOCAL_DATE_TIME), //TemporalValueParser.ISO_LOCAL_DATE_TIME
            arguments("unavailable_after", "2025-01-02T03:04:05+06:00", createInstant(EXPECTED_LOCAL_DATE_TIME, ZoneOffset.ofHours(6))), //TemporalValueParser.ISO_OFFSET_DATE_TIME
            arguments("unavailable_after", "2025-01-02T03:04:05+06:00[Asia/Thimphu]", createInstant(EXPECTED_LOCAL_DATE_TIME, ZoneId.of("Asia/Thimphu"))), //TemporalValueParser.ISO_ZONED_DATE_TIME
            arguments("unavailable_after", "20250102", EXPECTED_LOCAL_DATE), //TemporalValueParser.ISO_BASIC_LOCAL_DATE
            arguments("unavailable_after", "2025-002", EXPECTED_LOCAL_DATE), //TemporalValueParser.ISO_ORDINAL_DATE
            arguments("unavailable_after", "2025-W01-4", EXPECTED_LOCAL_DATE), //TemporalValueParser.ISO_WEEK_DATE
            arguments("unavailable_after", "Thu, 02 Jan 2025 03:04:05 +0600", createInstant(EXPECTED_LOCAL_DATE_TIME, ZoneOffset.ofHours(6))) //TemporalValueParser.RFC_1123_DATE_TIME
        );
    }
}
