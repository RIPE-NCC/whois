package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Component
public class DirtyClientAttributeMapper implements AttributeMapper {

    @Override
    public Collection<Attribute> map(RpslAttribute rpslAttribute, String source) {
        return Collections.singleton(new Attribute(rpslAttribute.getKey(), rpslAttribute.getFormattedValue(), null, null, null, null));
    }

}
