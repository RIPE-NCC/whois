package net.ripe.db.whois.api.whois.rdap.domain;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

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
public class Domain
    extends RdapObject
    implements Serializable
{

    @XmlElement(required = true)
    protected String handle;
    @XmlElement(required = true)
    protected String ldhName;
    @XmlElement(required = true)
    protected String unicodeName;
    @XmlElement(required = true)
    protected List<Nameserver> nameservers;
    protected Domain.SecureDNS secureDNS;
    protected HashMap publicIds;
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

    public List<Nameserver> getNameservers() {
        if (nameservers == null) {
            nameservers = new ArrayList<Nameserver>();
        }
        return this.nameservers;
    }

    public Domain.SecureDNS getSecureDNS() {
        return secureDNS;
    }

    public void setSecureDNS(Domain.SecureDNS value) {
        this.secureDNS = value;
    }

    public HashMap getPublicIds() {
        return publicIds;
    }

    public void setPublicIds(HashMap value) {
        this.publicIds = value;
    }

    public String getPort43() {
        return port43;
    }

    public void setPort43(String value) {
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

        protected boolean zoneSigned;
        protected boolean delegationSigned;
        @XmlElement(required = true)
        @XmlSchemaType(name = "nonNegativeInteger")
        protected BigInteger maxSigLife;
        protected List<Domain.SecureDNS.DsData> dsData;
        protected List<Domain.SecureDNS.KeyData> keyData;

        public boolean isZoneSigned() {
            return zoneSigned;
        }

        public void setZoneSigned(boolean value) {
            this.zoneSigned = value;
        }

        public boolean isDelegationSigned() {
            return delegationSigned;
        }

        public void setDelegationSigned(boolean value) {
            this.delegationSigned = value;
        }

        public BigInteger getMaxSigLife() {
            return maxSigLife;
        }

        public void setMaxSigLife(BigInteger value) {
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

            @XmlElement(required = true)
            @XmlSchemaType(name = "nonNegativeInteger")
            protected BigInteger keyTag;
            @XmlElement(required = true)
            @XmlSchemaType(name = "nonNegativeInteger")
            protected BigInteger algorithm;
            @XmlElement(required = true)
            protected String digest;
            @XmlElement(required = true)
            @XmlSchemaType(name = "nonNegativeInteger")
            protected BigInteger digestType;
            protected List<Event> events;

            public BigInteger getKeyTag() {
                return keyTag;
            }

            public void setKeyTag(BigInteger value) {
                this.keyTag = value;
            }

            public BigInteger getAlgorithm() {
                return algorithm;
            }

            public void setAlgorithm(BigInteger value) {
                this.algorithm = value;
            }

            public String getDigest() {
                return digest;
            }

            public void setDigest(String value) {
                this.digest = value;
            }

            public BigInteger getDigestType() {
                return digestType;
            }

            public void setDigestType(BigInteger value) {
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
            @XmlElement(required = true)
            @XmlSchemaType(name = "nonNegativeInteger")
            protected BigInteger algorithm;
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

            public BigInteger getAlgorithm() {
                return algorithm;
            }

            public void setAlgorithm(BigInteger value) {
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
