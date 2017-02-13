package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.ReferencedTypeResolver;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
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
    public DirtyServerAttributeMapper(final ReferencedTypeResolver referencedTypeResolver,
                                    @Value("${api.rest.baseurl}") final String baseUrl) {
        this.referencedTypeResolver = referencedTypeResolver;
        this.baseUrl = baseUrl;
    }

    @Override
    public Collection<Attribute> map(final RpslAttribute rpslAttribute, final String source) {
        final Set<CIString> cleanValues = rpslAttribute.getCleanValues();

        if (cleanValues.size() == 1) {
            // TODO: [AH] for each person or role reference returned, we make an sql lookup - baaad
            final CIString cleanValue = cleanValues.iterator().next();
            final AttributeType rpsAttributeType = rpslAttribute.getType();
            final String referencedType = (rpsAttributeType != null) ? referencedTypeResolver.getReferencedType(rpsAttributeType, cleanValue) : null;
            final Link link = (referencedType != null) ? Link.create(baseUrl, source, referencedType, cleanValue.toString()) : null;
            return Collections.singleton(new Attribute(rpslAttribute.getKey(), rpslAttribute.getFormattedValue(), null, referencedType, link));
        } else {
            return Collections.singleton(new Attribute(rpslAttribute.getKey(), rpslAttribute.getFormattedValue(), null, null, null));
        }
    }

}
