package net.ripe.db.whois.api.whois.rdap.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nameserver", propOrder = {
    "handle",
    "ldhName",
    "unicodeName",
    "ipAddresses"
})
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class Nameserver extends RdapObject implements Serializable {
    @XmlElement(required = true)
    protected String handle;
    @XmlElement
    protected String ldhName;
    @XmlElement
    protected String unicodeName;
    @XmlElement
    protected Nameserver.IpAddresses ipAddresses;

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

        public IpAddresses(final List<String> ipv4, final List<String> ipv6) {
            this.ipv4 = ipv4;
            this.ipv6 = ipv6;
        }

        public IpAddresses() {
            // required no-arg constructor
        }

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

        @Override
        public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final IpAddresses ipAddresses = (IpAddresses) object;
        return (ipAddresses.ipv4 != null ? ipAddresses.ipv4.equals(ipv4) : ipv4 == null) &&
                (ipAddresses.ipv6 != null ? ipAddresses.ipv6.equals(ipv6) : ipv6 == null);
        }
    }
}
