package net.ripe.db.whois.api.acl;

import net.ripe.db.whois.common.domain.IpInterval;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * ACL limit.
 * <p/>
 * For any address prefix range the most specific parent limit applies.
 */
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class Limit {
    private String prefix;
    private String comment;
    private int personObjectLimit;
    private boolean unlimitedConnections;

    public Limit() {
    }

    public Limit(final String prefix, final String comment, final int personObjectLimit, final boolean unlimitedConnections) {
        this.prefix = IpInterval.parse(prefix).toString();
        this.comment = comment;
        this.personObjectLimit = personObjectLimit;
        this.unlimitedConnections = unlimitedConnections;
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

    /**
     * daily limit for person object queries
     */
    @XmlElement(required = true)
    public int getPersonObjectLimit() {
        return personObjectLimit;
    }

    public void setPersonObjectLimit(final int personObjectLimit) {
        this.personObjectLimit = personObjectLimit;
    }

    /**
     * allow unlimited connections from a single IP
     */
    @XmlElement(required = true)
    public boolean isUnlimitedConnections() {
        return unlimitedConnections;
    }

    public void setUnlimitedConnections(final boolean unlimitedConnections) {
        this.unlimitedConnections = unlimitedConnections;
    }
}
