package net.ripe.db.whois.rdap.domain.vcard;

public enum VCardType {

    TEXT("text"),
    URI("uri");

    final String value;

    VCardType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
