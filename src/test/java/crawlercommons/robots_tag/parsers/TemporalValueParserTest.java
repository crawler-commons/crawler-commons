package crawlercommons.robots_tag.parsers;

import crawlercommons.robots_tag.ParserResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.*;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static utils.TemporalUtils.createInstant;

@DisplayName("TemporalValueParser")
class TemporalValueParserTest {
    /**
     * 2025-12-31
     */
    static final LocalDate EXPECTED_LOCAL_DATE = LocalDate.of(2025, 12, 31);

    /**
     * 2025-12-31 at 23:59:59.0
     */
    static final LocalDateTime EXPECTED_LOCAL_DATE_TIME = LocalDateTime.of(2025, 12, 31, 23, 59, 59, 0);

    static <T extends Temporal> void test(TemporalValueParser<T> parser, String input, T expectedTemporal) {
        //Test with different suffixes:
        List<String> suffixes = List.of(
            "",
            ", foo, bar, baz",
            ", foo: bar, baz"
        );

        suffixes.forEach(suffix -> {
            String stringToParse = input + suffix;
            var expectedResult = new ParserResult<>(expectedTemporal, suffix);
            assertEquals(expectedResult, parser.parse(stringToParse).orElseThrow());
        });
    }

    @Nested
    @DisplayName("TemporalValueParser.ISO_LOCAL_DATE")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS) //Required for @MethodSource because Java 11 does not support static methods in inner classes.
    class IsoLocalDate {
        @ParameterizedTest
        @DisplayName("should parse ISO 8601 dates without offset")
        @MethodSource("testArgs")
        void test(String input, LocalDate expectedTemporal) {
            TemporalValueParserTest.test(TemporalValueParser.ISO_LOCAL_DATE, input, expectedTemporal);
        }

        Stream<Arguments> testArgs() {
            return Stream.of(
                arguments("2025-12-31", EXPECTED_LOCAL_DATE),
                arguments("+123456789-01-01", LocalDate.of(123456789, 1, 1)),
                arguments("-123456789-01-01", LocalDate.of(-123456789, 1, 1)),
                arguments("0001-01-01", LocalDate.of(1, 1, 1)),
                arguments("-0001-01-01", LocalDate.of(-1, 1, 1))
            );
        }
    }

    @Nested
    @DisplayName("TemporalValueParser.ISO_OFFSET_DATE")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class IsoOffsetDate {
        @ParameterizedTest
        @DisplayName("should parse ISO 8601 dates with offset")
        @MethodSource("testArgs")
        void test(String input, LocalDate expectedTemporal) {
            TemporalValueParserTest.test(TemporalValueParser.ISO_OFFSET_DATE, input, expectedTemporal);
        }

        Stream<Arguments> testArgs() {
            return Stream.of(
                arguments("2025-12-31Z", EXPECTED_LOCAL_DATE),
                arguments("2025-12-31+00:00", EXPECTED_LOCAL_DATE),
                arguments("2025-12-31-00:00", EXPECTED_LOCAL_DATE),
                arguments("2025-12-31+00:00:00", EXPECTED_LOCAL_DATE),
                arguments("2025-12-31-00:00:00", EXPECTED_LOCAL_DATE)
            );
        }

        @Test
        @DisplayName("should not match offsets without offset minutes")
        void offsetMinutes() { //The underlying DateTimeFormatter can not parse offsets without offset minutes.
            assertTrue(TemporalValueParser.ISO_OFFSET_DATE.parse("2025-12-31+00").isEmpty());
            assertTrue(TemporalValueParser.ISO_OFFSET_DATE.parse("2025-12-31-00").isEmpty());
        }
    }

    @Nested
    @DisplayName("TemporalValueParser.ISO_LOCAL_DATE_TIME")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class IsoLocalDateTime {
        @ParameterizedTest
        @DisplayName("should parse ISO 8601 date-times without offset")
        @MethodSource("testArgs")
        void test(String input, LocalDateTime expectedTemporal) {
            TemporalValueParserTest.test(TemporalValueParser.ISO_LOCAL_DATE_TIME, input, expectedTemporal);
        }

        Stream<Arguments> testArgs() {
            return Stream.of(
                arguments("2025-12-31T23:59", EXPECTED_LOCAL_DATE_TIME.withSecond(0)),
                arguments("2025-12-31T23:59:59", EXPECTED_LOCAL_DATE_TIME),
                arguments("2025-12-31T23:59:59.0", EXPECTED_LOCAL_DATE_TIME),
                arguments("2025-12-31T23:59:59.123456789", EXPECTED_LOCAL_DATE_TIME.withNano(123456789))
            );
        }
    }

    @Nested
    @DisplayName("TemporalValueParser.ISO_OFFSET_DATE_TIME")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class IsoOffsetDateTime {
        @ParameterizedTest
        @DisplayName("should parse ISO 8601 date-times with offset")
        @MethodSource("testArgs")
        void test(String input, Instant expectedTemporal) {
            TemporalValueParserTest.test(TemporalValueParser.ISO_OFFSET_DATE_TIME, input, expectedTemporal);
        }

        Instant expectedInstantWith(int second, int nano, ZoneId zoneId) {
            return createInstant(2025, 12, 31, 23, 59, second, nano, zoneId);
        }

        Stream<Arguments> testArgs() {
            var plusHours = ZoneOffset.ofHours(1);
            var minusHours = ZoneOffset.ofHours(-1);
            var plusMinutes = ZoneOffset.ofHoursMinutes(1, 2);
            var minusMinutes = ZoneOffset.ofHoursMinutes(-1, -2);
            var plusSeconds = ZoneOffset.ofHoursMinutesSeconds(1, 2, 3);
            var minusSeconds = ZoneOffset.ofHoursMinutesSeconds(-1, -2, -3);

            return Stream.of(
                //Zulu:
                arguments("2025-12-31T23:59Z", expectedInstantWith(0, 0, ZoneOffset.UTC)),
                arguments("2025-12-31T23:59:59Z", expectedInstantWith(59, 0, ZoneOffset.UTC)),
                arguments("2025-12-31T23:59:59.0Z", expectedInstantWith(59, 0, ZoneOffset.UTC)),
                arguments("2025-12-31T23:59:59.123456789Z", expectedInstantWith(59, 123456789, ZoneOffset.UTC)),
                //±01:
                arguments("2025-12-31T23:59+01", expectedInstantWith(0, 0, plusHours)),
                arguments("2025-12-31T23:59-01", expectedInstantWith(0, 0, minusHours)),
                arguments("2025-12-31T23:59:59+01", expectedInstantWith(59, 0, plusHours)),
                arguments("2025-12-31T23:59:59-01", expectedInstantWith(59, 0, minusHours)),
                arguments("2025-12-31T23:59:59.0+01", expectedInstantWith(59, 0, plusHours)),
                arguments("2025-12-31T23:59:59.0-01", expectedInstantWith(59, 0, minusHours)),
                arguments("2025-12-31T23:59:59.123456789+01", expectedInstantWith(59, 123456789, plusHours)),
                arguments("2025-12-31T23:59:59.123456789-01", expectedInstantWith(59, 123456789, minusHours)),
                //±01:02:
                arguments("2025-12-31T23:59+01:02", expectedInstantWith(0, 0, plusMinutes)),
                arguments("2025-12-31T23:59-01:02", expectedInstantWith(0, 0, minusMinutes)),
                arguments("2025-12-31T23:59:59+01:02", expectedInstantWith(59, 0, plusMinutes)),
                arguments("2025-12-31T23:59:59-01:02", expectedInstantWith(59, 0, minusMinutes)),
                arguments("2025-12-31T23:59:59.0+01:02", expectedInstantWith(59, 0, plusMinutes)),
                arguments("2025-12-31T23:59:59.0-01:02", expectedInstantWith(59, 0, minusMinutes)),
                arguments("2025-12-31T23:59:59.123456789+01:02", expectedInstantWith(59, 123456789, plusMinutes)),
                arguments("2025-12-31T23:59:59.123456789-01:02", expectedInstantWith(59, 123456789, minusMinutes)),
                //±01:02:03:
                arguments("2025-12-31T23:59+01:02:03", expectedInstantWith(0, 0, plusSeconds)),
                arguments("2025-12-31T23:59-01:02:03", expectedInstantWith(0, 0, minusSeconds)),
                arguments("2025-12-31T23:59:59+01:02:03", expectedInstantWith(59, 0, plusSeconds)),
                arguments("2025-12-31T23:59:59-01:02:03", expectedInstantWith(59, 0, minusSeconds)),
                arguments("2025-12-31T23:59:59.0+01:02:03", expectedInstantWith(59, 0, plusSeconds)),
                arguments("2025-12-31T23:59:59.0-01:02:03", expectedInstantWith(59, 0, minusSeconds)),
                arguments("2025-12-31T23:59:59.123456789+01:02:03", expectedInstantWith(59, 123456789, plusSeconds)),
                arguments("2025-12-31T23:59:59.123456789-01:02:03", expectedInstantWith(59, 123456789, minusSeconds))
            );
        }
    }

    @Nested
    @DisplayName("TemporalValueParser.ISO_ZONED_DATE_TIME")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class IsoZonedDateTime {
        @ParameterizedTest
        @DisplayName("should parse ISO 8601 date-times with offset and time zone")
        @MethodSource("testArgs")
        void test(String input, Instant expectedTemporal) {
            TemporalValueParserTest.test(TemporalValueParser.ISO_ZONED_DATE_TIME, input, expectedTemporal);
        }

        Stream<Arguments> testArgs() {
            return Stream.of(
                arguments("2025-12-31T23:59:59+01:00[Europe/Berlin]", createInstant(EXPECTED_LOCAL_DATE_TIME, ZoneId.of("Europe/Berlin"))),
                arguments("2025-12-31T23:59:59-03:00[America/Argentina/Buenos_Aires]", createInstant(EXPECTED_LOCAL_DATE_TIME, ZoneId.of("America/Argentina/Buenos_Aires"))),
                arguments("2025-12-31T23:59:59-10:00[Etc/GMT+10]", createInstant(EXPECTED_LOCAL_DATE_TIME, ZoneId.of("Etc/GMT+10"))),
                arguments("2025-12-31T23:59:59+03:00[Etc/GMT-3]", createInstant(EXPECTED_LOCAL_DATE_TIME, ZoneId.of("Etc/GMT-3"))),
                arguments("2025-12-31T23:59:59+00:00[Universal]", createInstant(EXPECTED_LOCAL_DATE_TIME, ZoneId.of("Universal")))
            );
        }
    }

    @Nested
    @DisplayName("TemporalValueParser.ISO_BASIC_LOCAL_DATE")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class IsoBasicLocalDate {
        @ParameterizedTest
        @DisplayName("should parse ISO 8601 basic local dates with optional offset")
        @MethodSource("testArgs")
        void test(String input, LocalDate expectedTemporal) {
            TemporalValueParserTest.test(TemporalValueParser.ISO_BASIC_LOCAL_DATE, input, expectedTemporal);
        }

        Stream<Arguments> testArgs() {
            return Stream.of(
                arguments("20251231", EXPECTED_LOCAL_DATE),
                arguments("20251231Z", EXPECTED_LOCAL_DATE),
                arguments("20251231+00", EXPECTED_LOCAL_DATE),
                arguments("20251231-00", EXPECTED_LOCAL_DATE),
                arguments("20251231+0000", EXPECTED_LOCAL_DATE),
                arguments("20251231-0000", EXPECTED_LOCAL_DATE),
                arguments("20251231+000000", EXPECTED_LOCAL_DATE),
                arguments("20251231-000000", EXPECTED_LOCAL_DATE)
            );
        }
    }

    @Nested
    @DisplayName("TemporalValueParser.ISO_ORDINAL_DATE")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class IsoOrdinalDate {
        @ParameterizedTest
        @DisplayName("should parse ISO 8601 ordinal dates with optional offset")
        @MethodSource("testArgs")
        void test(String input, LocalDate expectedTemporal) {
            TemporalValueParserTest.test(TemporalValueParser.ISO_ORDINAL_DATE, input, expectedTemporal);
        }

        Stream<Arguments> testArgs() {
            return Stream.of(
                arguments("2025-365", EXPECTED_LOCAL_DATE),
                arguments("+123456789-001", LocalDate.of(123456789, 1, 1)),
                arguments("-123456789-001", LocalDate.of(-123456789, 1, 1)),
                arguments("0001-001", LocalDate.of(1, 1, 1)),
                arguments("-0001-001", LocalDate.of(-1, 1, 1)),
                arguments("2025-365Z", EXPECTED_LOCAL_DATE),
                arguments("2025-365+00:00", EXPECTED_LOCAL_DATE),
                arguments("2025-365-00:00", EXPECTED_LOCAL_DATE),
                arguments("2025-365+00:00:00", EXPECTED_LOCAL_DATE),
                arguments("2025-365-00:00:00", EXPECTED_LOCAL_DATE)
            );
        }

        @Test
        @DisplayName("should partially parse offsets without offset minutes")
        void offsetMinutes() { //The underlying DateTimeFormatter can not parse offsets without offset minutes.
            assertEquals(new ParserResult<>(EXPECTED_LOCAL_DATE, "+00"), TemporalValueParser.ISO_ORDINAL_DATE.parse("2025-365+00").orElseThrow());
            assertEquals(new ParserResult<>(EXPECTED_LOCAL_DATE, "-00"), TemporalValueParser.ISO_ORDINAL_DATE.parse("2025-365-00").orElseThrow());
        }
    }

    @Nested
    @DisplayName("TemporalValueParser.ISO_WEEK_DATE")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class IsoWeekDate {
        @ParameterizedTest
        @DisplayName("should parse ISO 8601 week-based dates with optional offset")
        @MethodSource("testArgs")
        void test(String input, LocalDate expectedTemporal) {
            TemporalValueParserTest.test(TemporalValueParser.ISO_WEEK_DATE, input, expectedTemporal);
        }

        Stream<Arguments> testArgs() {
            return Stream.of(
                arguments("2026-W01-3", EXPECTED_LOCAL_DATE),
                arguments("+123456788-W52-7", LocalDate.of(123456789, 1, 1)),
                arguments("-123456790-W52-6", LocalDate.of(-123456789, 1, 1)),
                arguments("0001-W01-1", LocalDate.of(1, 1, 1)),
                arguments("-0002-W53-5", LocalDate.of(-1, 1, 1)),
                arguments("2026-W01-3Z", EXPECTED_LOCAL_DATE),
                arguments("2026-W01-3+00:00", EXPECTED_LOCAL_DATE),
                arguments("2026-W01-3-00:00", EXPECTED_LOCAL_DATE),
                arguments("2026-W01-3+00:00:00", EXPECTED_LOCAL_DATE),
                arguments("2026-W01-3-00:00:00", EXPECTED_LOCAL_DATE)
            );
        }

        @Test
        @DisplayName("should partially parse offsets without offset minutes")
        void offsetMinutes() { //The underlying DateTimeFormatter can not parse offsets without offset minutes.
            assertEquals(new ParserResult<>(EXPECTED_LOCAL_DATE, "+00"), TemporalValueParser.ISO_WEEK_DATE.parse("2026-W01-3+00").orElseThrow());
            assertEquals(new ParserResult<>(EXPECTED_LOCAL_DATE, "-00"), TemporalValueParser.ISO_WEEK_DATE.parse("2026-W01-3-00").orElseThrow());
        }
    }

    @Nested
    @DisplayName("TemporalValueParser.RFC_1123_DATE_TIME")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Rfc1123DateTime {
        @ParameterizedTest
        @DisplayName("should parse RFC 1123 date-times")
        @MethodSource("testArgs")
        void test(String input, Instant expectedTemporal) {
            TemporalValueParserTest.test(TemporalValueParser.RFC_1123_DATE_TIME, input, expectedTemporal);
        }

        Stream<Arguments> testArgs() {
            return Stream.of(
                arguments("Wed, 31 Dec 2025 23:59:59 GMT", createInstant(EXPECTED_LOCAL_DATE_TIME, ZoneOffset.UTC)),
                arguments("Wed, 31 Dec 2025 23:59:59 +11", createInstant(EXPECTED_LOCAL_DATE_TIME, ZoneOffset.ofHours(11))),
                arguments("Wed, 31 Dec 2025 23:59:59 -0630", createInstant(EXPECTED_LOCAL_DATE_TIME, ZoneOffset.ofHoursMinutes(-6, -30))),
                arguments("Thu, 1 Jan 2026 04:00 GMT", createInstant(2026, 1, 1, 4, 0, 0, 0, ZoneOffset.UTC)),
                arguments("1 Jan 2026 04:00 GMT", createInstant(2026, 1, 1, 4, 0, 0, 0, ZoneOffset.UTC))
            );
        }
    }

    @ParameterizedTest
    @DisplayName("should use case-insensitive regular expressions")
    @MethodSource("caseInsensitiveRegexArgs")
    void caseInsensitiveRegex(TemporalValueParser<?> parser) {
        assertTrue((parser.getRegex().flags() & Pattern.CASE_INSENSITIVE) == Pattern.CASE_INSENSITIVE);
    }

    static Stream<Arguments> caseInsensitiveRegexArgs() {
        return Stream.of(
            arguments(TemporalValueParser.ISO_LOCAL_DATE),
            arguments(TemporalValueParser.ISO_OFFSET_DATE),
            arguments(TemporalValueParser.ISO_LOCAL_DATE_TIME),
            arguments(TemporalValueParser.ISO_OFFSET_DATE_TIME),
            arguments(TemporalValueParser.ISO_ZONED_DATE_TIME),
            arguments(TemporalValueParser.ISO_BASIC_LOCAL_DATE),
            arguments(TemporalValueParser.ISO_ORDINAL_DATE),
            arguments(TemporalValueParser.ISO_WEEK_DATE),
            arguments(TemporalValueParser.RFC_1123_DATE_TIME)
        );
    }

    @ParameterizedTest
    @DisplayName("should use the same regular expressions for the same things")
    @MethodSource("regexPrefixArgs")
    void regexPrefix(TemporalValueParser<?> parserLongRegex, TemporalValueParser<?> parserShortRegex) {
        //The regular expression used by parserLongRegex should start with the regular expression used by parserShortRegex:
        assertTrue(parserLongRegex.getRegex().pattern().startsWith(parserShortRegex.getRegex().pattern()));
    }

    static Stream<Arguments> regexPrefixArgs() {
        return Stream.of(
            arguments(TemporalValueParser.ISO_OFFSET_DATE, TemporalValueParser.ISO_LOCAL_DATE),
            arguments(TemporalValueParser.ISO_LOCAL_DATE_TIME, TemporalValueParser.ISO_LOCAL_DATE),
            arguments(TemporalValueParser.ISO_OFFSET_DATE_TIME, TemporalValueParser.ISO_LOCAL_DATE_TIME),
            arguments(TemporalValueParser.ISO_ZONED_DATE_TIME, TemporalValueParser.ISO_OFFSET_DATE_TIME)
        );
    }
}
