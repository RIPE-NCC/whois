package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class FormattedClientAttributeMapper implements AttributeMapper {

    @Override
    public Collection<RpslAttribute> map(Attribute attribute) {
        return Collections.singleton(new RpslAttribute(attribute.getName(), getAttributeValue(attribute)));
    }

    @Override
    public Collection<Attribute> map(RpslAttribute rpslAttribute, String source) {
        List<Attribute> result = new ArrayList(4);
        for (CIString value : rpslAttribute.getCleanValues()) {
            result.add(new Attribute(rpslAttribute.getKey(), value.toString(), rpslAttribute.getCleanComment(), null, null));
        }
        return result;
    }

    private static String getAttributeValue(final Attribute attribute) {
        if (StringUtils.isBlank(attribute.getComment())) {
            return attribute.getValue();
        } else {
            if (attribute.getValue().indexOf('#') >= 0) {
                throw new IllegalArgumentException("Value cannot have a comment in " + attribute);
            } else {
                return String.format("%s # %s", attribute.getValue(), attribute.getComment());
            }
        }
    }
}
