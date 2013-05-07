package net.ripe.db.whois.api.whois.domain;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "flag")
public class Flag {

    @XmlAttribute(name = "value", required = true)
    protected String value;

    public Flag(final String value) {
        this.value = value;
    }

    public Flag() {
        // required no-arg constructor
    }

    public String getValue() {
        return value;
    }
}
