package net.ripe.db.whois.api.rest.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.annotation.concurrent.Immutable;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value",
    "link"
})
@XmlRootElement(name = "language")
public class Language {
    @XmlAttribute
    private String value;

    @XmlElement
    private Link link;

    public Language(final String value, final Link link) {
        this.value = value;
        this.link = link;
    }

    public Language() {
        // required no-arg constructor
    }
}
