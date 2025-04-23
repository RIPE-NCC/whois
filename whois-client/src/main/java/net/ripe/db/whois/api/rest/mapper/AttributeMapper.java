package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;

public interface AttributeMapper {

    Collection<Attribute> map(RpslAttribute rpslObject, String source);

    default Collection<RpslAttribute> map(Attribute attribute) {
        return Collections.singleton(new RpslAttribute(attribute.getName(), getAttributeValue(attribute)));
    }

    default String getAttributeValue(final Attribute attribute) {
        if (StringUtils.isBlank(attribute.getComment())) {
            return attribute.getValue();
        } else {
            return String.format("%s # %s", attribute.getValue(), attribute.getComment());
        }
    }
}
