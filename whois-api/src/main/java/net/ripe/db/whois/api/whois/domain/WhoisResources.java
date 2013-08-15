package net.ripe.db.whois.api.whois.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.*;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "service",
        "link",
        "parameters",
        "objects",
        "sources",
        "grsSources",
        "geolocationAttributes",
        "versions"
})
@JsonIgnoreProperties({"noNamespaceSchemaLocation"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@XmlRootElement(name = "whois-resources")
public class WhoisResources {

    private static final String XSI_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String WHOIS_RESOURCES_SCHEMA_LOCATION = "http://rest.db.ripe.net/api-doc/whois-resources.xsd";

    protected Parameters parameters;

    @XmlAttribute
    protected String service;
    @XmlAttribute(namespace = XSI_NAMESPACE_URI)
    private String noNamespaceSchemaLocation = WHOIS_RESOURCES_SCHEMA_LOCATION;
    @XmlElement(name = "objects")
    protected WhoisObjects objects;
    @XmlElement(name = "sources")
    private Sources sources;
    @XmlElement(name = "grs-sources")
    private GrsSources grsSources;
    @XmlElement
    private Link link;
    @XmlElement(name = "geolocation-attributes")
    private GeolocationAttributes geolocationAttributes; // TODO [AK] What about this one? are we using getters or field access?
    @XmlElement(name = "versions")
    protected WhoisVersions versions;

    public Link getLink() {
        return link;
    }

    public WhoisResources setLink(Link value) {
        this.link = value;
        return this;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public WhoisResources setParameters(Parameters value) {
        this.parameters = value;
        return this;
    }

    public String getService() {
        return service;
    }

    public WhoisResources setService(String value) {
        this.service = value;
        return this;
    }

    public List<Source> getSources() {
        return sources != null ? sources.sources : null;
    }

    public WhoisResources setSources(List<Source> sources) {
        this.sources = new Sources(sources);
        return this;
    }

    public List<GrsSource> getGrsSources() {
        return grsSources != null ? grsSources.sources : null;
    }

    public WhoisResources setGrsSources(List<GrsSource> grsSources) {
        this.grsSources = new GrsSources(grsSources);
        return this;
    }

    public List<WhoisObject> getWhoisObjects() {
        return objects != null ? objects.whoisObjects : null;
    }

    public WhoisResources setWhoisObjects(List<WhoisObject> value) {
        this.objects = new WhoisObjects(value);
        return this;
    }

    public WhoisResources setGeolocationAttributes(GeolocationAttributes geolocationAttributes) {
        this.geolocationAttributes = geolocationAttributes;
        return this;
    }

    public WhoisVersions getVersions() {
        return versions;
    }

    public WhoisResources setVersions(WhoisVersions versions) {
        this.versions = versions;
        return this;
    }
}
