package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.api.rest.domain.AbusePKey;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.springframework.stereotype.Component;

@Component
public class AbuseContactMapper {

    public AbuseResources mapAbuseContact(final String key, final Iterable<RpslAttribute> attributes) {
        String foundKey = "";
        String abuseEmail = "";
        for (final RpslAttribute attribute : attributes) {
            if (attribute.getType() == AttributeType.ABUSE_MAILBOX) {
                abuseEmail = attribute.getCleanValue().toString();
            } else {
                foundKey = attribute.getCleanValue().toString();
            }
        }

        final AbuseResources abuseResources = new AbuseResources();
        abuseResources.setAbuseContact(new AbuseContact().setEmail(abuseEmail));
        abuseResources.setLink(new Link("locator", String.format("http://rest.db.ripe.net/abuse-contact/%s", key)));
        abuseResources.setService("abuse-contact");

        final Parameters parameters = new Parameters();
        parameters.setPrimaryKey(new AbusePKey(foundKey));
        abuseResources.setParameters(parameters);

        abuseResources.setTermsAndConditions(new Link("locator", WhoisResources.TERMS_AND_CONDITIONS));

        return abuseResources;
    }
}
