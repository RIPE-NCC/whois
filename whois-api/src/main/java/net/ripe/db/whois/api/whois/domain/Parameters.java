package net.ripe.db.whois.api.whois.domain;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.List;

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
    protected InverseAttributes inverseAttributes;
    @XmlElement(name="type-filters")
    protected TypeFilters typeFilters;
    @XmlElement(name = "flags")
    protected Flags flags;
    @XmlElement(name = "query-strings", required = true)
    protected QueryStrings queryStrings;
    @XmlElement(name = "sources", required = true)
    protected Sources sources;
    @XmlElement(name = "primary-key")
    protected AbusePKey primaryKey;

    public void setInverseLookup(List<InverseAttribute> values) {
        this.inverseAttributes = new InverseAttributes(values);
    }

    public void setInverseLookup(Collection<String> values) {
        this.inverseAttributes = new InverseAttributes(values);
    }

    public InverseAttributes getInverseLookup() {
        return this.inverseAttributes;
    }

    public void setTypeFilters(List<TypeFilter> values) {
        this.typeFilters = new TypeFilters(values);
    }

    public void setTypeFilters(Collection<String> values) {
        this.typeFilters = new TypeFilters(values);
    }

    public TypeFilters getTypeFilters() {
        return typeFilters;
    }

    public void setFlags(List<Flag> values) {
        this.flags = new Flags(values);
    }

    public void setFlags(Collection<String> flags) {
        this.flags = new Flags(flags);
    }

    public Flags getFlags() {
        return this.flags;
    }

    public void setQueryStrings(List<QueryString> values) {
        this.queryStrings = new QueryStrings(values);
    }

    public void setQueryStrings(String value) {
        this.queryStrings = new QueryStrings(new QueryString(value));
    }

    public QueryStrings getQueryStrings() {
        return this.queryStrings;
    }

    public void setSources(List<Source> values) {
        this.sources = new Sources(values);
    }

    public void setSources(Collection<String> values) {
        this.sources = new Sources(values);
    }

    public Sources getSources() {
        return sources;
    }

    public AbusePKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(AbusePKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
