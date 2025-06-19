package net.ripe.db.whois.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nameserver", propOrder = {
    "handle",
    "ldhName",
    "unicodeName",
    "ipAddresses"
})
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Nameserver extends RdapObject implements Serializable {
    @XmlElement(required = true)
    protected String handle;
    @XmlElement
    protected String ldhName;
    @XmlElement
    protected String unicodeName;
    @XmlElement
    protected Nameserver.IpAddresses ipAddresses;

    public Nameserver() {
        super();
        super.setObjectClassName("nameserver");
    }

    public Nameserver(final String handle, final String ldhName, final String unicodeName, final Nameserver.IpAddresses ipAddresses) {
        this();
        this.handle = handle;
        this.ldhName = ldhName;
        this.unicodeName = unicodeName;
        this.ipAddresses = ipAddresses;
    }

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

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final Nameserver nameserver = (Nameserver)object;
        return Objects.equals(nameserver.handle, handle) &&
            Objects.equals(nameserver.ldhName, ldhName) &&
            Objects.equals(nameserver.unicodeName, unicodeName) &&
            Objects.equals(nameserver.ipAddresses, ipAddresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle, ldhName, unicodeName, ipAddresses);
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

            return Objects.equals(((IpAddresses)object).ipv4, ipv4) &&
                Objects.equals(((IpAddresses)object).ipv6, ipv6);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ipv4, ipv6);
        }
    }
}
