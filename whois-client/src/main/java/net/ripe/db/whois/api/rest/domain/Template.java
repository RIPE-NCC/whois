package net.ripe.db.whois.api.rest.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "template")
@XmlAccessorType(XmlAccessType.FIELD)
public class Template {

    @XmlAttribute
    private String type;
    @XmlElement
    private Source source;
    @XmlElement
    private TemplateAttributes attributes;

    public String getType() {
        return type;
    }

    public Template setType(String type) {
        this.type = type;
        return this;
    }

    public Source getSource() {
        return source;
    }

    public Template setSource(Source source) {
        this.source = source;
        return this;
    }

    public List<TemplateAttribute> getAttributes() {
        return attributes != null ? attributes.getAttributes() : Collections.<TemplateAttribute>emptyList();
    }

    public Template setAttributes(List<TemplateAttribute> attributes) {
        this.attributes = new TemplateAttributes(attributes);
        return this;
    }
}
