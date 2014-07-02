package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "type",
    "link",
    "source",
    "primaryKey",
    "attributes",
    "tags",
    "versionDateTime"
})
@JsonInclude(NON_EMPTY)
@XmlRootElement(name = "object")
public class WhoisObject {

    @XmlElement
    private Link link;

    @XmlElement
    private Source source;

    @XmlElement(name = "primary-key")
    private PrimaryKey primaryKey;

    @XmlElement(name = "attributes", required = true)
    private Attributes attributes;

    @XmlElement(name = "tags")
    private WhoisTags tags;

    @XmlAttribute(required = true)
    private String type;

    @XmlAttribute(name = "version")
    private Integer version;

    @XmlElement(name="version-datetime")
    private String versionDateTime;

    public Link getLink() {
        return link;
    }

    public void setLink(Link value) {
        this.link = value;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source value) {
        this.source = value;
    }

    public List<Attribute> getPrimaryKey() {
        return primaryKey != null ? primaryKey.getAttributes() : Collections.<Attribute>emptyList();
    }

    public void setPrimaryKey(final List<Attribute> value) {
        this.primaryKey = new PrimaryKey(value);
    }

    public List<Attribute> getAttributes() {
        return attributes != null ? attributes.getAttributes() : Collections.<Attribute>emptyList();
    }

    public void setAttributes(final List<Attribute> value) {
        this.attributes = new Attributes(value);
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }

    public List<WhoisTag> getTags() {
        return tags != null ? tags.getTags() : Collections.<WhoisTag>emptyList();
    }

    public void setTags(final List<WhoisTag> tags) {
        this.tags = new WhoisTags(tags);
    }

    public String getVersionDateTime() {
        return versionDateTime;
    }

    public void setVersionDateTime(String versionDateTime) {
        this.versionDateTime = versionDateTime;
    }

    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (Attribute attribute : getAttributes()) {
            builder.append(attribute.toString()).append('\n');
        }
        return builder.toString();
    }
}
