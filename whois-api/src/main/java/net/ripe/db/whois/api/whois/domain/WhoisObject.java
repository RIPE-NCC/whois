package net.ripe.db.whois.api.whois.domain;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.*;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "type",
    "link",
    "source",
    "primaryKey",
    "attributes",
    "tags"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@XmlRootElement(name = "object")
public class WhoisObject {

    @XmlElement
    protected Link link;

    @XmlElement
    protected Source source;

    @XmlElement(name = "primary-key")
    protected PrimaryKey primaryKey;

    @XmlElement(name = "attributes", required = true)
    protected Attributes attributes;

    @XmlElement(name = "tags")
    protected WhoisTags tags;

    @XmlAttribute(required = true)
    protected String type;

    @XmlAttribute(name = "version")
    protected Integer version;

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
        return primaryKey != null ? primaryKey.attributes : null;
    }

    public void setPrimaryKey(List<Attribute> value) {
        this.primaryKey = new PrimaryKey(value);
    }

    public List<Attribute> getAttributes() {
        return attributes != null ? attributes.attributes : null;
    }

    public void setAttributes(List<Attribute> value) {
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

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<WhoisTag> getTags() {
        return tags != null ? tags.tags : null;
    }

    public void setTags(List<WhoisTag> tags) {
        this.tags = new WhoisTags(tags);
    }
}
