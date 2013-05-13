package net.ripe.db.whois.api.whois.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "attributes")
@XmlAccessorType(XmlAccessType.FIELD)
public class Attributes {

    @XmlElement(name = "attribute")
    protected List<Attribute> attributes;

    public Attributes(final List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public Attributes() {
        // required no-arg constructor
    }
}
