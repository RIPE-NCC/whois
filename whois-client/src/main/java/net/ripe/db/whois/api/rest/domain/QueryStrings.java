package net.ripe.db.whois.api.rest.domain;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "query-strings")
@XmlAccessorType(XmlAccessType.FIELD)
public class QueryStrings {

    @XmlElement(name = "query-string")
    protected List<QueryString> queryStrings;

    public QueryStrings(final List<QueryString> queryStrings) {
        this.queryStrings = queryStrings;
    }

    public QueryStrings(final QueryString queryString) {
        this.queryStrings = Lists.newArrayList(queryString);
    }

    public QueryStrings() {
        // required no-arg constructor
    }

    public List<QueryString> getQueryStrings() {
        return queryStrings;
    }
}
