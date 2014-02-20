package net.ripe.db.whois.api.rest.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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

    public TemplateResources setService(Service value) {
        this.service = value;
        return this;
    }

    public List<Template> getTemplates() {
        return templates != null ? templates.getTemplates() : Collections.<Template>emptyList();
    }

    public TemplateResources setTemplates(List<Template> templates) {
        this.templates = new Templates(templates);
        return this;
    }
}
