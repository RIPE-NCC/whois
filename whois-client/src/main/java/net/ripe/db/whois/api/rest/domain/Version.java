package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import javax.annotation.concurrent.Immutable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "version")
@JsonInclude(NON_EMPTY)
@Immutable
public class Version {

    @XmlAttribute
    @JsonProperty
    private String version;
    @XmlAttribute
    @JsonProperty
    private String timestamp;
    @XmlAttribute(name = "commit-id")
    @JsonProperty(value = "commit-id")
    private String commitId;

    public Version(final String version, final String timestamp, final String commitId) {
        this.version = version;
        this.timestamp = timestamp;
        this.commitId = commitId;
    }

    public Version() {
        // required no-arg constructor
    }

    public String getVersion() {
        return version;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getCommitId() {
        return commitId;
    }
}
