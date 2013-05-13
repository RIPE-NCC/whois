package net.ripe.db.whois.api.whois.domain;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value",
    "link"
})
@XmlRootElement(name = "language")
public class Language {
    @XmlAttribute
    private String value;

    @XmlElement
    private Link link;

    public Language(final String value, final Link link) {
        this.value = value;
        this.link = link;
    }

    public Language() {
        // required no-arg constructor
    }
}
