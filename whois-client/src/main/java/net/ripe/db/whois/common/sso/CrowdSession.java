package net.ripe.db.whois.common.sso;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "session")
@XmlAccessorType(XmlAccessType.FIELD)
public class CrowdSession {
    @XmlElement(name = "user")
    private CrowdUser user;
    @XmlElement(name = "token")
    private String token;
    @XmlElement(name = "expiry-date")
    private String expiryDate;

    public CrowdSession() {
        // required no-arg constructor
    }

    public CrowdSession(final CrowdUser user, final String token, final String expiryDate) {
        this.user = user;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public CrowdUser getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public String getExpiryDate() {
        return expiryDate;
    }
}
