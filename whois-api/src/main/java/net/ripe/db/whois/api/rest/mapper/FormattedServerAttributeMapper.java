package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.ReferencedTypeResolver;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class FormattedServerAttributeMapper implements AttributeMapper {
    private final ReferencedTypeResolver referencedTypeResolver;
    private final String baseUrl;

    @Autowired
    public FormattedServerAttributeMapper(final ReferencedTypeResolver referencedTypeResolver, @Value("${api.rest.baseurl}") final String baseUrl) {
        this.referencedTypeResolver = referencedTypeResolver;
        this.baseUrl = baseUrl;
    }

    @Override
    public Collection<RpslAttribute> map(final Attribute attribute) {
        return Collections.singleton(new RpslAttribute(attribute.getName(), getAttributeValue(attribute)));
    }

    @Override
    public Collection<Attribute> map(final RpslAttribute rpslAttribute, final String source) {
        final List<Attribute> result = new ArrayList(4);
        for (CIString value : rpslAttribute.getCleanValues()) {
            // TODO: [AH] for each person or role reference returned, we make an sql lookup - baaad
            final String referencedType = (rpslAttribute.getType() != null) ? referencedTypeResolver.getReferencedType(rpslAttribute.getType(), value) : null;
            final Link link = (referencedType != null) ? createLink(source, referencedType, value.toString()) : null;
            result.add(new Attribute(rpslAttribute.getKey(), value.toString(), rpslAttribute.getCleanComment(), referencedType, link));
        }
        return result;
    }

    protected Link createLink(final String source, final String type, final String key) {
        return new Link("locator", String.format("%s/%s/%s/%s", baseUrl, source, type, key));
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
