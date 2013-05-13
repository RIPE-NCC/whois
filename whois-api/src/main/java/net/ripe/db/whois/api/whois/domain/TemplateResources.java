package net.ripe.db.whois.api.whois.domain;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "template-resources")
@XmlAccessorType(XmlAccessType.FIELD)
public class TemplateResources {

    @XmlAttribute
    protected String service;
    @XmlElement
    protected Link link;
    @XmlElement
    protected Templates templates;

    public Link getLink() {
        return link;
    }

    public TemplateResources setLink(Link value) {
        this.link = value;
        return this;
    }

    public String getService() {
        return service;
    }

    public TemplateResources setService(String value) {
        this.service = value;
        return this;
    }

    public List<Template> getTemplates() {
        return templates != null ? templates.templates : null;
    }

    public TemplateResources setTemplates(List<Template> templates) {
        this.templates = new Templates(templates);
        return this;
    }
}
