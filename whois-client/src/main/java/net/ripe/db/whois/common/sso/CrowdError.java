package net.ripe.db.whois.common.sso;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error")
public class CrowdError {
    @XmlElement(name = "reason")
    private String reason;
    @XmlElement(name = "message")
    private String message;

    public CrowdError() {
        // required no-arg constructor
    }

    public CrowdError(final String reason, final String message) {
        this.reason = reason;
        this.message = message;
    }

    public String getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }
}
