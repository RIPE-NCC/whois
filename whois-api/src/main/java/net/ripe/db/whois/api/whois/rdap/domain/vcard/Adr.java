package net.ripe.db.whois.api.whois.rdap.domain.vcard;

import net.ripe.db.whois.api.whois.rdap.VcardObject;

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
@XmlRootElement(name = "adr")
public class Adr
    extends VcardObject
    implements Serializable
{

    @XmlElement(defaultValue = "adr")
    protected String name;
    protected HashMap parameters;
    @XmlElement(defaultValue = "text")
    protected String type;
    protected AdrEntryValueType value = new AdrEntryValueType();

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

    public AdrEntryValueType getValue() {
        return value;
    }

    public void setValue(AdrEntryValueType value) {
        this.value = value;
    }

    public String getName() {
        if (null == name) {
            return "adr";
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
