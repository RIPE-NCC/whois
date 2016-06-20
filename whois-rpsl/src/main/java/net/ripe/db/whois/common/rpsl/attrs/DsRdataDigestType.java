package net.ripe.db.whois.common.rpsl.attrs;

public enum DsRdataDigestType {
    SHA1(1, 40),
    SHA256(2, 64),
    GOSTR341194(3, 64),
    SHA384(4, 96);

    private final int digestType;
    private final int digestLength;

    DsRdataDigestType(final int digestType, final int digestLength) {
        this.digestType = digestType;
        this.digestLength = digestLength;
    }

    public static boolean validateLengthForKnownTypes(final int digestType, final String digest) {
        switch(digestType) {
            case 1: // SHA-1
                return (SHA1.digestLength == digest.length());
            case 2: // SHA-256
                return (SHA256.digestLength == digest.length());
            case 3: // GOST R 34.11-94
                return (GOSTR341194.digestLength == digest.length());
            case 4: // SHA-384
                return (SHA384.digestLength == digest.length());
            default:
                // we do not validate length on unknown digest types, let them through
                // (may be a new type we do not know about)
                return true;
        }
    }
}