package net.ripe.db.whois.api.rest.domain;

import com.google.common.collect.Lists;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

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
        this.versions = Lists.newArrayList();
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
