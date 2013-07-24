package net.ripe.db.whois.wsearch;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Logged update id.
 */
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class Update {
    private String host;
    private String id;

    public Update() {
    }

    public Update(final String host, final String id) {
        this.host = host;
        this.id = id;
    }

    /**
     * host where the update is logged
     */
    @XmlElement(required = true)
    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    /**
     * update id
     */
    @XmlElement(required = true)
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("Update{host='%s' , id='%s'}", host, id);
    }
}
