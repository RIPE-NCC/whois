package net.ripe.db.whois.api.rest.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "abuse-contact")
public class AbuseContact {
    @XmlAttribute(name = "email")
    private String email;

    public String getEmail() {
        return email;
    }

    public AbuseContact setEmail(String email) {
        this.email = email;
        return this;
    }
}
