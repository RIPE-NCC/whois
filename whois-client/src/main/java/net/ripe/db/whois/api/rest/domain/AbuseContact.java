package net.ripe.db.whois.api.rest.domain;

import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "abuse-contact")
public class AbuseContact {

    @XmlAttribute(name = "key")
    private String key;

    @XmlAttribute(name = "email")
    private String email;

    @XmlAttribute(name = "suspect")
    private boolean suspect;

    public AbuseContact(final String key, final String email, final boolean suspect) {
        this.key = key;
        this.email = email;
        this.suspect = suspect;
    }

    public AbuseContact(final CIString key, final CIString email, final boolean suspect) {
        this.key = key == null ? null : key.toString();
        this.email = email == null ? null : email.toString();
        this.suspect = suspect;
    }

    public AbuseContact() {
        // required no-arg constructor
    }

    public String getKey() {
        return key;
    }

    public String getEmail() {
        return email;
    }

    public boolean isSuspect() {
        return suspect;
    }
}
