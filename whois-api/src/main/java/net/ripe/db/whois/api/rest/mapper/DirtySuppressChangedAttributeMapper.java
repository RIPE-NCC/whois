package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

// TODO: remove this class once changed has been completely removed from whois
@Component
public class DirtySuppressChangedAttributeMapper extends BaseSuppressChangedAttributeMapper implements AttributeMapper {
    protected DirtyServerAttributeMapper mapper;

    @Autowired
    public DirtySuppressChangedAttributeMapper(final DirtyServerAttributeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Collection<RpslAttribute> map(final Attribute attribute) {
        if (!hasChanged(attribute)) {
            return this.mapper.map(attribute);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<Attribute> map(final RpslAttribute rpslAttribute, final String source) {
        if (!hasChanged(rpslAttribute)) {
            return this.mapper.map(rpslAttribute, source);
        }
        return Collections.EMPTY_LIST;
    }

}