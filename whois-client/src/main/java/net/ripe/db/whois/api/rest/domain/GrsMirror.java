package net.ripe.db.whois.api.rest.domain;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.*;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "grs-mirror")
public class GrsMirror {

    @XmlAttribute(name = "id", required = true)
    private String id;

    public GrsMirror(final String id) {
        this.id = id;
    }

    public GrsMirror() {
        // required no-arg constructor
    }

    public String getId() {
        return id;
    }
}
