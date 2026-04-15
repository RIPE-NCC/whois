package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.apache.commons.lang3.StringUtils;

public interface FormattedAttributeMapper extends AttributeMapper {

    @Override
    default String getAttributeValue(final Attribute attribute) {
        if (StringUtils.isBlank(attribute.getComment())) {
            return attribute.getValue();
        } else {
            //Contact attribute can contain #
            if (attribute.getName().equals(AttributeType.CONTACT.getName())){
                return String.format("%s # %s", attribute.getValue(), attribute.getComment());
            }

            if (attribute.getValue().indexOf('#') >= 0) {
                throw new IllegalArgumentException("Value cannot have a comment in " + attribute);
            } else {
                return String.format("%s # %s", attribute.getValue(), attribute.getComment());
            }
        }
    }
}
