package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.domain.ip.Ipv4Resource;
import net.ripe.db.whois.common.domain.ip.Ipv6Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserHelper {

    private ParserHelper() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(ParserHelper.class);
    static final long MAX_32BIT_NUMBER = (1L << 32) - 1;
    static final long MAX_16BIT_NUMBER = (1L << 16) - 1;

    private static final int DOMAIN_NAME_MAX_LENGTH = 255;
    private static final int DOMAIN_NAME_LABEL_MAX_LENGTH = 63;

    private static final int MAX_BIT_LENGTH_IPV4 = 32;
    private static final int MAX_BIT_LENGTH_IPV6 = 128;

    private static final Pattern ADDRESS_PREFIX_RANGE_PATTERN = Pattern.compile("^(.*)/(\\d+)[\\^][\\+]??(\\d+)*[-]??(\\d+)*$");
    private static final Pattern AS_RANGE_PATTERN = Pattern.compile("(?i)AS([0-9]+)[ ]*[-][ ]*AS([0-9]+)");

    public static void check16bit(final String number) {
        final long value = Long.parseLong(number);
        if (value < 0 || value > MAX_16BIT_NUMBER) {
            syntaxError("dpa value " + value +" is not between 0 and " + MAX_16BIT_NUMBER);
        }
    }

    public static void check32bit(final String number) {
        final long value = Long.parseLong(number);
        if (value < 0 || value > MAX_32BIT_NUMBER) {
            syntaxError("dpa value " + value +" is not between 0 and " + MAX_32BIT_NUMBER);
        }
    }

    public static void checkMaskLength(final String maskLength) {
        if (Integer.parseInt(maskLength) > MAX_BIT_LENGTH_IPV4) {
            syntaxError("masklen " + maskLength + " is not between 0 and 32");
        }
    }

    public static void checkMaskLengthv6(final String maskLength) {
        if (Integer.parseInt(maskLength) > MAX_BIT_LENGTH_IPV6) {
            syntaxError("masklen " + maskLength + " is not between 0 and 128");
        }
    }

    public static void checkStringLength(final String ip, final int maxAllowedLength) {
        int length = ip.length();
        if (length > maxAllowedLength) {
            syntaxError("Domain name " + length + " is longer than " + maxAllowedLength + " characters");
        }
    }

    public static void validateMoreSpecificsOperator(final String yytext) {
        int val = Integer.valueOf(yytext.substring(1));
        if (val > MAX_BIT_LENGTH_IPV4) {
            syntaxError("more specifics operator " + yytext.substring(1) + " not 0 to 32 bits");
        }
    }

    public static void validateRangeMoreSpecificsOperators(final String yytext) {
        final int dash = yytext.indexOf('-');

        int from = Integer.valueOf(yytext.substring(1, dash));
        if (from > MAX_BIT_LENGTH_IPV4) {
            syntaxError("more specifics operator " + yytext + " not 0 to 32 bits");
        }

        int to = Integer.valueOf(yytext.substring(dash + 1));
        if (to > MAX_BIT_LENGTH_IPV4) {
            syntaxError("more specifics operator " + yytext + " not 0 to 32 bits");
        }

        if (to < from) {
            syntaxError("more specifics operator " + yytext + " not 0 to 32 bits");
        }
    }

    public static void validateAsNumber(final String yytext) {
        final long value = Long.valueOf(yytext.substring(2));
        if (value > MAX_32BIT_NUMBER) {
            syntaxError("AS Number " + yytext + " length is invalid");
        }
    }

    /** check each number of 1.2.3.4/5 in prefix is valid, as
     *   well as any bit ranges specified
     * @param yytext
     */
    public static void validateIpv4PrefixRange(final String yytext) {
        Matcher m = ADDRESS_PREFIX_RANGE_PATTERN.matcher(yytext);
        if (m.matches()) {
            try {
                Ipv4Resource.parse(m.group(1));
            } catch (IllegalArgumentException e) {
                syntaxError("IP prefix " + yytext + " contains an invalid octet");
            }

            int prefix = Integer.valueOf(m.group(2));
            if (prefix > MAX_BIT_LENGTH_IPV4) {
                syntaxError("IP prefix range " + yytext + " contains an invalid prefix length");
            }

            String fromString = m.group(3);
            int from = 0;
            if (fromString != null) {
                from = Integer.valueOf(fromString);
                if (from > MAX_BIT_LENGTH_IPV4) {
                    syntaxError("IP prefix range " + yytext + " contains an invalid range");
                }
            }

            String toString = m.group(4);
            if (toString != null) {
                int to = Integer.valueOf(toString);
                if (to > MAX_BIT_LENGTH_IPV4) {
                    syntaxError("IP prefix range " + yytext + " contains an invalid range");
                }

                if (to < from) {
                    syntaxError("IP prefix " + yytext + " range end is less than range start");
                }
            }
        }
    }

    /**
     * check IPv6 address and prefix range is valid
     * @param yytext
     */
    public static void validateIpv6PrefixRange(final String yytext) {
        Matcher m = ADDRESS_PREFIX_RANGE_PATTERN.matcher(yytext);
        if (m.matches()) {
            try {
                Ipv6Resource.parse(m.group(1));
            } catch (IllegalArgumentException e) {
                syntaxError("IP prefix " + yytext + " contains an invalid quad");
            }

            int prefix = Integer.valueOf(m.group(2));
            if (prefix > MAX_BIT_LENGTH_IPV6) {
                syntaxError("IP prefix range " + yytext + " contains an invalid prefix length");
            }

            String fromString = m.group(3);
            int from = 0;
            if (fromString != null) {
                from = Integer.valueOf(fromString);
                if (from > MAX_BIT_LENGTH_IPV6) {
                    syntaxError("IP prefix range " + yytext + " contains an invalid range");
                }
            }

            String toString = m.group(4);
            if (toString != null) {
                int to = Integer.valueOf(toString);
                if (to > MAX_BIT_LENGTH_IPV6) {
                    syntaxError("IP prefix range " + yytext + " contains an invalid range");
                }

                if (to < from) {
                    syntaxError("IP prefix " + yytext + " range end is less than range start");
                }
            }
        }
    }

    /**
     * check each number of 1.2.3.4/5 in prefix is valid
     * @param yytext
     */
    public static void validateIpv4Prefix(final String yytext) {
        final int slash = yytext.indexOf('/');
        try {
            Ipv4Resource.parse(yytext.substring(0, slash));
        } catch (IllegalArgumentException e) {
            syntaxError("IP prefix " + yytext + " contains an invalid octet");
        }

        final int length = Integer.valueOf(yytext.substring(slash + 1));
        if (length > MAX_BIT_LENGTH_IPV4) {
            syntaxError("IP prefix " + yytext + " contains an invalid prefix length");
        }
    }

    /**
     * check each quad of 1A:3:7:8:AAAA:BBBB:DEAD:BEEF/55 in prefix is valid
     * @param yytext
     */
    public static void validateIpv6Prefix(final String yytext) {
        final int slash = yytext.indexOf('/');
        try {
            Ipv6Resource.parse(yytext.substring(0, slash));
        } catch (IllegalArgumentException e) {
            syntaxError("IPv6 prefix " + yytext + " contains an invalid quad");
        }

        final int length = Integer.valueOf(yytext.substring(slash + 1));
        if (length > MAX_BIT_LENGTH_IPV6) {
            syntaxError("IPv6 prefix " + yytext + " contains an invalid prefix length");
        }
    }

    public static void validateIpv4(final String yytext) {
        try {
            Ipv4Resource.parse(yytext);
        } catch (IllegalArgumentException e) {
            syntaxError("IP address " + yytext + " contains an invalid octet");
        }
    }

    /**
     * Validate an IPv6 address
     * @param yytext
     */
    public static void validateIpv6(final String yytext) {
        try {
            Ipv6Resource.parse(yytext);
        } catch (IllegalArgumentException e) {
            syntaxError("IP address " + yytext + " contains an invalid quad");
        }
    }

    /**
     * Verify a community number.
     * The definition is (2-octet):(2-octet), only 16-bit AS numbers are allowed.
     * @param yytext
     */
    public static void validateCommunity(final String yytext) {
        final int colon = yytext.indexOf(':');

        final long from = Long.valueOf(yytext.substring(0, colon));
        if (from > MAX_16BIT_NUMBER) {
            syntaxError("Community number " + yytext + " contains an invalid number");
        }

        final long to = Long.valueOf(yytext.substring(colon + 1));
        if (to > MAX_16BIT_NUMBER) {
            // TODO: allow invalid > 16 bit user-defined number for now, but log warning
            syntaxError("Community number " + yytext + " contains an invalid number");
        }
    }

    /**
     * Validate the total length of a domain name (ref. RFC833).
     * @param yytext
     */
    public static void validateDomainName(final String yytext) {
        if (yytext.length() > DOMAIN_NAME_MAX_LENGTH) {
            syntaxError("Domain name " + yytext + " is longer than 255 characters");
        }
    }

    /**
     * Validate the length of a portion of a domain name (aka 'label' in RFC833).
     * @param yytext
     */
    public static void validateDomainNameLabel(final String yytext) {
        if (yytext.length() > DOMAIN_NAME_LABEL_MAX_LENGTH) {
            syntaxError("Domain name label " + yytext + " is longer than 63 characters");
        }
    }

    public static void validateSmallInt(final String yytext) {
        final int value = Integer.valueOf(yytext);
        if (value < 0 || value > MAX_16BIT_NUMBER) {
            syntaxError("Numeric value " + yytext + " must be between 0 and 65535");
        }
    }

    public static void validateAsRange(final String yytext) {
        Matcher matcher = AS_RANGE_PATTERN.matcher(yytext);
        if (matcher.find()) {
            long begin = Long.valueOf(matcher.group(1));
            if (begin > MAX_32BIT_NUMBER) {
                syntaxError("AS Number " + yytext + " is invalid");
            }

            long end = Long.valueOf(matcher.group(2));
            if (end > MAX_32BIT_NUMBER) {
                syntaxError("AS Number " + yytext + " is invalid");
            }

            if (end < begin) {
                syntaxError("AS Range " + yytext + " is invalid");
            }
        }
    }

    public static void log(final String message) {
        LOGGER.debug(message);
    }

    public static void syntaxError(final String message) {
    	LOGGER.debug("syntax error: {}", message);
        throw new IllegalArgumentException(message);
    }

    public static void parserError(final String message) {
    	LOGGER.debug("parser error: {}", message);

        if (message.equalsIgnoreCase("syntax error")) {
            throw new IllegalArgumentException("invalid syntax");
        }
        throw new IllegalArgumentException(message);
    }
}
