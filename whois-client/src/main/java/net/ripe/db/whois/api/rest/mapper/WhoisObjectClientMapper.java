package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WhoisObjectClientMapper extends AbstractWhoisObjectMapper {

    @Autowired
    public WhoisObjectClientMapper(@Value("${api.rest.baseurl}") final String baseUrl) {
        super(baseUrl);
    }

    @Override
    Attribute buildAttribute(final RpslAttribute attribute, final CIString value, final String source) {
        return new Attribute(attribute.getKey(), value.toString(), attribute.getCleanComment(), null, null);
    }
}
