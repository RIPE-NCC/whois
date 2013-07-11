package net.ripe.db.whois.api.whois.rdap.domain;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nameserver", propOrder = {
    "handle",
    "ldhName",
    "unicodeName",
    "ipAddresses",
    "port43"
})
@XmlRootElement
public class Nameserver
    extends RdapObject
    implements Serializable
{
    @XmlElement(required = true)
    protected String handle;
    protected String ldhName;
    protected String unicodeName;
    protected Nameserver.IpAddresses ipAddresses;
    protected String port43;

    public String getHandle() {
        return handle;
    }

    public void setHandle(String value) {
        this.handle = value;
    }

    public String getLdhName() {
        return ldhName;
    }

    public void setLdhName(String value) {
        this.ldhName = value;
    }

    public String getUnicodeName() {
        return unicodeName;
    }

    public void setUnicodeName(String value) {
        this.unicodeName = value;
    }

    public Nameserver.IpAddresses getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(Nameserver.IpAddresses value) {
        this.ipAddresses = value;
    }

    public String getPort43() {
        return port43;
    }

    public void setPort43(String value) {
        this.port43 = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "ipv4",
        "ipv6"
    })
    public static class IpAddresses
        implements Serializable
    {
        protected List<String> ipv4;
        protected List<String> ipv6;

        public List<String> getIpv4() {
            if (ipv4 == null) {
                ipv4 = Lists.newArrayList();
            }
            return this.ipv4;
        }

        public List<String> getIpv6() {
            if (ipv6 == null) {
                ipv6 = Lists.newArrayList();
            }
            return this.ipv6;
        }
    }

}
