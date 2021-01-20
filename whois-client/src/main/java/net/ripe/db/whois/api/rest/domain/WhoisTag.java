package net.ripe.db.whois.api.rest.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "tag")
public class WhoisTag {

    @XmlAttribute(name = "id")
    private String id;
    @XmlAttribute(name = "data")
    private String data;

    public WhoisTag(final String id, final String data) {
        this.id = id;
        this.data = data;
    }

    public WhoisTag() {
        // required no-arg constructor
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final WhoisTag whoisTag = (WhoisTag) o;

        return Objects.equals(whoisTag.id, id) &&
                Objects.equals(whoisTag.data, data);
    }
}
