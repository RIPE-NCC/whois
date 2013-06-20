package net.ripe.db.whois.api.whois.domain;

import net.ripe.db.whois.api.whois.ValidXmlAdapter;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class RdapVCard {

    public String version = "version";

    /*public RdapVCard(final String name, final String value, final String comment, final String referencedType, final Link link) {
        this.name = name;
        this.value = value;
        this.comment = comment;
        this.referencedType = referencedType;
        this.link = link;
    }

    public RdapVCard(final String name, final String value) {
        this(name, value, null, null, null);
    }*/

    public RdapVCard() {
        // required no-arg constructor
    }

    /*public Link getLink() {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RdapVCard attribute = (RdapVCard) o;
        return (attribute.name != null ? attribute.name.equals(name) : name == null) &&
                (attribute.value != null ? attribute.value.equals(value) : value == null) &&
                (attribute.comment != null ? attribute.comment.equals(comment) : comment == null) &&
                (attribute.referencedType != null ? attribute.referencedType.equals(referencedType) : referencedType == null) &&
                (attribute.link != null ? attribute.link.equals(link) : link == null);
    }*/
}
