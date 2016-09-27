package net.ripe.db.whois.api.rest.mapper;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.ReferencedTypeResolver;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import net.ripe.db.whois.common.rpsl.attrs.MntRoutes;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class FormattedServerAttributeMapper implements AttributeMapper {

    private static final AttributeParser.MntRoutesParser MNT_ROUTES_PARSER = new AttributeParser.MntRoutesParser();

    private final ReferencedTypeResolver referencedTypeResolver;
    private final String baseUrl;

    @Autowired
    public FormattedServerAttributeMapper(final ReferencedTypeResolver referencedTypeResolver,
                                        @Value("${api.rest.baseurl}") final String baseUrl) {
        this.referencedTypeResolver = referencedTypeResolver;
        this.baseUrl = baseUrl;
    }

    @Override
    public Collection<RpslAttribute> map(final Attribute attribute) {
        return Collections.singleton(new RpslAttribute(attribute.getName(), getAttributeValue(attribute)));
    }

    @Override
    public Collection<Attribute> map(final RpslAttribute rpslAttribute, final String source) {
        final List<Attribute> result = Lists.newArrayList();
        for (CIString value : rpslAttribute.getCleanValues()) {
            // TODO: [AH] for each person or role reference returned, we make an sql lookup - baaad
            final String referencedType = (rpslAttribute.getType() != null) ? referencedTypeResolver.getReferencedType(rpslAttribute.getType(), value) : null;
            final Link link = (referencedType != null) ? Link.create(baseUrl, source, referencedType, getLinkValue(rpslAttribute.getType(), value)) : null;
            result.add(new Attribute(rpslAttribute.getKey(), value.toString(), rpslAttribute.getCleanComment(), referencedType, link));
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

    private static String getLinkValue(final AttributeType attributeType, final CIString value) {
        switch (attributeType) {
            case MNT_ROUTES:
                try {
                    final MntRoutes mntRoutes = MNT_ROUTES_PARSER.parse(value.toString());
                    return mntRoutes.getMaintainer().toString();
                } catch (AttributeParseException e) {
                    return value.toString();
                }
            default:
                return value.toString();
        }
    }
}
