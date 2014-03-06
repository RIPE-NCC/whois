package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "service")
@JsonInclude(NON_EMPTY)
public class Service {

    @XmlAttribute
    @JsonProperty
    private String name;

    public Service(String name) {
        this.name = name;
    }

    public Service() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
