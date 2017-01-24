package net.ripe.db.whois.common.sso;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CrowdValue {
    @XmlElement(name = "value")
    private String value;

    public CrowdValue() {
        // required no-arg constructor
    }

    public CrowdValue(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
