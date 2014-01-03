package net.ripe.db.whois.api.rest.domain;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.*;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "query-string")
public class QueryString {

    @XmlAttribute(name = "value", required = true)
    private String value;

    public QueryString(final String value) {
        this.value = value;
    }

    public QueryString() {
        // required no-arg constructor
    }

    public String getValue() {
        return value;
    }
}
