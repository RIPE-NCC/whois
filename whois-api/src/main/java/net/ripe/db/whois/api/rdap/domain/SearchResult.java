package net.ripe.db.whois.api.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entity", propOrder = {
        "entityResults",
        "domainResults",
        "ipSearchResults",
        "autnumSearchResults"
})
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResult extends RdapObject implements Serializable {

    @XmlElement(name = "entitySearchResults")
    protected List<Entity> entityResults;

    @XmlElement(name = "domainSearchResults")
    protected List<Domain> domainResults;

    @XmlElement(name = "ipSearchResults")
    protected List<Ip> ipResults;

    @XmlElement(name = "autnumSearchResults")
    protected List<Autnum> autnumResults;

    public SearchResult initialiseEmpty(){
        this.entityResults = Lists.newArrayList();
        this.domainResults = Lists.newArrayList();
        this.ipResults = Lists.newArrayList();
        this.autnumResults = Lists.newArrayList();
        return this;
    }

    public List<Entity> getEntitySearchResults() {
        return entityResults;
    }

    public List<Domain> getDomainSearchResults() {
        return domainResults;
    }

    public List<Ip> getIpSearchResults() {
        return ipResults;
    }

    public List<Autnum> getAutnumSearchResults() {
        return autnumResults;
    }

    public void addEntitySearchResult(final Entity entity) {
        if (entityResults == null) {
            entityResults = Lists.newArrayList();
        }
        entityResults.add(entity);
    }

    public void addDomainSearchResult(final Domain domain) {
        if (domainResults == null) {
            domainResults = Lists.newArrayList();
        }
        domainResults.add(domain);
    }

    public void addIpSearchResult(final Ip ip) {
        if (ipResults == null) {
            ipResults = Lists.newArrayList();
        }
        ipResults.add(ip);
    }

    public void addAutnumSearchResult(final Autnum autnum) {
        if (autnumResults == null) {
            autnumResults = Lists.newArrayList();
        }
        autnumResults.add(autnum);
    }
}
