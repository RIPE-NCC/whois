package net.ripe.db.whois.api.whois.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "tags")
@XmlAccessorType(XmlAccessType.FIELD)
public class WhoisTags {

    @XmlElement(name = "tag")
    protected List<WhoisTag> tags;

    public WhoisTags(final List<WhoisTag> tags) {
        this.tags = tags;
    }

    public WhoisTags() {
        // required no-arg constructor
    }

    public List<WhoisTag> getTags() {
        return tags;
    }
}
