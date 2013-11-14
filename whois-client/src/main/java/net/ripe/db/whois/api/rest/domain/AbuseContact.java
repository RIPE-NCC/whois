package net.ripe.db.whois.api.rest.domain;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "abuse-contact")
public class AbuseContact {
    @XmlAttribute(name = "email")
    private String email;

    public AbuseContact(final String email) {
        this.email = email;
    }

    public AbuseContact() {
        // required no-arg constructor
    }

    public String getEmail() {
        return email;
    }
}
