package net.ripe.db.whois.api.rdap.domain.vcard;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
