package net.ripe.db.whois.api.whois.rdap.domain.vcard;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "parameters",
    "type",
    "value"
})
@XmlRootElement(name = "categories")
public class Categories
    extends VCardProperty
    implements Serializable
{

    @XmlElement(defaultValue = "categories")
    protected String name;
    protected HashMap parameters;
    @XmlElement(defaultValue = "text")
    protected String type;
    protected List<String> value;

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

    public List<String> getValue() {
        if (value == null) {
            value = Lists.newArrayList();
        }
        return this.value;
    }

    public String getName() {
        if (null == name) {
            return "categories";
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
