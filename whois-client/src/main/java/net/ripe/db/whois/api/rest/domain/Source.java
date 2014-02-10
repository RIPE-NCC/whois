package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "source")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Source {

    @XmlAttribute(required = true)
    private String name;
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

    public String getName() {
        return name;
    }

    public Source setName(String value) {
        this.name = value;
        return this;
    }

    @Override
    public int hashCode() {
        int result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Source source = (Source)o;
        return Objects.equals(source.id, id) &&
                Objects.equals(source.name, name);
    }
}
