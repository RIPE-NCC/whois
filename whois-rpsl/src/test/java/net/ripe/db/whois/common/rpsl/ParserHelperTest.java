package net.ripe.db.whois.common.rpsl;

import org.junit.Test;

public class ParserHelperTest {

    // validateMoreSpecificsOperator

    @Test
    public void validateMoreSpecificsOperator() {
        ParserHelper.validateMoreSpecificsOperator("^0");
        ParserHelper.validateMoreSpecificsOperator("^32");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateMoreSpecificsOperatorFailsTooLarge() {
        ParserHelper.validateMoreSpecificsOperator("^33");
    }

    // validateRangeMoreSpecificsOperators

    @Test
    public void validateRangeMoreSpecificsOperators() {
        ParserHelper.validateRangeMoreSpecificsOperators("^0-0");
        ParserHelper.validateRangeMoreSpecificsOperators("^0-1");
        ParserHelper.validateRangeMoreSpecificsOperators("^0-32");
        ParserHelper.validateRangeMoreSpecificsOperators("^31-32");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateRangeMoreSpecificsOperatorsInverseRange() {
        ParserHelper.validateRangeMoreSpecificsOperators("^1-0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateRangeMoreSpecificsOperatorsOutOfRange() {
        ParserHelper.validateRangeMoreSpecificsOperators("^0-33");
    }

    // validateAsNumber

    @Test
    public void validateAsNumber() {
        ParserHelper.validateAsNumber("AS0");
        ParserHelper.validateAsNumber("AS" + ParserHelper.MAX_32BIT_NUMBER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateAsNumberOutOfRange() {
        ParserHelper.validateAsNumber("AS" + (ParserHelper.MAX_32BIT_NUMBER + 1L));
    }

    // validateIpv4PrefixRange

    @Test
    public void validateIpv4PrefixRange() {
        ParserHelper.validateIpv4PrefixRange("128.9.0.0/16^-");
        ParserHelper.validateIpv4PrefixRange("5.0.0.0/8^+");
        ParserHelper.validateIpv4PrefixRange("30.0.0.0/8^16");
        ParserHelper.validateIpv4PrefixRange("30.0.0.0/8^24-32");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv4PrefixRangeInvalidPrefix() {
        ParserHelper.validateIpv4PrefixRange("128.9.0.0/33^-");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv4PrefixRangeInvalidPrefixPlus() {
        ParserHelper.validateIpv4PrefixRange("128.9.0.0/33^+");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv4PrefixRangeInvalidPrefixCaretLength() {
        ParserHelper.validateIpv4PrefixRange("128.9.0.0/8^33");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv4PrefixRangeInvalidPrefixCaretLengthEndRange() {
        ParserHelper.validateIpv4PrefixRange("128.9.0.0/8^24-33");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv4PrefixRangeInvalidPrefixCaretLengthInverseRange() {
        ParserHelper.validateIpv4PrefixRange("128.9.0.0/8^32-24");
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

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv6PrefixRangeInvalidPrefix() {
        ParserHelper.validateIpv6PrefixRange("2001:0DB8::/129^-");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv6PrefixRangeInvalidPrefixPlus() {
        ParserHelper.validateIpv6PrefixRange("2001:0DB8::/129^+");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv6PrefixRangeInvalidPrefixCaretLength() {
        ParserHelper.validateIpv6PrefixRange("2001:0DB8::/32^129");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv6PrefixRangeInvalidPrefixCaretLengthEndRange() {
        ParserHelper.validateIpv6PrefixRange("2001:0DB8::/32^32-129");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv6PrefixRangeInvalidPrefixCaretLengthInverseRange() {
        ParserHelper.validateIpv6PrefixRange("2001:0DB8::/32^32-24");
    }

    // validateIpv4Prefix

    @Test
    public void validateIpv4Prefix() {
        ParserHelper.validateIpv4Prefix("0.0.0.0/0");
        ParserHelper.validateIpv4Prefix("1.2.3.4/32");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv4PrefixOutOfRange() {
        ParserHelper.validateIpv4Prefix("1.2.3.4/33");
    }

    // validateIpv6Prefix

    @Test
    public void validateIpv6Prefix() {
        ParserHelper.validateIpv6Prefix("2001:0DB8::/32");
        ParserHelper.validateIpv6Prefix("2001:06f0:0041:0000:0000:0000:3c00:0004/128");
        ParserHelper.validateIpv6Prefix("::/0");
        ParserHelper.validateIpv6Prefix("2001:503:231d::/48");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv6PrefixOutOfRange() {
        ParserHelper.validateIpv6Prefix("2001:0DB8::/129");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv6PrefixDoubleColonOutOfRange() {
        ParserHelper.validateIpv6Prefix("2001:503:231d::/129");
    }

    // validateIpv4

    @Test
    public void validateIpv4() {
        ParserHelper.validateIpv4("1.2.3.4/32");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv4InvalidPrefixLength() {
        ParserHelper.validateIpv4("1.2.3.4/33");
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

    @Test(expected = IllegalArgumentException.class)
    public void validateIpv6InvalidPrefixLength() {
        ParserHelper.validateIpv6("2001:0DB8::/129");
    }

    // validateCommunity

    @Test
    public void validateCommunity() {
        ParserHelper.validateCommunity("1:1");
        ParserHelper.validateCommunity("65535:65535");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateCommunityBeforeColonOutOfRange() {
        ParserHelper.validateCommunity("65536:1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateCommunityAfterColonOutOfRange() {
        ParserHelper.validateCommunity("1:65536");
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

    @Test(expected = IllegalArgumentException.class)
    public void validateDomainNameTooLong() {
        ParserHelper.validateDomainName("12345678901234567890123456789012345678901234567890" +
                                        "12345678901234567890123456789012345678901234567890" +
                                        "12345678901234567890123456789012345678901234567890" +
                                        "12345678901234567890123456789012345678901234567890" +
                                        "12345678901234567890123456789012345678901234567890123456");
    }

    // validateDomainNameLabel

    @Test
    public void validateDomainNameLabel() {
        ParserHelper.validateDomainNameLabel("abcdefghijklmnopqrstuvwxyz");
        ParserHelper.validateDomainNameLabel("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        ParserHelper.validateDomainNameLabel("123456789012345678901234567890123456789012345678901234567890123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateDomainNameLabelTooLong() {
        ParserHelper.validateDomainNameLabel("1234567890123456789012345678901234567890123456789012345678901234");
    }

    // validate AS Range

    @Test
    public void validateAsRange() {
        ParserHelper.validateAsRange("AS1 - AS2");
        ParserHelper.validateAsRange("AS1  -   AS2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateAsRangeInverseRange() {
        ParserHelper.validateAsRange("AS2 - AS1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateAsRangeInvalidToNoSpaces() {
        ParserHelper.validateAsRange("AS1-AS" + (1L << 32));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateAsRangeInvalidToWithSpaces() {
        ParserHelper.validateAsRange("AS1 -  AS" + (1L << 32));
    }
}
