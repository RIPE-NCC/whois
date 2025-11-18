package net.ripe.db.whois.rdap.domain.vcard;

public enum VCardName {

    ORG("org"),
    VERSION("version"),
    TELEPHONE("tel"),
    KIND("kind"),
    GEO("geo"),
    FN("fn"),
    EMAIL("email"),
    ADDRESS("adr");

    final String value;

    VCardName(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
