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
@XmlRootElement(name = "n")
public class N
    extends VCardProperty
    implements Serializable
{

    @XmlElement(defaultValue = "n")
    protected String name;
    protected HashMap parameters;
    @XmlElement(defaultValue = "text")
    protected String type;
    protected NValueType value = new NValueType();

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

    public NValueType getValue() {
        return value;
    }

    public void setValue(NValueType value) {
        this.value = value;
    }

    public String getName() {
        if (null == name) {
            return "n";
        }
        return name;
    }

    public String getType() {
        if (null == type) {
            return "text";
        }
        return type;
    }

}
