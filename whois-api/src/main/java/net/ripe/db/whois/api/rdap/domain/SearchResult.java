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

    public void setEntityResults(List<Entity> entityResults) {
        this.entityResults = entityResults;
    }

    public void setDomainResults(List<Domain> domainResults) {
        this.domainResults = domainResults;
    }

    public void setIpResults(List<Ip> ipResults) {
        this.ipResults = ipResults;
    }

    public void setAutnumResults(List<Autnum> autnumResults) {
        this.autnumResults = autnumResults;
    }
}
