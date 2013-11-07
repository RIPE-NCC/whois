package net.ripe.db.whois.api.rest.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
@XmlRootElement(name = "attributes")
@XmlAccessorType(XmlAccessType.FIELD)
public class TemplateAttributes {

    @XmlElement(name = "attribute")
    protected List<TemplateAttribute> attributes;

    public TemplateAttributes(final List<TemplateAttribute> attributes) {
        this.attributes = attributes;
    }

    public TemplateAttributes() {
        // required no-arg constructor
    }
}
