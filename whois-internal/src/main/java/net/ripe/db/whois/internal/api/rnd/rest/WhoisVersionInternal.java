package net.ripe.db.whois.internal.api.rnd.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.ripe.db.whois.api.rest.domain.Link;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "type",
        "key",
        "revision",
        "from",
        "to",
        "link"
})
@XmlRootElement(name = "version")
public class WhoisVersionInternal {
    @XmlElement(name = "type", required = false)
    private String type;
    @JsonProperty("pkey")
    @XmlElement(name = "key", required = false)
    private String key;
    @XmlElement(name = "revision", required = false)
    private int revision;
    @XmlElement(name = "from")
    private String from;
    @XmlElement(name = "to", required = false)
    private String to;
    @XmlElement
    private Link link;
    @JsonIgnoreProperties
    private int versionId;


    public WhoisVersionInternal(final int revision, final String from, final String to, final Link link) {
        this.revision = revision;
        this.from = from;
        this.to = to;
        this.link = link;
    }

    public WhoisVersionInternal(final int revision, final String type, final String key, final String from, final String to, Link link) {
        this.type = type;
        this.key = key;
        this.revision = revision;
        this.from = from;
        this.to = to;
        this.link = link;
    }

    public WhoisVersionInternal() {
        // required no-arg constructor
    }

    public int getRevision() {
        return revision;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Link getLink() {
        return link;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WhoisVersionInternal)) return false;

        WhoisVersionInternal that = (WhoisVersionInternal) o;

        if (revision != that.revision) return false;
        if (!from.equals(that.from)) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (!link.equals(that.link)) return false;
        if (!to.equals(that.to)) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + revision;
        result = 31 * result + from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + link.hashCode();
        return result;
    }
}
