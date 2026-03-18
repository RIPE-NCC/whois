package net.ripe.db.whois.common;

import org.apache.commons.mail2.jakarta.util.IDNEmailAddressConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PunycodeConversion {

    private static final Logger LOGGER = LoggerFactory.getLogger(PunycodeConversion.class);

    private static final IDNEmailAddressConverter CONVERTER = new IDNEmailAddressConverter();

    private static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("(?m)^(abuse-mailbox|e-mail|irt-nfy|mnt-nfy|notify|ref-nfy|upd-to)(?:\\:)([^#\\n]*)(.*)(\\n|$)");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("^(\\s*)(.*?)(\\s*)$");

    private PunycodeConversion() {
        // do not instantiate
    }

    public static String convert(final String value) {
        final Matcher emailMatcher = EMAIL_ADDRESS_PATTERN.matcher(value);
        while (emailMatcher.find()) {
            final String attrKey = emailMatcher.group(1);
            final String attrValue = emailMatcher.group(2);
            final String attrComment = emailMatcher.group(3);
            final String newline = emailMatcher.group(4);

            final Matcher whitespaceMatcher = WHITESPACE_PATTERN.matcher(attrValue);
            if (whitespaceMatcher.find()) {
                final String leadingSpaces = whitespaceMatcher.group(1);
                final String address = whitespaceMatcher.group(2);
                final String trailingSpaces = whitespaceMatcher.group(3);

                final String convertedAddress = toAscii(address);
                if (!convertedAddress.equals(address)) {
                    final String originalMatch = emailMatcher.group(0);
                    final String convertedMatch = String.format("%s:%s%s%s%s%s", attrKey, leadingSpaces, convertedAddress, trailingSpaces, attrComment, newline);
                    return convert(value.replace(originalMatch, convertedMatch));
                }
            }
        }

        return value;
    }

    public static String toAscii(final String address) {
        try {
            return CONVERTER.toASCII(address);
        } catch (Exception e) {
            LOGGER.warn("Unable to convert {} to Punycode due to {}: {}", address, e.getClass().getName(), e.getMessage());
            return address;
        }
    }

}
