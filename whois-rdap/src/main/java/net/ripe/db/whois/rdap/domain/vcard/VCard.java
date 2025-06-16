package net.ripe.db.whois.rdap.domain.vcard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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

