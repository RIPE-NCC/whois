package net.ripe.db.whois.common.sso;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "authentication-context")
public class CrowdAuthenticationContext {
    @XmlElement(name = "username")
    private String username;
    @XmlElement(name = "password")
    private String password;

    CrowdAuthenticationContext() {
        // required no-arg constructor
    }

    CrowdAuthenticationContext(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
