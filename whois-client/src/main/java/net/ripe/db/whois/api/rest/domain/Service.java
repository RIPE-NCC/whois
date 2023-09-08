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
@XmlRootElement(name = "service")
@JsonInclude(NON_EMPTY)
@Immutable
public class Service {

    @XmlAttribute
    @JsonProperty
    private String name;

    public Service(final String name) {
        this.name = name;
    }

    public Service() {
        // required no-arg constructor
    }

    public String getName() {
        return name;
    }
}
