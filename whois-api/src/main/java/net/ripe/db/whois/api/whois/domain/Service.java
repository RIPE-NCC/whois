package net.ripe.db.whois.api.whois.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "service")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Service {

    @XmlAttribute
    @JsonProperty
    protected String name;

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
