package net.ripe.db.whois.common.rpsl;

public final class RpslAttributeFilter {
    private RpslAttributeFilter() {
    }

    /**
     * Converts all continuation lines to use '+' (required for shorthand)
     */
    public static String getValueForShortHand(String value) {
        final StringBuilder result = new StringBuilder(value.length());

        boolean foundNonSpace = false;
        boolean needContinuation = false;
        boolean seenContinuationChar = false;

        for (final char c : value.toCharArray()) {
            if (!foundNonSpace && (c == ' ' || c == '\t')) {
                continue;
            }

            if (c == '\n') {
                needContinuation = true;
                continue;
            }

            if (needContinuation && (c == ' ' || c == '+' || c == '\t')) {
                // if (seenContinuationChar) throw new IllegalStateBBQ
                seenContinuationChar = true;
                continue;
            }

            if (seenContinuationChar && (c == ' ' || c == '\t')) {
                continue;
            }

            if (needContinuation && seenContinuationChar) {
                result.append("\n+ ");
                needContinuation = false;
                seenContinuationChar = false;
            }

            if (!foundNonSpace) {
                result.append(' ');
            }
            result.append(c);

            foundNonSpace = true;
        }

        if (result.length() == 0) {
            return " ";
        }

        return result.toString();
    }
}
