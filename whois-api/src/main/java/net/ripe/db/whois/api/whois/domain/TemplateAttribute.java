package net.ripe.db.whois.api.whois.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.AttributeTemplate.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class TemplateAttribute {

    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(required = true)
    protected Requirement requirement;
    @XmlAttribute(required = true)
    protected Cardinality cardinality;
    @XmlAttribute
    protected Set<Key> keys;

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

    public TemplateAttribute setKey(Set<Key> keys) {
        this.keys = keys;
        return this;
    }
}
