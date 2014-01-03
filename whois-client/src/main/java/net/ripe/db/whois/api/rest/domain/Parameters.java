package net.ripe.db.whois.api.rest.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "inverseAttributes",
    "typeFilters",
    "flags",
    "queryStrings",
    "primaryKey",
    "sources"
})
@XmlRootElement(name = "parameters")
public class Parameters {
    @XmlElement(name = "inverse-lookup")
    private InverseAttributes inverseAttributes;
    @XmlElement(name="type-filters")
    private TypeFilters typeFilters;
    @XmlElement(name = "flags")
    private Flags flags;
    @XmlElement(name = "query-strings", required = true)
    private QueryStrings queryStrings;
    @XmlElement(name = "sources", required = true)
    private Sources sources;
    @XmlElement(name = "primary-key")
    private AbusePKey primaryKey;

    public Parameters(final InverseAttributes inverseAttributes, final TypeFilters typeFilters, final Flags flags, final QueryStrings queryStrings, final Sources sources, final AbusePKey primaryKey) {
        this.inverseAttributes = inverseAttributes;
        this.typeFilters = typeFilters;
        this.flags = flags;
        this.queryStrings = queryStrings;
        this.sources = sources;
        this.primaryKey = primaryKey;
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
}
