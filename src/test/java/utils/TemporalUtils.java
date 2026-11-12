package utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Time-related utilities for unit tests.
 */
public final class TemporalUtils {
    public static Instant createInstant(LocalDateTime localDateTime, ZoneId zoneId) {
        return ZonedDateTime.of(localDateTime, zoneId).toInstant();
    }

    public static Instant createInstant(int year, int month, int day, int hour, int minute, int second, int nano, ZoneId zoneId) {
        return ZonedDateTime.of(year, month, day, hour, minute, second, nano, zoneId).toInstant();
    }
}
