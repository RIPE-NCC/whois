package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

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
