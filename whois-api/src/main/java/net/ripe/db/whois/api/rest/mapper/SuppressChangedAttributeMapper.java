package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Component
public abstract class SuppressChangedAttributeMapper implements AttributeMapper {

    private static final CIString CHANGED = CIString.ciString("changed");

    protected abstract Collection<RpslAttribute> mapInternal(final Attribute attribute);

    protected abstract Collection<Attribute> mapInternal(RpslAttribute rpslObject, String source);

    @Override
    public Collection<RpslAttribute> map(final Attribute attribute) {
        if (!hasChanged(attribute)) {
            return mapInternal(attribute);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<Attribute> map(final RpslAttribute rpslAttribute, final String source) {
        if (!hasChanged(rpslAttribute)) {
            return mapInternal(rpslAttribute, source);
        }
        return Collections.EMPTY_LIST;
    }

    protected boolean hasChanged(final Attribute attribute) {
        return CHANGED.equals(attribute.getName());
    }

    protected boolean hasChanged(final RpslAttribute rpslAttribute) {
        return CHANGED.equals(rpslAttribute.getKey());
    }
}