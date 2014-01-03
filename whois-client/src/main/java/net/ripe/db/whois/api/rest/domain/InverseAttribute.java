package net.ripe.db.whois.api.rest.domain;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.*;

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
