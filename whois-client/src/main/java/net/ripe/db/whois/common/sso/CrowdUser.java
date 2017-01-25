package net.ripe.db.whois.common.sso;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user")
@XmlAccessorType(XmlAccessType.FIELD)
public class CrowdUser {
    @XmlAttribute(name = "name")
    private String name;
    @XmlElement(name = "display-name")
    private String displayName;
    @XmlElement(name = "active")
    private Boolean active;

    public CrowdUser() {
        // required no-arg constructor
    }

    public CrowdUser(final String name, final String displayName, final Boolean active) {
        this.name = name;
        this.displayName = displayName;
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }
}
