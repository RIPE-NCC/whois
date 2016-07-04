package net.ripe.db.whois.api.rest.mapper;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class FormattedClientAttributeMapper implements AttributeMapper {

    @Override
    public Collection<RpslAttribute> map(final Attribute attribute) {
        return Collections.singleton(new RpslAttribute(attribute.getName(), getAttributeValue(attribute)));
    }

    @Override
    public Collection<Attribute> map(final RpslAttribute rpslAttribute, final String source) {
        List<Attribute> result = Lists.newArrayList();
        for (CIString value : rpslAttribute.getCleanValues()) {
            result.add(new Attribute(rpslAttribute.getKey(), value.toString(), rpslAttribute.getCleanComment(), null, null));
        }
        return result;
    }

    // TODO: duplicate method
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
