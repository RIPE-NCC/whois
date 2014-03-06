package net.ripe.db.whois.api.rest.domain;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
