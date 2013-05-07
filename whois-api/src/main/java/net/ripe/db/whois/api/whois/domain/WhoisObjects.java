package net.ripe.db.whois.api.whois.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "objects")
@XmlAccessorType(XmlAccessType.FIELD)
public class WhoisObjects {

    @XmlElement(name = "object")
    protected List<WhoisObject> whoisObjects;

    public WhoisObjects(final List<WhoisObject> whoisObjects) {
        this.whoisObjects = whoisObjects;
    }

    public WhoisObjects() {
        // required no-arg constructor
    }
}
