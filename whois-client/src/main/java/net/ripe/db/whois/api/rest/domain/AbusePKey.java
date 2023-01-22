package net.ripe.db.whois.api.rest.domain;

import jakarta.annotation.concurrent.Immutable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class AbusePKey {
    @XmlAttribute(name = "value")
    private String value;

    public AbusePKey(final String value) {
        this.value = value;
    }

    public AbusePKey() {
        // required no-arg constructor
    }

    public String getValue() {
        return value;
    }
}
