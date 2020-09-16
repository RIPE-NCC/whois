package net.ripe.db.whois.common;

import org.apache.commons.mail.util.IDNEmailAddressConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PunycodeConversion {

    private static final Logger LOGGER = LoggerFactory.getLogger(PunycodeConversion.class);

    private static final IDNEmailAddressConverter CONVERTER = new IDNEmailAddressConverter();

    private static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("(?m)^(abuse-mailbox|e-mail|irt-nfy|mnt-nfy|notify|ref-nfy|upd-to)(?:\\:)(\\s+)(.*)(\\n|$)");

    private PunycodeConversion() {
        // do not instantiate
    }

    public static String convert(final String value) {
        final Matcher matcher = EMAIL_ADDRESS_PATTERN.matcher(value);
        while (matcher.find()) {
            final String key = matcher.group(1);
            final String space = matcher.group(2);
            final String address = matcher.group(3);
            final String newline = matcher.group(4);

            final String convertedAddress = toAscii(address);
            if (!convertedAddress.equals(address)) {
                final String originalMatch = matcher.group(0);
                final String convertedMatch = String.format("%s:%s%s%s", key, space, convertedAddress, newline);
                return convert(value.replace(originalMatch, convertedMatch));
            }
        }

        return value;
    }

    private static String toAscii(final String address) {
        try {
            return CONVERTER.toASCII(address);
        } catch (Exception e) {
            LOGGER.warn("Unable to convert {} to Punycode due to {}: {}", address, e.getClass().getName(), e.getMessage());
            return address;
        }
    }

}
