package net.ripe.db.whois.api.rest.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "template-resources")
@XmlAccessorType(XmlAccessType.FIELD)
public class TemplateResources {
    @XmlElement
    private Link link;
    @XmlElement
    private Service service;
    @XmlElement
    private Templates templates;

    public Link getLink() {
        return link;
    }

    public TemplateResources setLink(Link value) {
        this.link = value;
        return this;
    }

    public Service getService() {
        return service;
    }

    public TemplateResources setService(final Service value) {
        this.service = value;
        return this;
    }

    public List<Template> getTemplates() {
        return templates != null ? templates.getTemplates() : Collections.<Template>emptyList();
    }

    public TemplateResources setTemplates(final List<Template> templates) {
        this.templates = new Templates(templates);
        return this;
    }
}
