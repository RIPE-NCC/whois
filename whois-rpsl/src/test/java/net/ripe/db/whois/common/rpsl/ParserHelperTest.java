package net.ripe.db.whois.common.rpsl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParserHelperTest {

    // validateMoreSpecificsOperator

    @Test
    public void validateMoreSpecificsOperator() {
        ParserHelper.validateMoreSpecificsOperator("^0");
        ParserHelper.validateMoreSpecificsOperator("^32");
    }

    @Test
    public void validateMoreSpecificsOperatorFailsTooLarge() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateMoreSpecificsOperator("^33");
        });
    }

    // validateRangeMoreSpecificsOperators

    @Test
    public void validateRangeMoreSpecificsOperators() {
        ParserHelper.validateRangeMoreSpecificsOperators("^0-0");
        ParserHelper.validateRangeMoreSpecificsOperators("^0-1");
        ParserHelper.validateRangeMoreSpecificsOperators("^0-32");
        ParserHelper.validateRangeMoreSpecificsOperators("^31-32");
    }

    @Test
    public void validateRangeMoreSpecificsOperatorsInverseRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateRangeMoreSpecificsOperators("^1-0");
        });
    }

    @Test
    public void validateRangeMoreSpecificsOperatorsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateRangeMoreSpecificsOperators("^0-33");
        });
    }

    // validateAsNumber

    @Test
    public void validateAsNumber() {
        ParserHelper.validateAsNumber("AS0");
        ParserHelper.validateAsNumber("AS" + ParserHelper.MAX_32BIT_NUMBER);
    }

    @Test
    public void validateAsNumberOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateAsNumber("AS" + (ParserHelper.MAX_32BIT_NUMBER + 1L));
        });
    }

    // validateIpv4PrefixRange

    @Test
    public void validateIpv4PrefixRange() {
        ParserHelper.validateIpv4PrefixRange("128.9.0.0/16^-");
        ParserHelper.validateIpv4PrefixRange("5.0.0.0/8^+");
        ParserHelper.validateIpv4PrefixRange("30.0.0.0/8^16");
        ParserHelper.validateIpv4PrefixRange("30.0.0.0/8^24-32");
    }

    @Test
    public void validateIpv4PrefixRangeInvalidPrefix() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv4PrefixRange("128.9.0.0/33^-");
        });
    }

    @Test
    public void validateIpv4PrefixRangeInvalidPrefixPlus() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv4PrefixRange("128.9.0.0/33^+");
        });
    }

    @Test
    public void validateIpv4PrefixRangeInvalidPrefixCaretLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv4PrefixRange("128.9.0.0/8^33");
        });
    }

    @Test
    public void validateIpv4PrefixRangeInvalidPrefixCaretLengthEndRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv4PrefixRange("128.9.0.0/8^24-33");
        });
    }

    @Test
    public void validateIpv4PrefixRangeInvalidPrefixCaretLengthInverseRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv4PrefixRange("128.9.0.0/8^32-24");
        });
    }

    // validateIpv6PrefixRange

    @Test
    public void validateIpv6PrefixRange() {
        ParserHelper.validateIpv6PrefixRange("2001:0DB8::/32^+");
        ParserHelper.validateIpv6PrefixRange("2001:0DB8:0100::/48^+");
        ParserHelper.validateIpv6PrefixRange("2001:0DB8:0200::/48^64");
        ParserHelper.validateIpv6PrefixRange("2001:0DB8:0200::/48^48-64");
        ParserHelper.validateIpv6PrefixRange("0::/0^0-128");
        ParserHelper.validateIpv6PrefixRange("::/0^0-48");
        ParserHelper.validateIpv6PrefixRange("::/0^0-64");
        ParserHelper.validateIpv6PrefixRange("::/0^0");
    }

    @Test
    public void validateIpv6PrefixRangeInvalidPrefix() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv6PrefixRange("2001:0DB8::/129^-");
        });
    }

    @Test
    public void validateIpv6PrefixRangeInvalidPrefixPlus() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv6PrefixRange("2001:0DB8::/129^+");
        });
    }

    @Test
    public void validateIpv6PrefixRangeInvalidPrefixCaretLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv6PrefixRange("2001:0DB8::/32^129");
        });
    }

    @Test
    public void validateIpv6PrefixRangeInvalidPrefixCaretLengthEndRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv6PrefixRange("2001:0DB8::/32^32-129");
        });
    }

    @Test
    public void validateIpv6PrefixRangeInvalidPrefixCaretLengthInverseRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv6PrefixRange("2001:0DB8::/32^32-24");
        });
    }

    // validateIpv4Prefix

    @Test
    public void validateIpv4Prefix() {
        ParserHelper.validateIpv4Prefix("0.0.0.0/0");
        ParserHelper.validateIpv4Prefix("1.2.3.4/32");
    }

    @Test
    public void validateIpv4PrefixOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv4Prefix("1.2.3.4/33");
        });
    }

    // validateIpv6Prefix

    @Test
    public void validateIpv6Prefix() {
        ParserHelper.validateIpv6Prefix("2001:0DB8::/32");
        ParserHelper.validateIpv6Prefix("2001:06f0:0041:0000:0000:0000:3c00:0004/128");
        ParserHelper.validateIpv6Prefix("::/0");
        ParserHelper.validateIpv6Prefix("2001:503:231d::/48");
    }

    @Test
    public void validateIpv6PrefixOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv6Prefix("2001:0DB8::/129");
        });
    }

    @Test
    public void validateIpv6PrefixDoubleColonOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv6Prefix("2001:503:231d::/129");
        });
    }

    // validateIpv4

    @Test
    public void validateIpv4() {
        ParserHelper.validateIpv4("1.2.3.4/32");
    }

    @Test
    public void validateIpv4InvalidPrefixLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv4("1.2.3.4/33");
        });
    }

    // validateIpv6

    @Test
    public void validateIpv6() {
        ParserHelper.validateIpv6("2001:0DB8::/32");
        ParserHelper.validateIpv6("2001:06f0:0041:0000:0000:0000:3c00:0004/128");
        ParserHelper.validateIpv6("2001:470:1:35e::1");
        ParserHelper.validateIpv6("2001:7F8:14::58:2");
        ParserHelper.validateIpv6("2001:7f8:13::a500:8587:1");
        ParserHelper.validateIpv6("2001:504:1::");
    }

    @Test
    public void validateIpv6InvalidPrefixLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateIpv6("2001:0DB8::/129");
        });
    }

    // validateCommunity

    @Test
    public void validateCommunity() {
        ParserHelper.validateCommunity("1:1");
        ParserHelper.validateCommunity("65535:65535");
    }

    @Test
    public void validateCommunityBeforeColonOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateCommunity("65536:1");
        });
    }

    @Test
    public void validateCommunityAfterColonOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateCommunity("1:65536");
        });
    }

    // validateDomainName

    @Test
    public void validateDomainName() {
        ParserHelper.validateDomainName("12345678901234567890123456789012345678901234567890" +
                                        "12345678901234567890123456789012345678901234567890" +
                                        "12345678901234567890123456789012345678901234567890" +
                                        "12345678901234567890123456789012345678901234567890" +
                                        "1234567890123456789012345678901234567890123456789012345");
    }

    @Test
    public void validateDomainNameTooLong() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateDomainName("12345678901234567890123456789012345678901234567890" +
                    "12345678901234567890123456789012345678901234567890" +
                    "12345678901234567890123456789012345678901234567890" +
                    "12345678901234567890123456789012345678901234567890" +
                    "12345678901234567890123456789012345678901234567890123456");
        });
    }

    // validateDomainNameLabel

    @Test
    public void validateDomainNameLabel() {
        ParserHelper.validateDomainNameLabel("abcdefghijklmnopqrstuvwxyz");
        ParserHelper.validateDomainNameLabel("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        ParserHelper.validateDomainNameLabel("123456789012345678901234567890123456789012345678901234567890123");
    }

    @Test
    public void validateDomainNameLabelTooLong() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateDomainNameLabel("1234567890123456789012345678901234567890123456789012345678901234");
        });
    }

    // validate AS Range

    @Test
    public void validateAsRange() {
        ParserHelper.validateAsRange("AS1 - AS2");
        ParserHelper.validateAsRange("AS1  -   AS2");
    }

    @Test
    public void validateAsRangeInverseRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateAsRange("AS2 - AS1");
        });
    }

    @Test
    public void validateAsRangeInvalidToNoSpaces() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateAsRange("AS1-AS" + (1L << 32));
        });

    }

    @Test
    public void validateAsRangeInvalidToWithSpaces() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParserHelper.validateAsRange("AS1 -  AS" + (1L << 32));
        });
    }
}
