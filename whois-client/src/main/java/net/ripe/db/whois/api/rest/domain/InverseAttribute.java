package net.ripe.db.whois.api.rest.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.annotation.concurrent.Immutable;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "inverse-attribute")
public class InverseAttribute {

    @XmlAttribute(name = "value", required = true)
    private String value;

    public InverseAttribute(final String value) {
        this.value = value;
    }

    public InverseAttribute() {
        // required no-arg constructor
    }

    public String getValue() {
        return value;
    }
}
