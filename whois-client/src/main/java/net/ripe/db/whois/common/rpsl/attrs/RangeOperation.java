package net.ripe.db.whois.common.rpsl.attrs;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RangeOperation {
    private static final Pattern RANGE_OPERATION_PATTERN = Pattern.compile("^\\^(?:[+-]|(\\d+)(?:\\-(\\d+))?)$");

    private final Integer n;
    private final Integer m;

    public static RangeOperation parse(final String value, final int prefixLength, final int maxRange) {
        if (StringUtils.isEmpty(value)) {
            return new RangeOperation(prefixLength, prefixLength);
        }

        final Matcher matcher = RANGE_OPERATION_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new AttributeParseException("Invalid range operation", value);
        }

        if (value.startsWith("^-")) {
            return new RangeOperation(prefixLength + 1, maxRange);
        }

        if (value.startsWith("^+")) {
            return new RangeOperation(prefixLength, maxRange);
        }

        final Integer n = Integer.parseInt(matcher.group(1));
        if (n < prefixLength) {
            throw new AttributeParseException("n cannot be smaller than prefix length", value);
        }

        if (n > maxRange) {
            throw new AttributeParseException("n cannot be larger than max range" + n, value);
        }

        final Integer m = matcher.group(2) == null ? null : Integer.parseInt(matcher.group(2));
        if (m == null) {
            return new RangeOperation(n, n);
        }

        if (m > maxRange) {
            throw new AttributeParseException("Invalid m: " + m, value);
        }

        if (n > m) {
            throw new AttributeParseException("Too large n: " + n, value);
        }

        return new RangeOperation(n, m);
    }

    private RangeOperation(final Integer n, final Integer m) {
        this.n = n;
        this.m = m;
    }

    public Integer getN() {
        return n;
    }

    public Integer getM() {
        return m;
    }
}
