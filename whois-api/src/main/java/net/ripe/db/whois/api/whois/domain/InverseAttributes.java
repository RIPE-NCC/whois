package net.ripe.db.whois.api.whois.domain;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.List;

@XmlRootElement(name = "inverse-lookup")
@XmlAccessorType(XmlAccessType.FIELD)
public class InverseAttributes {

    @XmlElement(name = "inverse-attribute")
    protected List<InverseAttribute> attributes;

    public InverseAttributes(final List<InverseAttribute> attributes) {
        this.attributes = attributes;
    }

    public InverseAttributes(final Collection<String> attributes) {
        this.attributes = Lists.newArrayList();
        for (String attribute : attributes) {
            this.attributes.add(new InverseAttribute(attribute));
        }
    }

    public InverseAttributes() {
        // required no-arg constructor
    }

    public List<InverseAttribute> getInverseAttributes() {
        return attributes;
    }
}
