package net.ripe.db.whois.api.whois.rdap.domain.vcard;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class VCard {

    @XmlElement(name = "vcard")
    protected List<Object> properties = Lists.newArrayList();

    public VCard(final List<VCardProperty> properties) {
        for (VCardProperty next : properties) {
            this.properties.add(next.getObjects());
        }
    }

    public VCard() {
        // required no-arg constructor
    }

    public List<Object> getValues() {
        return properties;
    }
}

