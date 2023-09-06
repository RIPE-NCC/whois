package net.ripe.db.whois.api.rest.domain;

import com.google.common.collect.Lists;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class Templates {

    @XmlElement(name = "template")
    private List<Template> templates;

    public Templates(final List<Template> templates) {
        this.templates = templates;
    }

    public Templates() {
        this.templates = Lists.newArrayList();
    }

    public List<Template> getTemplates() {
        return this.templates;
    }
}
