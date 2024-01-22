package net.ripe.db.whois.api.rest.domain;

import com.google.common.collect.Lists;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "query-strings")
@XmlAccessorType(XmlAccessType.FIELD)
public class QueryStrings {

    @XmlElement(name = "query-string")
    private List<QueryString> queryStrings;

    public QueryStrings(final List<QueryString> queryStrings) {
        this.queryStrings = queryStrings;
    }

    public QueryStrings(final QueryString queryString) {
        this.queryStrings = Lists.newArrayList(queryString);
    }

    public QueryStrings() {
        this.queryStrings = Lists.newArrayList();
    }

    public List<QueryString> getQueryStrings() {
        return queryStrings;
    }

}
