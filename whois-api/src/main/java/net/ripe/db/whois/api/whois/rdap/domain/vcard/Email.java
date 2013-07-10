package net.ripe.db.whois.api.whois.rdap.domain.vcard;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "name",
        "parameters",
        "type",
        "value"
})
@XmlRootElement
public class Email extends VCardProperty implements Serializable {
    @XmlElement
    protected String name;
    @XmlElement
    protected Map parameters;
    @XmlElement
    protected String type;
    @XmlElement
    protected String value;

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setName(final String value) {
        this.name = value;
    }

    public String getName() {
        return name;
    }

    public Map getParameters() {
        return parameters;
    }

    public void setParameters(final Map value) {
        this.parameters = value;
    }

    public void setType(String value) {
        this.type = value;
    }

    public String getType() {
        return type;
    }
}
