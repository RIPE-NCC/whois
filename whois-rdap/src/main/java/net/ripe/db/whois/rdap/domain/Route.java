package net.ripe.db.whois.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "handle",
    "origin",
    "country",
    "pingable",
    "pingable",
    "ping_hdl",
    "holes"
})
@XmlRootElement(name = "ip")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Route extends RdapObject implements Serializable {
    @XmlElement(required = true)
    protected String handle;
    @XmlElement(required = true)
    protected String origin;
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
    @XmlElement(required = true)
    protected List<IpCidr0> cidr0_cidrs;

    public Route() {
        super();
        super.setObjectClassName("ip network");
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String value) {
        this.handle = value;
    }

    public void setCidr0_cidrs(List<IpCidr0> cidr0_cidrs) {
        this.cidr0_cidrs = cidr0_cidrs;
    }

    public List<IpCidr0> getCidr0_cidrs() {
        return cidr0_cidrs;
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
