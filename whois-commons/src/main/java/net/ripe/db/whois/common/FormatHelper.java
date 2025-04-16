package net.ripe.db.whois.common;

import org.apache.commons.lang3.StringUtils;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

public final class FormatHelper {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_UTC_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DAY_DATE_UTC_FORMAT = DateTimeFormatter.ofPattern("E MMM d HH:mm:ss yyyy'Z'").withZone(ZoneOffset.UTC);

    private static final String SPACES = StringUtils.repeat(" ", 100);

    private FormatHelper() {
        // do not instantiate
    }

    public static String dateToString(final TemporalAccessor temporalAccessor) {
        if (temporalAccessor == null) {
            return null;
        }

        return DATE_FORMAT.format(temporalAccessor);
    }


    public static String dateTimeToString(final TemporalAccessor temporalAccessor) {
        if (temporalAccessor == null) {
            return null;
        }

        if (temporalAccessor.isSupported(ChronoField.HOUR_OF_DAY)) {
            return DATE_TIME_FORMAT.format(temporalAccessor);
        }

        return DATE_FORMAT.format(temporalAccessor);
    }

    public static String dateTimeToUtcString(final TemporalAccessor temporalAccessor) {
        if (temporalAccessor == null) {
            return null;
        }

        return DATE_TIME_UTC_FORMAT.format(temporalAccessor);
    }

    public static String dayDateTimeToUtcString(final TemporalAccessor temporalAccessor) {
        if (temporalAccessor == null) {
            return null;
        }

        return DAY_DATE_UTC_FORMAT.format(temporalAccessor);
    }

    public static String prettyPrint(final String prefix, final String value, final int indentation, final int maxLineLength) {
        return prettyPrint(prefix, value, indentation, maxLineLength, false);
    }

    public static String prettyPrint(final String prefix, final String value, final int indentation, final int maxLineLength, boolean repeatPrefix) {
        final String firstPrefix = StringUtils.rightPad(prefix, indentation, ' ');
        final String newLinePrefix = "\n" + (repeatPrefix ? firstPrefix : SPACES.substring(0, indentation));

        final StringBuilder valueBuilder = new StringBuilder(value.length());
        valueBuilder.append(firstPrefix);

        int lastSpace = 0, spaces = 0, lineBreaks = 0, lastLineBreak = 0;
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);

            if (c == ' ') {
                spaces++;
                lastSpace = valueBuilder.length();
                continue;
            }

            if (c == '\n') {
                lineBreaks++;
                lastLineBreak = valueBuilder.length();
                continue;
            }

            while (lineBreaks > 0) {
                lineBreaks--;
                lastLineBreak = valueBuilder.length();
                valueBuilder.append(newLinePrefix);
                spaces = 0;
            }

            while (spaces > 0 && lastSpace - lastLineBreak < maxLineLength) {
                spaces--;
                lastSpace = valueBuilder.length();
                valueBuilder.append(' ');
            }

            if (valueBuilder.length() - lastLineBreak >= maxLineLength && lastSpace > lastLineBreak) {
                lastLineBreak = lastSpace;

                if (lastSpace < valueBuilder.length() && valueBuilder.charAt(lastSpace) == ' ') {
                    valueBuilder.replace(lastSpace, lastSpace + 1, newLinePrefix);
                } else {
                    valueBuilder.insert(lastSpace, newLinePrefix);
                }
                lastSpace = valueBuilder.length();
                spaces = 0;
            }


            valueBuilder.append(c);
        }

        if (valueBuilder.length() == 0 || valueBuilder.charAt(valueBuilder.length() - 1) != '\n') {
            valueBuilder.append('\n');
        }

        return valueBuilder.toString();
    }
}
