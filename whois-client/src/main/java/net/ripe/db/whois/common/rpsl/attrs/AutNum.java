package net.ripe.db.whois.common.rpsl.attrs;

import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.concurrent.Immutable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Immutable
public class AutNum {
    private static final Pattern AUTNUM_PATTERN = Pattern.compile("(?i)^AS([0-9]+)$");

    final private Long value;

    public AutNum(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    public static AutNum parse(final CIString value) {
        return parse(value.toString());
    }

    public static AutNum parse(String value) {
        final Matcher matcher = AUTNUM_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new AttributeParseException("Invalid syntax", value);
        }

        final String num = matcher.group(1);
        long numericValue = -1;

        try {
            numericValue = Long.parseLong(num);
        } catch (NumberFormatException e) {}

        if (numericValue < 0 || numericValue > 4294967295L) {
            throw new AttributeParseException("AS number has to be between 0 and 4294967295", value);
        }

        if (numericValue != 0 && num.charAt(0) == '0') {
            throw new AttributeParseException("Cannot start with 0", value);
        }

        return new AutNum(numericValue);
    }
}
