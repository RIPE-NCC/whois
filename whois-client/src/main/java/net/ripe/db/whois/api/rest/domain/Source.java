package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "source")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Source {

    @XmlAttribute(required = true)
    private String id;

    public Source(final String id) {
        this.id = id;
    }

    public Source() {
        // default no-arg constructor
    }

    public String getId() {
        return id;
    }

    public Source setId(String value) {
        this.id = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Source source = (Source) o;

        if (!id.equals(source.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
