package net.ripe.db.whois.common.sso;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class CrowdAttribute {
    @XmlElement
    private List<CrowdValue> values;
    @XmlAttribute(name = "name")
    private String name;

    public CrowdAttribute() {
        // required no-arg constructor
    }

    public CrowdAttribute(final List<CrowdValue> values, final String name) {
        this.values = values;
        this.name = name;
    }

    public List<CrowdValue> getValues() {
        return values;
    }

    public String getName() {
        return name;
    }
}
