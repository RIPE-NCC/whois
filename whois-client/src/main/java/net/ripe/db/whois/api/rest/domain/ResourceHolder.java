package net.ripe.db.whois.api.rest.domain;

import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.concurrent.Immutable;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@Immutable
@XmlRootElement
public class ResourceHolder {

    @XmlAttribute(name = "key")
    private String orgKey;
    @XmlAttribute(name = "name")
    private String orgName;

    private ResourceHolder() {
        // required no-arg constructor
    }

    public ResourceHolder(final String orgKey, final String orgName) {
        this.orgKey = orgKey;
        this.orgName = orgName;
    }

    public ResourceHolder(final CIString orgKey, final CIString orgName) {
        this.orgKey = orgKey == null ? null : orgKey.toString();
        this.orgName = orgName == null ? null : orgName.toString();
    }

    public String getOrgKey() {
        return orgKey;
    }

    public String getOrgName() {
        return orgName;
    }
}
