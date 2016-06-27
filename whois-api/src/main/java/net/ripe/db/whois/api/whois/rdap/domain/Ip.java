package net.ripe.db.whois.api.whois.rdap.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "handle",
    "startAddress",
    "endAddress",
    "ipVersion",
    "name",
    "type",
    "country",
    "parentHandle"
})
@XmlRootElement(name = "ip")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class Ip extends RdapObject implements Serializable {
    @XmlElement(required = true)
    protected String handle;
    @XmlElement(required = true)
    protected String startAddress;
    @XmlElement(required = true)
    protected String endAddress;
    @XmlElement(required = true)
    protected String ipVersion;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String type;
    @XmlElement(required = true)
    protected String country;
    @XmlElement(required = true)
    protected String parentHandle;

    public Ip() {
        super();
        super.setObjectClassName("ip network");
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String value) {
        this.handle = value;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String value) {
        this.startAddress = value;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String value) {
        this.endAddress = value;
    }

    public String getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(String value) {
        this.ipVersion = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String value) {
        this.country = value;
    }

    public String getParentHandle() {
        return parentHandle;
    }

    public void setParentHandle(String value) {
        this.parentHandle = value;
    }
}
