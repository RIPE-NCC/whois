package net.ripe.db.whois.api.rest.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.concurrent.Immutable;

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

    @XmlAttribute(name = "org-id")
    private String orgId;

    public AbuseContact(final String key, final String email, final boolean suspect, final String orgId) {
        this.key = key;
        this.email = email;
        this.suspect = suspect;
        this.orgId = orgId;
    }

    public AbuseContact(final CIString key, final CIString email, final boolean suspect, final CIString orgId) {
        this.key = key == null ? null : key.toString();
        this.email = email == null ? null : email.toString();
        this.suspect = suspect;
        this.orgId = orgId == null? null : orgId.toString();
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

    public String getOrgId() {
        return orgId;
    }
}
