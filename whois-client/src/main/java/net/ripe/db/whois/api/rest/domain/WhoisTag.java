package net.ripe.db.whois.api.rest.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
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
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final WhoisTag whoisTag = (WhoisTag) o;
        return (whoisTag.id != null ? whoisTag.id.equals(id) : id == null) &&
                (whoisTag.data != null ? whoisTag.data.equals(data) : data == null);
    }
}
