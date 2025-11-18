package net.ripe.db.whois.rdap.ipranges.administrative;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Rdap {
    @XmlElement(name = "server", namespace = "http://www.iana.org/assignments")
    private String server;

    public String getServer() { return server != null ? server.trim() : null; }
    public void setServer(String server) { this.server = server; }
}