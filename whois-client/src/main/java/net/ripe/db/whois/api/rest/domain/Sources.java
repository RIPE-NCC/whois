package net.ripe.db.whois.api.rest.domain;


import com.google.common.collect.Lists;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.List;

@XmlRootElement(name = "sources")
@XmlAccessorType(XmlAccessType.FIELD)
public class Sources {

    @XmlElement(name = "source")
    private List<Source> sources;

    public Sources(final List<Source> sources) {
        this.sources = sources;
    }

    public Sources(final Collection<String> sources) {
        this.sources = Lists.newArrayList();
        for (String source : sources) {
            this.sources.add(new Source(source));
        }
    }

    public Sources() {
        this.sources = Lists.newArrayList();
    }

    public List<Source> getSources() {
        return sources;
    }
}
