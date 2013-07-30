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
    private String path;

    public Update(final String host, final String id, final String path) {
        this.host = host;
        this.id = id;
        this.path = path;
    }

    public Update() {
        //necessary no-arg constructor
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

    @XmlElement(required = false)
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return String.format("Update{host='%s' , id='%s', path='%s'}", host, id, path);
    }
}
