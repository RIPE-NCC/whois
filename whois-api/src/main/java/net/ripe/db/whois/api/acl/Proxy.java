package net.ripe.db.whois.api.acl;

import net.ripe.db.whois.common.domain.IpInterval;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * ACL proxy.
 * <p/>
 * A proxy applies to the exact matching address prefix range.
 */
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class Proxy {
    private String prefix;
    private String comment;

    public Proxy() {
    }

    public Proxy(final String prefix, final String comment) {
        this.prefix = IpInterval.parse(prefix).toString();
        this.comment = comment;
    }

    /**
     * the IPv4 or IPv6 address prefix range
     */
    @XmlElement(required = true)
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = IpInterval.parse(prefix).toString();
    }

    /**
     * free text for reference
     */
    @XmlElement(required = true)
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }
}
