

package net.ripe.db.whois.api.whois.rdap.domain;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "handle",
    "ldhName",
    "unicodeName",
    "nameservers",
    "secureDNS",
    "publicIds",
    "port43"
})
@XmlRootElement(name = "domain")
public class Domain extends RdapObject implements Serializable {

    @XmlElement(required = true)
    protected String handle;
    @XmlElement(required = true)
    protected String ldhName;
    @XmlElement(required = true)
    protected String unicodeName;
    @XmlElement(required = true, name = "nameServers")
    protected List<Nameserver> nameservers;
    protected Domain.SecureDNS secureDNS;
    protected Map publicIds;
    protected String port43;

    public String getHandle() {
        return handle;
    }

    public void setHandle(final String value) {
        this.handle = value;
    }

    public String getLdhName() {
        return ldhName;
    }

    public void setLdhName(final String value) {
        this.ldhName = value;
    }

    public String getUnicodeName() {
        return unicodeName;
    }

    public void setUnicodeName(final String value) {
        this.unicodeName = value;
    }

    public List<Nameserver> getNameservers() {
        if (nameservers == null) {
            nameservers = Lists.newArrayList();
        }
        return this.nameservers;
    }

    public Domain.SecureDNS getSecureDNS() {
        return secureDNS;
    }

    public void setSecureDNS(Domain.SecureDNS value) {
        this.secureDNS = value;
    }

    public Map getPublicIds() {
        return publicIds;
    }

    public void setPublicIds(final Map value) {
        this.publicIds = value;
    }

    public String getPort43() {
        return port43;
    }

    public void setPort43(final String value) {
        this.port43 = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "zoneSigned",
        "delegationSigned",
        "maxSigLife",
        "dsData",
        "keyData"
    })
    public static class SecureDNS
        implements Serializable
    {

        protected Boolean zoneSigned;
        protected Boolean delegationSigned;
        protected Long maxSigLife;
        protected List<Domain.SecureDNS.DsData> dsData;
        protected List<Domain.SecureDNS.KeyData> keyData;

        public Boolean isZoneSigned() {
            return zoneSigned;
        }

        public void setZoneSigned(Boolean value) {
            this.zoneSigned = value;
        }

        public Boolean isDelegationSigned() {
            return delegationSigned;
        }

        public void setDelegationSigned(Boolean value) {
            this.delegationSigned = value;
        }

        public Long getMaxSigLife() {
            return maxSigLife;
        }

        public void setMaxSigLife(Long value) {
            this.maxSigLife = value;
        }

        public List<Domain.SecureDNS.DsData> getDsData() {
            if (dsData == null) {
                dsData = new ArrayList<Domain.SecureDNS.DsData>();
            }
            return this.dsData;
        }

        public List<Domain.SecureDNS.KeyData> getKeyData() {
            if (keyData == null) {
                keyData = new ArrayList<Domain.SecureDNS.KeyData>();
            }
            return this.keyData;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "keyTag",
            "algorithm",
            "digest",
            "digestType",
            "events"
        })
        public static class DsData
            implements Serializable
        {

            @XmlSchemaType(name = "unsignedInt")
            protected long keyTag;
            @XmlSchemaType(name = "unsignedByte")
            protected int algorithm;
            @XmlElement(required = true)
            protected String digest;
            @XmlSchemaType(name = "unsignedInt")
            @XmlElement(required = true)
            protected int digestType;
            protected List<Event> events;

            public long getKeyTag() {
                return keyTag;
            }

            public void setKeyTag(long value) {
                this.keyTag = value;
            }

            public int getAlgorithm() {
                return algorithm;
            }

            public void setAlgorithm(int value) {
                this.algorithm = value;
            }

            public String getDigest() {
                return digest;
            }

            public void setDigest(String value) {
                this.digest = value;
            }

            public int getDigestType() {
                return digestType;
            }

            public void setDigestType(int value) {
                this.digestType = value;
            }

            public List<Event> getEvents() {
                if (events == null) {
                    events = new ArrayList<Event>();
                }
                return this.events;
            }

        }


        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "flags",
            "protocol",
            "publicKey",
            "algorithm",
            "events"
        })
        public static class KeyData
            implements Serializable
        {

            @XmlElement(required = true)
            protected String flags;
            @XmlElement(required = true)
            protected String protocol;
            @XmlElement(required = true)
            protected String publicKey;
            @XmlSchemaType(name = "unsignedInt")
            @XmlElement(required = true)
            protected int algorithm;
            protected List<Event> events;

            public String getFlags() {
                return flags;
            }

            public void setFlags(String value) {
                this.flags = value;
            }

            public String getProtocol() {
                return protocol;
            }

            public void setProtocol(String value) {
                this.protocol = value;
            }

            public String getPublicKey() {
                return publicKey;
            }

            public void setPublicKey(String value) {
                this.publicKey = value;
            }

            public int getAlgorithm() {
                return algorithm;
            }

            public void setAlgorithm(int value) {
                this.algorithm = value;
            }

            public List<Event> getEvents() {
                if (events == null) {
                    events = new ArrayList<Event>();
                }
                return this.events;
            }

        }

    }

}
