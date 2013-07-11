package net.ripe.db.whois.api.whois.rdap.domain;

import javax.xml.bind.annotation.*;
import java.io.Serializable;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "handle",
        "startAutnum",
        "endAutnum",
        "name",
        "type",
        "country"
})
@XmlRootElement(name = "autnum")
public class Autnum extends RdapObject implements Serializable {

    @XmlElement(required = true)
    protected String handle;
    @XmlElement(required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected long startAutnum;
    @XmlElement(required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected long endAutnum;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String type;
    @XmlElement(required = true)
    protected String country;

    public String getHandle() {
        return handle;
    }

    public void setHandle(final String value) {
        this.handle = value;
    }

    public long getStartAutnum() {
        return startAutnum;
    }

    public void setStartAutnum(final long value) {
        this.startAutnum = value;
    }

    public long getEndAutnum() {
        return endAutnum;
    }

    public void setEndAutnum(final long value) {
        this.endAutnum = value;
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        this.name = value;
    }

    public String getType() {
        return type;
    }

    public void setType(final String value) {
        this.type = value;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String value) {
        this.country = value;
    }
}
