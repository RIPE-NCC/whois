package net.ripe.db.whois.api.rest.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "grs-sources")
@XmlAccessorType(XmlAccessType.FIELD)
public class GrsSources {

    @XmlElement(name = "source")
    private List<GrsSource> sources;

    public GrsSources(final List<GrsSource> sources) {
        this.sources = sources;
    }

    public GrsSources() {
        // required no-arg constructor
    }

    public List<GrsSource> getSources() {
        return sources;
    }
}
