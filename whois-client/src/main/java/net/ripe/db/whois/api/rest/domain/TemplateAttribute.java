package net.ripe.db.whois.api.rest.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Collections;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Cardinality;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Key;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Requirement;

@XmlAccessorType(XmlAccessType.FIELD)
public class TemplateAttribute {

    @XmlAttribute(required = true)
    private String name;
    @XmlAttribute(required = true)
    private Requirement requirement;
    @XmlAttribute(required = true)
    private Cardinality cardinality;
    @XmlAttribute
    private Set<Key> keys = Collections.emptySet();

    public String getName() {
        return name;
    }

    public TemplateAttribute setName(String name) {
        this.name = name;
        return this;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public TemplateAttribute setRequirement(Requirement requirement) {
        this.requirement = requirement;
        return this;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public TemplateAttribute setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
        return this;
    }

    public Set<Key> getKeys() {
        return keys;
    }

    public TemplateAttribute setKeys(Set<Key> keys) {
        this.keys = keys;
        return this;
    }
}
