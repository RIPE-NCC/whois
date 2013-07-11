package net.ripe.db.whois.api.whois.rdap.domain.vcard;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "vcardEntries"
})
@XmlRootElement(name = "vcard")
public class Vcard
    extends net.ripe.db.whois.api.whois.rdap.VcardObject
    implements Serializable
{
    @XmlElement(defaultValue = "vcard")
    protected String name;
    protected List<net.ripe.db.whois.api.whois.rdap.VcardObject> vcardEntries;

    public void setName(String value) {
        this.name = value;
    }

    public List<net.ripe.db.whois.api.whois.rdap.VcardObject> getVcardEntries() {
        if (vcardEntries == null) {
            vcardEntries = new ArrayList<>();
        }
        return this.vcardEntries;
    }

    public String getName() {
        if (null == name) {
            return "vcard";
        }
        return name;
    }
}
