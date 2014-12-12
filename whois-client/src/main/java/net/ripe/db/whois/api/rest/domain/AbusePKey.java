package net.ripe.db.whois.api.rest.domain;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

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
