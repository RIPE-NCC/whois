package net.ripe.db.whois.api.whois.domain;

import javax.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "type-filter")
public class TypeFilter {

    @XmlAttribute(name = "id", required = true)
    protected String id;

    public TypeFilter(final String id) {
        this.id = id;
    }

    public TypeFilter() {
        // required no-arg constructor
    }

    public String getId() {
        return id;
    }
}
