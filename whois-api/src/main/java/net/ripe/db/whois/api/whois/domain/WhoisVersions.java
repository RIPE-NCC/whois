package net.ripe.db.whois.api.whois.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "source",
        "versions",
        "type",
        "key"
})
@XmlRootElement(name = "versions")
public class WhoisVersions {
    @XmlElement(name = "version")
    protected List<WhoisVersion> versions;
    @XmlElement(name = "source")
    protected Source source;
    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "key")
    protected String key;

    public WhoisVersions(final String source, final String type, final String key, final List<WhoisVersion> versions) {
        this.source = new Source().setId(source);
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

    public Source getSource() {
        return source;
    }
}
