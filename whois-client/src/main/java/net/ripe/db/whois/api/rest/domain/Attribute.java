package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.ripe.db.whois.api.rest.mapper.ValidXmlAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "link",
    "name",
    "value",
    "referencedType"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@XmlRootElement(name = "attribute")
public class Attribute {

    @XmlElement
    protected Link link;
    @XmlAttribute(name = "value", required = true)
    @XmlJavaTypeAdapter(value = ValidXmlAdapter.class)
    protected String value;
    @XmlAttribute(name = "referenced-type")
    protected String referencedType;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "comment")
    protected String comment;

    public Attribute(final String name, final String value, final String comment, final String referencedType, final Link link) {
        this.name = name;
        this.value = value;
        this.comment = comment;
        this.referencedType = referencedType;
        this.link = link;
    }

    public Attribute(final String name, final String value) {
        this(name, value, null, null, null);
    }

    public Attribute() {
        // required no-arg constructor
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link value) {
        this.link = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getReferencedType() {
        return referencedType;
    }

    public void setReferencedType(String value) {
        this.referencedType = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String value) {
        this.comment = value;
    }

    @Override
    public int hashCode() {
        int result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (referencedType != null ? referencedType.hashCode() : 0);
        result = 31 * result + (link != null ? link.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Attribute attribute = (Attribute) o;
        return (attribute.name != null ? attribute.name.equals(name) : name == null) &&
                (attribute.value != null ? attribute.value.equals(value) : value == null) &&
                (attribute.comment != null ? attribute.comment.equals(comment) : comment == null) &&
                (attribute.referencedType != null ? attribute.referencedType.equals(referencedType) : referencedType == null) &&
                (attribute.link != null ? attribute.link.equals(link) : link == null);
    }

    public String toString() {
        return name + ": " + value;
    }
}
