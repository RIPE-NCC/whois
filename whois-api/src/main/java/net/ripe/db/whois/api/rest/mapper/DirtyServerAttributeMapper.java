package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.ReferencedTypeResolver;
import net.ripe.db.whois.api.rest.SourceResolver;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Component
public class DirtyServerAttributeMapper implements AttributeMapper {

    private final ReferencedTypeResolver referencedTypeResolver;
    private final SourceResolver sourceResolver;
    private final String baseUrl;

    @Autowired
    public DirtyServerAttributeMapper(final ReferencedTypeResolver referencedTypeResolver,
                                      final SourceResolver sourceResolver,
                                      @Value("${api.rest.baseurl}") final String baseUrl) {
        this.referencedTypeResolver = referencedTypeResolver;
        this.sourceResolver = sourceResolver;
        this.baseUrl = baseUrl;
    }

    @Override
    public Collection<Attribute> map(final RpslAttribute rpslAttribute, final String source) {
        final Set<CIString> cleanValues = rpslAttribute.getCleanValues();

        if (cleanValues.size() == 1) {
            // TODO: [AH] for each person or role reference returned, we make an sql lookup - baaad
            final CIString cleanValue = cleanValues.iterator().next();
            final AttributeType attributeType = rpslAttribute.getType();
            final String referencedType = (attributeType != null) ? referencedTypeResolver.getReferencedType(attributeType, cleanValue) : null;

            final Link link = (referencedType != null) ?
                    Link.create(baseUrl, sourceResolver.getSource(referencedType, cleanValue, source), referencedType, cleanValue.toString()) : null;

            return Collections.singleton(new Attribute(rpslAttribute.getKey(), rpslAttribute.getFormattedValue(), null, referencedType, link, null));
        } else {
            return Collections.singleton(new Attribute(rpslAttribute.getKey(), rpslAttribute.getFormattedValue(), null, null, null, null));
        }
    }

}
