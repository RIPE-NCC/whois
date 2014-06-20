package net.ripe.db.whois.api.rest.domain;

import com.google.common.collect.Lists;

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
public class WhoisVersionsInternal {
    @XmlElement(name = "version")
    private List<WhoisVersionInternal> versions;
    @XmlAttribute(name = "type")
    private String type;
    @XmlAttribute(name = "key")
    private String key;

    public WhoisVersionsInternal(final String type, final String key, final List<WhoisVersionInternal> versions) {
        this.type = type;
        this.key = key;
        this.versions = versions;
    }

    public WhoisVersionsInternal() {
        this.versions = Lists.newArrayList();
    }

    public List<WhoisVersionInternal> getVersions() {
        return versions;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }
}
