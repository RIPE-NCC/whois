package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.annotation.concurrent.Immutable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Immutable
@SuppressWarnings("UnusedDeclaration")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "service",
        "link",
        "parameters",
        "abuseContact",
        "message",
        "termsAndConditions"
})
@JsonInclude(NON_EMPTY)
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
    @XmlElement(name = "message")
    private String message;
    @XmlElement(name = "terms-and-conditions")
    private Link termsAndConditions;

    public AbuseResources(final String service, final Link link, final Parameters parameters, final AbuseContact abuseContact, final Link termsAndConditions) {
        this.service = service;
        this.link = link;
        this.parameters = parameters;
        this.abuseContact = abuseContact;
        this.termsAndConditions = termsAndConditions;
    }

    public AbuseResources(final String message) {
        this.message = message;
    }

    public AbuseResources() {
        // required no-arg constructor
    }

    public String getService() {
        return service;
    }
    public Link getLink() {
        return link;
    }
    public Parameters getParameters() {
        return parameters;
    }
    public AbuseContact getAbuseContact() {
        return abuseContact;
    }
    public String getMessage() {
        return message;
    }
    public Link getTermsAndConditions() {
        return termsAndConditions;
    }
}
