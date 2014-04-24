package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Component
public class DirtyClientAttributeMapper implements AttributeMapper {

    @Override
    public Collection<RpslAttribute> map(Attribute attribute) {
        return Collections.singleton(new RpslAttribute(attribute.getName(), getAttributeValue(attribute)));
    }

    @Override
    public Collection<Attribute> map(RpslAttribute rpslAttribute, String source) {
        return Collections.singleton(new Attribute(rpslAttribute.getKey(), rpslAttribute.getValue(), null, null, null));
    }

    private static String getAttributeValue(final Attribute attribute) {
        if (StringUtils.isBlank(attribute.getComment())) {
            return attribute.getValue();
        } else {
            return String.format("%s # %s", attribute.getValue(), attribute.getComment());
        }
    }
}
