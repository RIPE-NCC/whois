package net.ripe.db.whois.api.whois.rdap.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

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

    public Link() {
        // required no-arg constructor
    }

    public Link(final String value, final String rel, final String href, final String media, final String type) {

        if (Strings.isNullOrEmpty(href)) {
            throw new IllegalArgumentException("link href is required");
        }

        if (Strings.isNullOrEmpty(rel)) {
            throw new IllegalArgumentException("link rel is required");
        }

        this.value = value;
        this.rel = rel;
        this.href = href;
        this.media = media;
        this.type = type;
    }


    public String getValue() {
        return value;
    }

    public String getRel() {
        return rel;
    }

    public String getHref() {
        return href;
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

    public String getType() {
        return type;
    }

    @Override
    public int compareTo(Link o) {
        return this.getRel().compareTo(o.getRel());
    }
}
