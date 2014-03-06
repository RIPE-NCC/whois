package net.ripe.db.whois.api.rest.domain;

import com.google.common.collect.Lists;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Immutable
@XmlRootElement(name = "inverse-lookup")
@XmlAccessorType(XmlAccessType.FIELD)
public class InverseAttributes {

    @XmlElement(name = "inverse-attribute")
    private List<InverseAttribute> attributes = Lists.newArrayList();

    public InverseAttributes(final Collection<String> attributes) {
        for (String attribute : attributes) {
            this.attributes.add(new InverseAttribute(attribute));
        }
    }

    public InverseAttributes(final List<InverseAttribute> attributes) {
        this.attributes = attributes;
    }

    public InverseAttributes() {
        // required no-arg constructor
    }

    public List<InverseAttribute> getInverseAttributes() {
        return Collections.unmodifiableList(attributes);
    }
}
