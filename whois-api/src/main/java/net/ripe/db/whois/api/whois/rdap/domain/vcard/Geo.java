package net.ripe.db.whois.api.whois.rdap.domain.vcard;

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
@XmlRootElement(name = "geo")
public class Geo extends VCardProperty implements Serializable {

    @XmlElement(defaultValue = "geo")
    protected String name;
    protected Map parameters;
    @XmlElement(defaultValue = "uri")
    protected String type;
    protected String value;

    public void setName(final String value) {
        this.name = value;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        if (null == name) {
            return "geo";
        }
        return name;
    }

    public String getType() {
        if (null == type) {
            return "uri";
        }
        return type;
    }
}
