package net.ripe.db.whois.internal.api.acl;

import net.ripe.db.whois.common.domain.ip.IpInterval;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

/**
 * ACL permanent ban.
 * <p/>
 * A permanent ban applies to the exact matching address prefix range.
 */
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class Ban {
    private String prefix;
    private String comment;
    private Date since;

    public Ban() {
    }

    public Ban(final String prefix, final String comment) {
        this.prefix = IpInterval.parse(prefix).toString();
        this.comment = comment;
    }

    public Ban(final String prefix, final String comment, final Date since) {
        this.prefix = prefix;
        this.comment = comment;
        this.since = since;
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
     * creation date of the ban, ignored for updates
     */
    @XmlElement(required = false)
    @XmlSchemaType(name = "date", type = XMLGregorianCalendar.class)
    public Date getSince() {
        return since;
    }

    public void setSince(final Date since) {
        this.since = since;
    }
}
