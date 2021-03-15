package net.ripe.db.whois.api.rest.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
    @XmlJavaTypeAdapter(value = RequirementAdapter.class)
    private Requirement requirement;
    @XmlAttribute(required = true)
    private Cardinality cardinality;
    @XmlAttribute
    private Set<Key> keys = Collections.emptySet();

    public String getName() {
        return name;
    }

    public TemplateAttribute setName(final String name) {
        this.name = name;
        return this;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public TemplateAttribute setRequirement(final Requirement requirement) {
        this.requirement = requirement;
        return this;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public TemplateAttribute setCardinality(final Cardinality cardinality) {
        this.cardinality = cardinality;
        return this;
    }

    public Set<Key> getKeys() {
        return keys;
    }

    public TemplateAttribute setKeys(final Set<Key> keys) {
        this.keys = keys;
        return this;
    }

    public static class RequirementAdapter extends XmlAdapter<String, Requirement> {
        @Override
        public Requirement unmarshal(String v) throws Exception {
            return Requirement.valueOf(v);
        }

        @Override
        public String marshal(Requirement v) throws Exception {
            return v.getExternalName().toUpperCase();
        }
    }
}
