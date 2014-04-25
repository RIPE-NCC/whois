package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.rpsl.RpslAttribute;

import java.util.Collection;

public interface AttributeMapper {
    Collection<RpslAttribute> map(Attribute attributes);
    Collection<Attribute> map(RpslAttribute rpslObject, String source);
}
