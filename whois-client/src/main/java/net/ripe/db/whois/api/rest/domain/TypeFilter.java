package net.ripe.db.whois.api.rest.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.annotation.concurrent.Immutable;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "type-filter")
public class TypeFilter {

    @XmlAttribute(name = "id", required = true)
    private String id;

    public TypeFilter(final String id) {
        this.id = id;
    }

    public TypeFilter() {
        // required no-arg constructor
    }

    public String getId() {
        return id;
    }
}
