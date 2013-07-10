package net.ripe.db.whois.api.whois.rdap.domain.vcard;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "properties"
})
@XmlRootElement(name = "vcard")
public class VCard implements Serializable {
    @XmlElement(defaultValue = "vcard")
    protected String name;
    protected List<VCardProperty> properties;

    public VCard(final String name, final List<VCardProperty> properties) {
        this.name = name;
        this.properties = properties;
    }

    public VCard() {
        // required no-arg constructor
    }

    public List<VCardProperty> getProperties() {
        return properties;
    }

    public String getName() {
        return name;
    }
}
