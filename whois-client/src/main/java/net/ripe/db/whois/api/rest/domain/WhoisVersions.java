package net.ripe.db.whois.api.rest.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "type",
        "key",
        "versions"
})
@XmlRootElement(name = "versions")
public class WhoisVersions {
    @XmlElement(name = "version")
    private List<WhoisVersion> versions;
    @XmlAttribute(name = "type")
    private String type;
    @XmlAttribute(name = "key")
    private String key;

    public WhoisVersions(final String type, final String key, final List<WhoisVersion> versions) {
        this.type = type;
        this.key = key;
        this.versions = versions;
    }

    public WhoisVersions() {
        // required no-arg constructor
    }

    public List<WhoisVersion> getVersions() {
        return versions;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }
}
