package net.ripe.db.whois.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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

    public Domain() {
        super();
        super.setObjectClassName("domain");
    }

    public Domain(final String handle, final String ldhName, final String unicodeName, final List<Nameserver> nameservers, final Domain.SecureDNS secureDNS, final Map publicIds) {
        this();
        this.handle = handle;
        this.ldhName = ldhName;
        this.unicodeName = unicodeName;
        this.nameservers = nameservers;
        this.secureDNS = secureDNS;
        this.publicIds = publicIds;
    }

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

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        return Objects.equals(((Domain)object).handle, handle) &&
            Objects.equals(((Domain)object).ldhName, ldhName) &&
            Objects.equals(((Domain)object).unicodeName, unicodeName) &&
            Objects.equals(((Domain)object).nameservers, nameservers) &&
            Objects.equals(((Domain)object).secureDNS, secureDNS) &&
            Objects.equals(((Domain)object).publicIds, publicIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle, ldhName, unicodeName, nameservers, secureDNS, publicIds);
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

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }

            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            return Objects.equals(((SecureDNS)object).zoneSigned, zoneSigned) &&
                Objects.equals(((SecureDNS)object).delegationSigned, delegationSigned) &&
                Objects.equals(((SecureDNS)object).maxSigLife, maxSigLife) &&
                Objects.equals(((SecureDNS)object).dsData, dsData) &&
                Objects.equals(((SecureDNS)object).keyData, keyData);
        }

        @Override
        public int hashCode() {
            return Objects.hash(zoneSigned, delegationSigned, maxSigLife, dsData, keyData);
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

            public DsData() {
                // required no-arg constructor
            }

            public DsData(final long keyTag, final int algorithm, final String digest, final int digestType, final List<Event> events) {
                this.keyTag = keyTag;
                this.algorithm = algorithm;
                this.digest = digest;
                this.digestType = digestType;
                this.events = events;
            }

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

            @Override
            public boolean equals(final Object object) {
                if (this == object) {
                    return true;
                }

                if (object == null || getClass() != object.getClass()) {
                    return false;
                }

                return Objects.equals(((DsData)object).keyTag, keyTag) &&
                    Objects.equals(((DsData)object).algorithm, algorithm) &&
                    Objects.equals(((DsData)object).digest, digest) &&
                    Objects.equals(((DsData)object).digestType, digestType) &&
                    Objects.equals(((DsData)object).events, events);
            }

            @Override
            public int hashCode() {
                return Objects.hash(keyTag, algorithm, digest, digestType, events);
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

            @Override
            public boolean equals(final Object object) {
                if (this == object) {
                    return true;
                }

                if (object == null || getClass() != object.getClass()) {
                    return false;
                }

                return Objects.equals(((KeyData)object).flags, flags) &&
                    Objects.equals(((KeyData)object).protocol, protocol) &&
                    Objects.equals(((KeyData)object).publicKey, publicKey) &&
                    Objects.equals(((KeyData)object).algorithm, algorithm) &&
                    Objects.equals(((KeyData)object).events, events);
            }

            @Override
            public int hashCode() {
                return Objects.hash(flags, protocol, publicKey, algorithm, events);
            }
        }
    }
}
