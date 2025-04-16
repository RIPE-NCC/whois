package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.domain.Attribute;
import org.apache.commons.lang3.StringUtils;

public interface FormattedAttributeMapper extends AttributeMapper {

    @Override
    default String getAttributeValue(final Attribute attribute) {
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
