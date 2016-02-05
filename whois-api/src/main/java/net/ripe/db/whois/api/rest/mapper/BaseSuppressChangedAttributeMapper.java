package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslAttribute;

// TODO: remove this class once changed has been completely removed from whois
class BaseSuppressChangedAttributeMapper {
    protected static final CIString CHANGED = CIString.ciString("changed");

    protected boolean hasChanged(final Attribute attribute) {
        return CHANGED.equals(attribute.getName());
    }

    protected boolean hasChanged(final RpslAttribute rpslAttribute) {
        return CHANGED.equals(rpslAttribute.getKey());
    }
}
