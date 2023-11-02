package net.ripe.db.whois.api.rest.domain;

import com.google.common.collect.Lists;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
@XmlRootElement(name = "attributes")
@XmlAccessorType(XmlAccessType.FIELD)
public class TemplateAttributes {

    @XmlElement(name = "attribute")
    private List<TemplateAttribute> attributes;

    public TemplateAttributes(final List<TemplateAttribute> attributes) {
        this.attributes = attributes;
    }

    public TemplateAttributes() {
        this.attributes = Lists.newArrayList();
    }

    public List<TemplateAttribute> getAttributes() {
        return this.attributes;
    }
}
