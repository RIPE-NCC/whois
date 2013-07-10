package net.ripe.db.whois.api.whois.rdap.domain.vcard;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.HashMap;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "parameters",
    "type",
    "value"
})
@XmlRootElement(name = "version")
public class Version
    extends VCardProperty
    implements Serializable
{
    @XmlElement(defaultValue = "version")
    protected String name;
    protected HashMap parameters;
    @XmlElement(defaultValue = "text")
    protected String type;
    @XmlElement(defaultValue = "4.0")
    protected String value;

    public void setName(String value) {
        this.name = value;
    }

    public HashMap getParameters() {
        return parameters;
    }

    public void setParameters(HashMap value) {
        this.parameters = value;
    }

    public void setType(String value) {
        this.type = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        if (null == name) {
            return "version";
        }
        return name;
    }

    public String getType() {
        if (null == type) {
            return "text";
        }
        return type;
    }

    public String getValue() {
        if (null == value) {
            return "4.0";
        }
        return value;
    }
}
