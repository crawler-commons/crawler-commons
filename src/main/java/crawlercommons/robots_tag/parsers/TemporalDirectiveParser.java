package crawlercommons.robots_tag.parsers;

import crawlercommons.robots_tag.*;

import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Optional;

/**
 * Parses directives with {@link Temporal} values (e.g.
 * {@code unavailable_after: Wed, 31 Dec 2025 23:59:59 GMT}).
 */
public final class TemporalDirectiveParser implements DirectiveParser<Temporal> {
    private static final TemporalDirectiveParser SINGLETON = new TemporalDirectiveParser();

    private static final List<TemporalValueParser<? extends Temporal>> TEMPORAL_VALUE_PARSERS = List.of(
    // The order of the elements in this collection is important.
                    TemporalValueParser.ISO_ZONED_DATE_TIME, // 2025-12-31T23:59:59+01:00:00[Europe/Berlin]
                    TemporalValueParser.ISO_OFFSET_DATE_TIME, // 2025-12-31T23:59:59+01:00:00
                    TemporalValueParser.ISO_LOCAL_DATE_TIME, // 2025-12-31T23:59:59
                    TemporalValueParser.ISO_OFFSET_DATE, // 2025-12-31+01:00:00
                    TemporalValueParser.ISO_LOCAL_DATE, // 2025-12-31
                    TemporalValueParser.ISO_BASIC_LOCAL_DATE, // 20251231
                    TemporalValueParser.ISO_ORDINAL_DATE, // 2025-365
                    TemporalValueParser.ISO_WEEK_DATE, // 2026-W01-3
                    TemporalValueParser.RFC_1123_DATE_TIME // Wed, 31 Dec 2025
                                                           // 23:59:59 GMT
                    );

    @Override
    public ParserResult<Directive<Temporal>> parse(PreprocessedString input) {
        if (input.getTail().isEmpty()) {
            throw new ParserException("Failed to parse key-value directive due to missing value");
        }

        String tail = input.getTail().get();
        Optional<? extends ParserResult<? extends Temporal>> resultOption = Optional.empty();
        DateTimeParseException exception = null;

        for (TemporalValueParser<? extends Temporal> parser : TEMPORAL_VALUE_PARSERS) {
            try {
                resultOption = parser.parse(tail);

                if (resultOption.isPresent()) {
                    break;
                }
            } catch (DateTimeParseException e) { //Store the exception and continue iterating; one of the other TemporalValueParsers might still produce a usable result.
                exception = e;
            }
        }

        if (resultOption.isPresent()) {
            ParserResult<Temporal> result = (ParserResult<Temporal>) resultOption.get();
            var directive = new Directive<>(input.getFirstToken(), result.getValue());
            return new ParserResult<>(directive, result.getRemainder());
        } else {
            if (exception == null) {
                throw new ParserException("Failed to find a suitable TemporalValueParser for \"" + tail + '"');
            } else {
                throw new ParserException(exception.toString(), exception);
            }
        }
    }

    public static TemporalDirectiveParser getSingleton() {
        return SINGLETON;
    }
}
