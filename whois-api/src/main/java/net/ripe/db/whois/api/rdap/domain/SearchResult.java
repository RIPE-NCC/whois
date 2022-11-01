package net.ripe.db.whois.api.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entity", propOrder = {
        "entityResults",
        "domainResults",
        "autnumResults",
        "networkResults"
})
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SearchResult extends RdapObject implements Serializable {

    @XmlElement(name = "entitySearchResults")
    protected List<Entity> entityResults;

    @XmlElement(name = "domainSearchResults")
    protected List<Domain> domainResults;

    @XmlElement(name = "autnumSearchResults")
    protected List<Autnum> autnumResults;

    @XmlElement(name = "networkSearchResults")
    protected List<Ip> networkResults;

    public List<Entity> getEntitySearchResults() {
        return entityResults;
    }

    public List<Domain> getDomainSearchResults() {
        return domainResults;
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

    public void addAutnumSeachResult(final Autnum autnum){
        if (autnumResults == null){
            autnumResults = Lists.newArrayList();
        }
        autnumResults.add(autnum);
    }

    public void addNetworkSeachResult(final Ip ip){
        if (networkResults == null){
            networkResults = Lists.newArrayList();
        }
        networkResults.add(ip);
    }
}
