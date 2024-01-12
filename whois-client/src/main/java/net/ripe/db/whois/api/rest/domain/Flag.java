package net.ripe.db.whois.api.rest.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import net.ripe.db.whois.query.QueryFlag;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "flag")
public class Flag {

    @XmlAttribute(name = "value", required = true)
    private String value;

    public Flag(final QueryFlag value) {
        this.value = value.getName();
    }

    public Flag() {
        // required no-arg constructor
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || (o.getClass() != getClass())) {
            return false;
        }

        return Objects.equals(value, ((Flag)o).getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
