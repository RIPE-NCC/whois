package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "source")
@JsonInclude(NON_EMPTY)
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

    public Source setId(final String value) {
        this.id = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Source source = (Source) o;

        return Objects.equals(source.id, id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
