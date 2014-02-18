package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.ripe.db.whois.api.rest.mapper.Json;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "link",
        "service",
        "parameters",
        "objects",
        "sources",
        "errorMessages",
        "geolocationAttributes",
        "versions",
        "termsAndConditions"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@XmlRootElement(name = "whois-resources")
public class WhoisResources {
    public static final String TERMS_AND_CONDITIONS = "http://www.ripe.net/db/support/db-terms-conditions.pdf";

    private Parameters parameters;

    @XmlElement
    private Service service;
    @XmlElement(name = "objects", required = true)
    @JsonProperty(value = "objects", required = true)
    @JsonDeserialize(using = Json.WhoisObjectsDeserializer.class)
    @JsonSerialize(using = Json.WhoisObjectsSerializer.class)
    private WhoisObjects objects;
    @JsonDeserialize(using = Json.SourcesDeserializer.class)
    @JsonSerialize(using = Json.SourcesSerializer.class)
    @XmlElement(name = "sources")
    private Sources sources = new Sources();
    @XmlElement
    private Link link;
    @XmlElement(name = "geolocation-attributes")
    private GeolocationAttributes geolocationAttributes;
    @JsonDeserialize(using = Json.ErrorMessagesDeserializer.class)
    @JsonSerialize(using = Json.ErrorMessagesSerializer.class)
    @XmlElement(name = "errormessages")
    private ErrorMessages errorMessages = new ErrorMessages();
    @XmlElement(name = "versions")
    private WhoisVersions versions;
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

    public WhoisResources setGeolocationAttributes(final GeolocationAttributes geolocationAttributes) {
        this.geolocationAttributes = geolocationAttributes;
        return this;
    }

    public WhoisVersions getVersions() {
        return versions;
    }

    public WhoisResources setVersions(final WhoisVersions versions) {
        this.versions = versions;
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
