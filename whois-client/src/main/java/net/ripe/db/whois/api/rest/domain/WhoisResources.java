package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@SuppressWarnings("UnusedDeclaration")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "link",
        "service",
        "parameters",
        "objects",
        "referencing",
        "referencedBy",
        "sources",
        "errorMessages",
        "geolocationAttributes",
        "versions",
        "versionsInternal",
        "termsAndConditions"
})
@JsonInclude(NON_EMPTY)
@XmlRootElement(name = "whois-resources")
public class WhoisResources {
    public static final String TERMS_AND_CONDITIONS = "http://www.ripe.net/db/support/db-terms-conditions.pdf";

    private Parameters parameters;

    @XmlElement
    private Service service;
    @XmlElement(name = "objects", required = true)
    @JsonProperty(value = "objects", required = true)
    private WhoisObjects objects;
    @XmlElement(name = "referenced-by")
    @JsonProperty(value = "referenced-by")
    private WhoisObjects referencedBy;
    @XmlElement(name = "referencing")
    @JsonProperty(value = "referencing")
    private WhoisObjects referencing;
    @XmlElement(name = "sources")
    private Sources sources;
    @XmlElement
    private Link link;
    @XmlElement(name = "geolocation-attributes")
    private GeolocationAttributes geolocationAttributes;
    @XmlElement(name = "errormessages")
    private ErrorMessages errorMessages;
    @XmlElement(name = "versions")
    private WhoisVersions versions;
    @XmlElement(name = "versionsInternal")
    private WhoisVersionsInternal versionsInternal;
    @XmlElement(name = "terms-and-conditions")
    private Link termsAndConditions;

    public Link getLink() {
        return link;
    }

    public WhoisResources setLink(final Link value) {
        this.link = value;
        return this;
    }

    public void setErrorMessages(final List<ErrorMessage> errorMessages) {
        if (errorMessages.size() > 1) {
            Collections.sort(errorMessages);
        }
        this.errorMessages = new ErrorMessages(errorMessages);
    }

    public List<ErrorMessage> getErrorMessages() {
        return errorMessages != null ? errorMessages.getErrorMessages() : Collections.<ErrorMessage>emptyList();
    }

    public Parameters getParameters() {
        return parameters;
    }

    public WhoisResources setParameters(final Parameters value) {
        this.parameters = value;
        return this;
    }

    public Service getService() {
        return service;
    }

    public WhoisResources setService(final Service value) {
        this.service = value;
        return this;
    }

    public List<Source> getSources() {
        return sources != null ? sources.getSources() : Collections.<Source>emptyList();
    }

    public WhoisResources setSources(final List<Source> sources) {
        this.sources = new Sources(sources);
        return this;
    }

    public List<WhoisObject> getWhoisObjects() {
        return objects != null ? objects.getWhoisObjects() : Collections.<WhoisObject>emptyList();
    }

    public WhoisResources setWhoisObjects(final List<WhoisObject> value) {
        this.objects = new WhoisObjects(value);
        return this;
    }

    public WhoisObjects getReferencedBy() {
        return referencedBy;
    }

    public void setReferencedBy(final WhoisObjects referencedBy) {
        this.referencedBy = referencedBy;
    }

    public WhoisObjects getReferencing() {
        return referencing;
    }

    public void setReferencing(final WhoisObjects referencing) {
        this.referencing = referencing;
    }

    public WhoisResources setGeolocationAttributes(final GeolocationAttributes geolocationAttributes) {
        this.geolocationAttributes = geolocationAttributes;
        return this;
    }

    public WhoisVersions getVersions() {
        return versions;
    }

    public WhoisVersionsInternal getVersionsInternal() {
        return versionsInternal;
    }

    public WhoisResources setVersions(final WhoisVersions versions) {
        this.versions = versions;
        return this;
    }

    public WhoisResources setVersions(final WhoisVersionsInternal versions) {
        this.versionsInternal = versions;
        return this;
    }

    public Link getTermsAndConditions() {
        return termsAndConditions;
    }

    public WhoisResources includeTermsAndConditions() {
        this.termsAndConditions = new Link("locator", TERMS_AND_CONDITIONS);
        return this;
    }
}
