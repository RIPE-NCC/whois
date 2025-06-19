package net.ripe.db.whois.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Autnum extends RdapObject implements Serializable {

    @XmlElement(required = true)
    protected String handle;
    @XmlElement(required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected Long startAutnum;
    @XmlElement(required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected Long endAutnum;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String country;

    public Autnum() {
        super();
        super.setObjectClassName("autnum");
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(final String value) {
        this.handle = value;
    }

    public Long getStartAutnum() {
        return startAutnum;
    }

    public void setStartAutnum(final Long value) {
        this.startAutnum = value;
    }

    public Long getEndAutnum() {
        return endAutnum;
    }

    public void setEndAutnum(final Long value) {
        this.endAutnum = value;
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        this.name = value;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String value) {
        this.country = value;
    }
}
