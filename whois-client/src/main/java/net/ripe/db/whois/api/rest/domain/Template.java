package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@XmlRootElement(name = "template")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(NON_EMPTY)
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

    public Template setType(final String type) {
        this.type = type;
        return this;
    }

    public Source getSource() {
        return source;
    }

    public Template setSource(final Source source) {
        this.source = source;
        return this;
    }

    public List<TemplateAttribute> getAttributes() {
        return attributes != null ? attributes.getAttributes() : Collections.<TemplateAttribute>emptyList();
    }

    public Template setAttributes(final List<TemplateAttribute> attributes) {
        this.attributes = new TemplateAttributes(attributes);
        return this;
    }
}
