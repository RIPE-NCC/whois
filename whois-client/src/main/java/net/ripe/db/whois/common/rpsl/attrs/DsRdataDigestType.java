package net.ripe.db.whois.common.rpsl.attrs;

import java.util.regex.Pattern;

public enum DsRdataDigestType {
    SHA1(1, "^[0-9a-fA-F]{40}$"),
    SHA256(2, "^[0-9a-fA-F]{64}$"),
    GOSTR341194(3, "^[0-9a-fA-F]{64}$"),
    SHA384(4, "^[0-9a-fA-F]{96}$");

    private final int digestType;
    private final String validationPattern;

    DsRdataDigestType(final int digestType, final String validationPattern) {
        this.digestType = digestType;
        this.validationPattern = validationPattern;
    }

    public static boolean validateLength(final int digestType, final String digest) {
        switch(digestType) {
            case 1: // SHA-1
                return Pattern.matches(SHA1.validationPattern, digest);
            case 2: // SHA-256
                return Pattern.matches(SHA256.validationPattern, digest);
            case 3: // GOST R 34.11-94
                return Pattern.matches(GOSTR341194.validationPattern, digest);
            case 4: // SHA-384
                return Pattern.matches(SHA384.validationPattern, digest);
            default:
                throw new IllegalArgumentException("Unknown digest type");
        }
    }
}