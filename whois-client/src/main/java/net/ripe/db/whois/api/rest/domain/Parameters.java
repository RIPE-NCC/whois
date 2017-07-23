package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "",
    propOrder = {
        "inverseAttributes",
        "typeFilters",
        "flags",
        "queryStrings",
        "primaryKey",
        "sources",
        "managedAttributes",
        "resourceHolder",
        "abuseContact",
        "limit",
        "offset"
    }
)
@XmlRootElement(name = "parameters")
public class Parameters {
    @XmlElement(name = "inverse-lookup")
    @JsonProperty(value = "inverse-lookup")
    private InverseAttributes inverseAttributes;
    @XmlElement(name="type-filters")
    @JsonProperty(value = "type-filters")
    private TypeFilters typeFilters;
    @XmlElement(name = "flags")
    private Flags flags;
    @XmlElement(name = "query-strings", required = true)
    @JsonProperty(value = "query-strings")
    private QueryStrings queryStrings;
    @XmlElement(name = "sources", required = true)
    private Sources sources;
    @XmlElement(name = "primary-key")
    @JsonProperty(value = "primary-key")
    private AbusePKey primaryKey;
    @XmlElement(name="managed-attributes", nillable = true)
    private Boolean managedAttributes;
    @XmlElement(name="resource-holder", nillable = true)
    private Boolean resourceHolder;
    @XmlElement(name="abuse-contact", nillable = true)
    private Boolean abuseContact;
    @XmlElement(nillable = true)
    private Integer limit;
    @XmlElement(nillable = true)
    private Integer offset;

    public Parameters(
            final InverseAttributes inverseAttributes,
            final TypeFilters typeFilters,
            final Flags flags,
            final QueryStrings queryStrings,
            final Sources sources,
            final AbusePKey primaryKey,
            final Boolean managedAttributes,
            final Boolean resourceHolder,
            final Boolean abuseContact,
            final Integer limit,
            final Integer offset) {
        this.inverseAttributes = inverseAttributes;
        this.typeFilters = typeFilters;
        this.flags = flags;
        this.queryStrings = queryStrings;
        this.sources = sources;
        this.primaryKey = primaryKey;
        this.managedAttributes = managedAttributes;
        this.resourceHolder = resourceHolder;
        this.abuseContact = abuseContact;
        this.limit = limit;
        this.offset = offset;
    }

    public Parameters() {
        // required no-arg constructor
    }

    public InverseAttributes getInverseLookup() {
        return this.inverseAttributes;
    }

    public TypeFilters getTypeFilters() {
        return typeFilters;
    }

    public Flags getFlags() {
        return this.flags;
    }

    public QueryStrings getQueryStrings() {
        return this.queryStrings;
    }

    public Sources getSources() {
        return sources;
    }

    public AbusePKey getPrimaryKey() {
        return primaryKey;
    }

    public static class Builder {

        private InverseAttributes inverseAttributes;
        private TypeFilters typeFilters;
        private Flags flags;
        private QueryStrings queryStrings;
        private Sources sources;
        private AbusePKey primaryKey;
        private Boolean managedAttributes;
        private Boolean resourceHolder;
        private Boolean abuseContact;
        private Integer limit;
        private Integer offset;

        public Builder inverseAttributes(InverseAttributes inverseAttributes) {
            this.inverseAttributes = inverseAttributes;
            return this;
        }

        public Builder typeFilters(TypeFilters typeFilters) {
            this.typeFilters = typeFilters;
            return this;
        }

        public Builder flags(Flags flags) {
            this.flags = flags;
            return this;
        }

        public Builder queryStrings(QueryStrings queryStrings) {
            this.queryStrings = queryStrings;
            return this;
        }

        public Builder sources(Sources sources) {
            this.sources = sources;
            return this;
        }

        public Builder primaryKey(AbusePKey primaryKey) {
            this.primaryKey = primaryKey;
            return this;
        }

        public Builder managedAttributes(Boolean managedAttributes) {
            this.managedAttributes = managedAttributes;
            return this;
        }

        public Builder resourceHolder(Boolean resourceHolder) {
            this.resourceHolder = resourceHolder;
            return this;
        }

        public Builder abuseContact(Boolean abuseContact) {
            this.abuseContact = abuseContact;
            return this;
        }

        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

        public Parameters build() {
            return new Parameters(
                    inverseAttributes,
                    typeFilters,
                    flags,
                    queryStrings,
                    sources,
                    primaryKey,
                    managedAttributes,
                    resourceHolder,
                    abuseContact,
                    limit,
                    offset);
        }
    }
}
