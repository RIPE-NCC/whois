package net.ripe.db.whois.api.whois.rdap.domain;

import com.google.common.collect.Lists;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "handle",
    "ldhName",
    "unicodeName",
    "nameservers",
    "secureDNS",
    "publicIds"
})
@XmlRootElement(name = "domain")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "zoneSigned",
        "delegationSigned",
        "maxSigLife",
        "dsData",
        "keyData"
    })
    public static class SecureDNS implements Serializable {

        protected Boolean zoneSigned;
        protected Boolean delegationSigned;
        protected Long maxSigLife;
        protected List<Domain.SecureDNS.DsData> dsData;
        protected List<Domain.SecureDNS.KeyData> keyData;

        public Boolean isZoneSigned() {
            return zoneSigned;
        }

        public void setZoneSigned(final Boolean value) {
            this.zoneSigned = value;
        }

        public Boolean isDelegationSigned() {
            return delegationSigned;
        }

        public void setDelegationSigned(final Boolean value) {
            this.delegationSigned = value;
        }

        public Long getMaxSigLife() {
            return maxSigLife;
        }

        public void setMaxSigLife(final Long value) {
            this.maxSigLife = value;
        }

        public List<Domain.SecureDNS.DsData> getDsData() {
            if (dsData == null) {
                dsData = Lists.newArrayList();
            }
            return this.dsData;
        }

        public List<Domain.SecureDNS.KeyData> getKeyData() {
            if (keyData == null) {
                keyData = Lists.newArrayList();
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
        public static class DsData implements Serializable {

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

            public void setKeyTag(final long value) {
                this.keyTag = value;
            }

            public int getAlgorithm() {
                return algorithm;
            }

            public void setAlgorithm(final int value) {
                this.algorithm = value;
            }

            public String getDigest() {
                return digest;
            }

            public void setDigest(final String value) {
                this.digest = value;
            }

            public int getDigestType() {
                return digestType;
            }

            public void setDigestType(final int value) {
                this.digestType = value;
            }

            public List<Event> getEvents() {
                if (events == null) {
                    events = Lists.newArrayList();
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
        public static class KeyData implements Serializable {

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

            public void setFlags(final String value) {
                this.flags = value;
            }

            public String getProtocol() {
                return protocol;
            }

            public void setProtocol(final String value) {
                this.protocol = value;
            }

            public String getPublicKey() {
                return publicKey;
            }

            public void setPublicKey(final String value) {
                this.publicKey = value;
            }

            public int getAlgorithm() {
                return algorithm;
            }

            public void setAlgorithm(final int value) {
                this.algorithm = value;
            }

            public List<Event> getEvents() {
                if (events == null) {
                    events = Lists.newArrayList();
                }
                return this.events;
            }
        }
    }
}
