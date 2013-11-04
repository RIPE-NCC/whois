package net.ripe.db.whois.api.whois.mapper;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.whois.domain.Attribute;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WhoisObjectClientMapper extends AbstractWhoisObjectMapper {

    @Autowired
    public WhoisObjectClientMapper(@Value("${api.rest.baseurl}") final String baseUrl) {
        super(baseUrl);
    }

    List<Attribute> buildAttributes(RpslObject rpslObject, String source) {
        final List<Attribute> attributes = Lists.newArrayList();
        for (RpslAttribute attribute : rpslObject.getAttributes()) {
            final String comment = getComment(attribute);
            for (CIString value : attribute.getCleanValues()) {
                if (value.length() > 0) {
                    attributes.add(createAttributeWithoutLocator(attribute.getKey(), value.toString(), comment));
                }
            }
        }
        return attributes;
    }

    private Attribute createAttributeWithoutLocator(final String name, final String value, final String comment) {
        return createAttribute(name, value, comment, null, null);
    }
}
