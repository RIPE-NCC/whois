package net.ripe.db.whois.common.rpki;

public enum TrustAnchor {

    ARIN("arin"),
    LACNIC("lacnic"),
    RIPE("ripe"),
    AFRINIC("afrinic"),
    APNIC("apnic"),
    UNSUPPORTED("unsupported");

    private final String name;

    TrustAnchor(final String name) {
        this.name = name;
    }

    public static TrustAnchor fromRpkiName(final String name) {
        for (TrustAnchor trustAnchor : values()) {
            if (name.equals(trustAnchor.getName())) {
                return trustAnchor;
            }
        }
        return UNSUPPORTED;
    }

    public String getName() {
        return name;
    }
}
