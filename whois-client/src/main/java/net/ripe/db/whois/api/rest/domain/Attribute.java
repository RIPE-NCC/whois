package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.ripe.db.whois.api.rest.mapper.ValidXmlAdapter;
import org.apache.commons.lang.StringUtils;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Objects;

@Immutable
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

    public String getValue() {
        return value;
    }

    public String getReferencedType() {
        return referencedType;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
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

        return Objects.equals(attribute.name, name) &&
                Objects.equals(attribute.value, value) &&
                Objects.equals(attribute.comment, comment) &&
                Objects.equals(attribute.referencedType, referencedType) &&
                Objects.equals(attribute.link, link);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(": ").append(value);

        if (!StringUtils.isBlank(comment)) {
            builder.append(" # ").append(comment);
        }

        if (!StringUtils.isBlank(referencedType)) {
            builder.append(" [").append(referencedType).append("]");
        }

        if (link != null) {
            builder.append(" [").append(link).append("]");
        }

        return builder.toString();
    }
}
