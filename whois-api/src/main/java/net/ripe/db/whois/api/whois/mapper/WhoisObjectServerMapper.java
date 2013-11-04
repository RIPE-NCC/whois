package net.ripe.db.whois.api.whois.mapper;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.whois.ReferencedTypeResolver;
import net.ripe.db.whois.api.whois.domain.Attribute;
import net.ripe.db.whois.api.whois.domain.Link;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WhoisObjectServerMapper extends AbstractWhoisObjectMapper {

    private final ReferencedTypeResolver referencedTypeResolver;

    @Autowired
    public WhoisObjectServerMapper(final ReferencedTypeResolver referencedTypeResolver, @Value("${api.rest.baseurl}") final String baseUrl) {
        super(baseUrl);
        this.referencedTypeResolver = referencedTypeResolver;
    }

    @Override
    List<Attribute> buildAttributes(RpslObject rpslObject, String source) {
        final List<Attribute> attributes = Lists.newArrayList();
        for (RpslAttribute attribute : rpslObject.getAttributes()) {
            final String comment = getComment(attribute);
            for (CIString value : attribute.getCleanValues()) {
                if (value.length() > 0) {
                    // TODO: [AH] for each person or role reference returned, we make an sql lookup - baaad
                    final String referencedType = (attribute.getType() != null && referencedTypeResolver != null) ? referencedTypeResolver.getReferencedType(attribute.getType(), value) : null;
                    final Link link = (referencedType != null) ? createLink(source, referencedType, value.toString()) : null;
                    attributes.add(createAttribute(attribute.getKey(), value.toString(), comment, referencedType, link));
                }
            }
        }
        return attributes;
    }
}
