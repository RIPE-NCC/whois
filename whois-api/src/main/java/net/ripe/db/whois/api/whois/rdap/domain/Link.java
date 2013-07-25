package net.ripe.db.whois.api.whois.rdap.domain;

import com.google.common.collect.Lists;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "link", propOrder = {
        "value",
        "rel",
        "href",
        "hreflang",
        "title",
        "media",
        "type"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class Link implements Serializable, Comparable<Link> {
    @XmlElement(required = true)
    protected String value;
    @XmlElement(required = true)
    protected String rel;
    @XmlElement(required = true)
    protected String href;
    @XmlElement
    protected List<String> hreflang;
    @XmlElement
    protected List<String> title;
    @XmlElement
    protected String media;
    @XmlElement
    protected String type;

    public String getValue() {
        return value;
    }

    public Link setValue(final String value) {
        this.value = value;
        return this;
    }

    public String getRel() {
        return rel;
    }

    public Link setRel(final String value) {
        this.rel = value;
        return this;
    }

    public String getHref() {
        return href;
    }

    public Link setHref(final String value) {
        this.href = value;
        return this;
    }

    public List<String> getHreflang() {
        if (hreflang == null) {
            hreflang = Lists.newArrayList();
        }
        return this.hreflang;
    }

    public List<String> getTitle() {
        if (title == null) {
            title = Lists.newArrayList();
        }
        return this.title;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(final String value) {
        this.media = value;
    }

    public String getType() {
        return type;
    }

    public void setType(final String value) {
        this.type = value;
    }

    @Override
    public int compareTo(Link o) {
        return this.getRel().compareTo(o.getRel());
    }
}
