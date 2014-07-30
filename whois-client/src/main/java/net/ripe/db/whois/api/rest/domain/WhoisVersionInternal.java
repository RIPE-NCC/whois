package net.ripe.db.whois.api.rest.domain;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.*;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "revision",
        "from",
        "to",
        "link"

})
@XmlRootElement(name = "version")
public class WhoisVersionInternal {
    @XmlElement(name = "revision", required = false)
    private int revision;
    @XmlElement(name = "from")
    private String from;
    @XmlElement(name = "to", required = false)
    private String to;
    @XmlElement
    private Link link;


    public WhoisVersionInternal(final int revision, final String from, final String to, final Link link) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final WhoisVersionInternal that = (WhoisVersionInternal) o;

        if (revision != that.revision) return false;
        if (from != null ? !from.equals(that.from) : that.from != null) return false;
        if (link != null ? !link.equals(that.link) : that.link != null) return false;
        if (to != null ? !to.equals(that.to) : that.to != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = revision;
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        result = 31 * result + (link != null ? link.hashCode() : 0);
        return result;
    }
}
