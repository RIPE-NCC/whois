package net.ripe.db.whois.api.whois.mapper;

import net.ripe.db.whois.api.whois.domain.Attribute;
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
    Attribute buildAttribute(RpslAttribute attribute, final CIString value, final String comment, final String source) {
        return createAttribute(attribute.getKey(), value.toString(), comment, null, null);
    }
}
