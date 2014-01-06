package net.ripe.db.whois.api.rest.domain;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "source")
public class GrsSource {

    @XmlAttribute(required = true)
    private String name;

    @XmlAttribute(required = true)
    private String id;

    @XmlAttribute(name = "grs-id", required = true)
    private String grsId;

    public GrsSource(final String name, final String id, final String grsId) {
        this.name = name;
        this.id = id;
        this.grsId = grsId;
    }

    public GrsSource() {
        // required no-arg constructor
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getGrsId() {
        return grsId;
    }
}
