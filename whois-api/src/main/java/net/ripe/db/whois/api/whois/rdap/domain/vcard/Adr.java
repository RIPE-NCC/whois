package net.ripe.db.whois.api.whois.rdap.domain.vcard;

import net.ripe.db.whois.api.whois.rdap.VcardObject;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "name",
        "parameters",
        "type",
        "value"
})
@XmlRootElement(name = "adr")
public class Adr extends VcardObject implements Serializable {

    @XmlElement(defaultValue = "adr")
    protected String name;
    protected Map parameters;
    @XmlElement(defaultValue = "text")
    protected String type;
    protected AdrEntryValueType value = new AdrEntryValueType();

    public void setName(final String value) {
        this.name = value;
    }

    public Map getParameters() {
        return parameters;
    }

    public void setParameters(final Map value) {
        this.parameters = value;
    }

    public void setType(final String value) {
        this.type = value;
    }

    public AdrEntryValueType getValue() {
        return value;
    }

    public void setValue(final AdrEntryValueType value) {
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
