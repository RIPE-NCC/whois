package net.ripe.db.whois.api.rdap.domain.vcard;

public enum VCardKind {

    INDIVIDUAL("individual"),
    ORGANISATION("org"),
    GROUP("group");

    final String value;

    VCardKind(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
