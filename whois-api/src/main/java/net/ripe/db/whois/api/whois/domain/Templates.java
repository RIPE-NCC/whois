package net.ripe.db.whois.api.whois.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class Templates {

    @XmlElement(name = "template")
    protected List<Template> templates;

    public Templates(final List<Template> templates) {
        this.templates = templates;
    }

    public Templates() {
        // required no-arg constructor
    }
}
