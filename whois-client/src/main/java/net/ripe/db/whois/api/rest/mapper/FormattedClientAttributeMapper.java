package net.ripe.db.whois.api.rest.mapper;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class FormattedClientAttributeMapper implements FormattedAttributeMapper {


    @Override
    public Collection<Attribute> map(final RpslAttribute rpslAttribute, final String source) {
        List<Attribute> result = Lists.newArrayList();
        for (CIString value : rpslAttribute.getCleanValues()) {
            result.add(new Attribute(rpslAttribute.getKey(), value.toString(), rpslAttribute.getCleanComment(), null, null, null));
        }
        return result;
    }
}
