package net.ripe.db.whois.api.rest.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
public class Link {

    public static final String XLINK_URI = "http://www.w3.org/1999/xlink";

    @XmlAttribute(namespace = XLINK_URI)
    private String type;

    @XmlAttribute(namespace = XLINK_URI)
    private String href;

    public Link(final String type, final String href) {
        this.type = type;
        this.href = href;
    }

    public Link() {
        // required no-arg constructor
    }

    // factory

    public static Link create(final String baseUrl, final RpslObject rpslObject) {
        final String source = rpslObject.getValueForAttribute(AttributeType.SOURCE).toString().toLowerCase();
        final String type = rpslObject.getType().getName();
        final String key = rpslObject.getKey().toString();
        return create(baseUrl, source, type, key);
    }

    public static Link create(final String baseUrl, final String source, final String type, final String key) {
        return create(String.format("%s/%s/%s/%s", baseUrl, source, type, key));
    }

    public static Link create(final String href) {
        return new Link("locator", href);
    }

    // getters

    public String getType() {
        return type;
    }

    public String getHref() {
        return href;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, href);
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Link link = (Link) o;

        return Objects.equals(link.type, type) &&
                Objects.equals(link.href, href);
    }

    @Override
    public String toString() {
        return String.format("%s: %s", type, href);
    }
}
