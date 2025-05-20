package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.ripe.db.whois.api.rest.mapper.ValidXmlAdapter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "link",
    "name",
    "value",
    "referencedType"
})
@JsonInclude(NON_NULL)
@XmlRootElement(name = "attribute")
public class Attribute {

    @XmlElement
    private Link link;
    @XmlAttribute(name = "value", required = true)
    @XmlJavaTypeAdapter(value = ValidXmlAdapter.class)
    private String value;
    @XmlAttribute(name = "referenced-type")
    private String referencedType;
    @XmlAttribute(name = "name")
    private String name;
    @XmlAttribute(name = "comment")
    private String comment;
    @XmlAttribute
    private Boolean managed;

    public Attribute(
            final String name,
            final String value,
            @Nullable final String comment,
            @Nullable final String referencedType,
            @Nullable final Link link,
            @Nullable final Boolean managed) {
        this.name = name;
        this.value = value;
        this.comment = comment;
        this.referencedType = referencedType;
        this.link = link;
        this.managed = managed;
    }

    public Attribute(final String name, final String value) {
        this(name, value, null, null, null, null);
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

    public Boolean getManaged() {
        return managed;
    }

    public void setManaged(final Boolean managed) {
        this.managed = managed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, comment, referencedType, link, managed);
    }

    @Override
    public boolean equals(final Object o) {
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
                Objects.equals(attribute.link, link) &&
                Objects.equals(attribute.managed, managed);
    }

    /** does not properly handle multiline attributes; first line will have an extra space before the value */
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
