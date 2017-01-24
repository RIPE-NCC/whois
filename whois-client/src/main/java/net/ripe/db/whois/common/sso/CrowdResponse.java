package net.ripe.db.whois.common.sso;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "attributes")
public class CrowdResponse {

    @XmlElement(name = "attribute")
    private List<CrowdAttribute> attributes;

    public CrowdResponse() {
        // required no-arg constructor
    }

    public CrowdResponse(final List<CrowdAttribute> attributes) {
        this.attributes = attributes;
    }

    public List<CrowdAttribute> getAttributes() {
        return attributes;
    }

    public String getUUID() {
        final CrowdAttribute uuid = Iterables.find(attributes, new Predicate<CrowdAttribute>() {
            @Override
            public boolean apply(final CrowdAttribute input) {
                return input.getName().equals("uuid");
            }
        });

        return uuid.getValues().get(0).getValue();
    }
}
