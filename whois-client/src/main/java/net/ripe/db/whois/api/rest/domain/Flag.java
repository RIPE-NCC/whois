package net.ripe.db.whois.api.rest.domain;

import net.ripe.db.whois.query.QueryFlag;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.*;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "flag")
public class Flag {

    @XmlAttribute(name = "value", required = true)
    protected String value;

    public Flag(final QueryFlag value) {
        this.value = value.getName();
    }

    public Flag() {
        // required no-arg constructor
    }

    public String getValue() {
        return value;
    }
}
