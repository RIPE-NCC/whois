package net.ripe.db.whois.internal.api.acl;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.util.Date;

import static net.ripe.db.whois.common.domain.BlockEvent.Type;

/**
 * ACL ban event.
 * <p/>
 * Ban events are created every time an addres prefix range gets temporary banned, permanently banned or
 * unbanned. A ban event describes the event for the exact matching address prefix range.
 */
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class BanEvent {
    private String prefix;
    private Date time;
    private Type type;
    private int personObjectLimit;

    public BanEvent() {
    }

    public BanEvent(final String prefix, final Timestamp time, final Type type, final int personObjectLimit) {
        this.prefix = prefix;
        this.time = time;
        this.type = type;
        this.personObjectLimit = personObjectLimit;
    }

    /**
     * the IPv4 or IPv6 address prefix range
     */
    @XmlElement(required = true)
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    /**
     * time when the event occured
     */
    @XmlElement(required = false)
    @XmlSchemaType(name = "datetime", type = XMLGregorianCalendar.class)
    public Date getTime() {
        return time;
    }

    public void setTime(final Date time) {
        this.time = time;
    }

    /**
     * event type
     */
    @XmlElement(required = false)
    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    /**
     * daily limit for person object queries
     */
    @XmlElement
    public int getPersonObjectLimit() {
        return personObjectLimit;
    }

    public void setPersonObjectLimit(final int personObjectLimit) {
        this.personObjectLimit = personObjectLimit;
    }
}
