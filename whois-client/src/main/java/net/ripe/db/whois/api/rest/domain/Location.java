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
@XmlRootElement(name = "location")
public class Location {

    @XmlAttribute
    private String value;

    @XmlElement
    private Link link;

    public Location(final String value, final Link link) {
        this.value = value;
        this.link = link;
    }

    public Location() {
        // required no-arg constructor
    }
}
