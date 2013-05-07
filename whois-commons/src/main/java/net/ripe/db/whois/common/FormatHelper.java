package net.ripe.db.whois.common;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeFieldType;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public final class FormatHelper {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private static final String SPACES = StringUtils.repeat(" ", 100);

    private FormatHelper() {
    }

    public static String dateToString(final ReadablePartial readablePartial) {
        if (readablePartial == null) {
            return null;
        }

        return DATE_FORMAT.print(readablePartial);
    }

    public static String dateTimeToString(final ReadablePartial readablePartial) {
        if (readablePartial == null) {
            return null;
        }

        if (readablePartial.isSupported(DateTimeFieldType.hourOfDay())) {
            return DATE_TIME_FORMAT.print(readablePartial);
        }

        return DATE_FORMAT.print(readablePartial);
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
