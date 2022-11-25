package net.ripe.db.whois.api.rdap.domain.vcard;

public enum VCardType {

    TEXT("text"),
    URI("uri"),
    WORK("work");


    final String value;

    VCardType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
