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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Component
public class DirtyServerAttributeMapper implements AttributeMapper {
    private final ReferencedTypeResolver referencedTypeResolver;
    private final String baseUrl;

    @Autowired
    public DirtyServerAttributeMapper(final ReferencedTypeResolver referencedTypeResolver, @Value("${api.rest.baseurl}") final String baseUrl) {
        this.referencedTypeResolver = referencedTypeResolver;
        this.baseUrl = baseUrl;
    }

    @Override
    public Collection<RpslAttribute> map(Attribute attribute) {
        return Collections.singleton(new RpslAttribute(attribute.getName(), getAttributeValue(attribute)));
    }

    @Override
    public Collection<Attribute> map(RpslAttribute rpslAttribute, String source) {
        final Set<CIString> cleanValues = rpslAttribute.getCleanValues();

        if (cleanValues.size() == 1) {
            // TODO: [AH] for each person or role reference returned, we make an sql lookup - baaad
            final CIString cleanValue = cleanValues.iterator().next();
            final String referencedType = (rpslAttribute.getType() != null) ? referencedTypeResolver.getReferencedType(rpslAttribute.getType(), cleanValue) : null;
            final Link link = (referencedType != null) ? createLink(source, referencedType, cleanValue.toString()) : null;
            return Collections.singleton(new Attribute(rpslAttribute.getKey(), rpslAttribute.getFormattedValue(), null, referencedType, link));
        } else {
            return Collections.singleton(new Attribute(rpslAttribute.getKey(), rpslAttribute.getFormattedValue(), null, null, null));
        }
    }

    protected Link createLink(final String source, final String type, final String key) {
        return new Link("locator", String.format("%s/%s/%s/%s", baseUrl, source, type, key));
    }

    // TODO: duplicate method
    private static String getAttributeValue(final Attribute attribute) {
        if (StringUtils.isBlank(attribute.getComment())) {
            return attribute.getValue();
        } else {
            return String.format("%s # %s", attribute.getValue(), attribute.getComment());
        }
    }
}
