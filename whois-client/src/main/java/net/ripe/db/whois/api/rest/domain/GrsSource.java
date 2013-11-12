package net.ripe.db.whois.api.rest.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "source")
public class GrsSource {

    @XmlAttribute(required = true)
    protected String name;

    @XmlAttribute(required = true)
    protected String id;

    @XmlAttribute(name = "grs-id", required = true)
    protected String grsId;

    public GrsSource(final String id) {
        this.id = id;
    }

    public GrsSource() {
        // required no-arg constructor
    }

    public String getName() {
        return name;
    }

    public GrsSource setName(String value) {
        this.name = value;
        return this;
    }

    public String getId() {
        return id;
    }

    public GrsSource setId(String value) {
        this.id = value;
        return this;
    }

    public String getGrsId() {
        return grsId;
    }

    public GrsSource setGrsId(String value) {
        this.grsId = value;
        return this;
    }

}
