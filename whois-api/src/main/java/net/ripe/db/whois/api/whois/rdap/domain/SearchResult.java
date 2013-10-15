package net.ripe.db.whois.api.whois.rdap.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entity", propOrder = {
        "searchResults"
})
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class SearchResult extends RdapObject implements Serializable {

    @XmlElement(name = "entitySearchResults")
    protected List<Entity> searchResults;

    public List<Entity> getSearchResults() {
        if (searchResults == null) {
            searchResults = Lists.newArrayList();
        }
        return searchResults;
    }

    public void setSearchResults(final List<Entity> searchResults) {
        this.searchResults = searchResults;
    }

    public void addSearchResult(final Entity entity) {
        getSearchResults().add(entity);
    }
}
