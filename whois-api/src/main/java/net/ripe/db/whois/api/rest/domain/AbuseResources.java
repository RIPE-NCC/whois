package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@SuppressWarnings("UnusedDeclaration")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "service",
        "link",
        "parameters",
        "abuseContact",
        "termsAndConditions"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@XmlRootElement(name = "abuse-resources")
public class AbuseResources {
    @XmlAttribute
    private String service;
    @XmlElement
    private Link link;
    @XmlElement
    private Parameters parameters;
    @XmlElement(name = "abuse-contacts")
    private AbuseContact abuseContact;
    @XmlElement(name = "terms-and-conditions")
    private Link termsAndConditions;

    public String getService() {
        return service;
    }
    public AbuseResources setService(final String service) {
        this.service = service;
        return this;
    }

    public Link getLink() {
        return link;
    }
    public void setLink(Link link) {
        this.link = link;
    }

    public Parameters getParameters() {
        return parameters;
    }
    public AbuseResources setParameters(final Parameters value) {
        this.parameters = value;
        return this;
    }

    public AbuseContact getAbuseContact() {
        return abuseContact;
    }
    public AbuseResources setAbuseContact(final AbuseContact abuseContact) {
        this.abuseContact = abuseContact;
        return this;
    }

    public Link getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(final Link termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }
}
