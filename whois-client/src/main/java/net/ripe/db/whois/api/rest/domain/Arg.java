package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "arg")
public class Arg {

    @XmlAttribute(name = "value")
    private String value;

    public Arg(String value) {
        this.value = value;
    }

    public Arg() {
        // required no-arg constructor
    }

    public String getValue() {
        return value;
    }
}
