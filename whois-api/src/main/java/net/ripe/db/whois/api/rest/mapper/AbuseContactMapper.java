package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.api.rest.domain.AbusePKey;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;

public class AbuseContactMapper {

    public static AbuseResources mapAbuseContact(final String key, final Iterable<RpslAttribute> attributes) {
        String foundKey = "";
        String abuseEmail = "";
        for (final RpslAttribute attribute : attributes) {
            if (attribute.getType() == AttributeType.ABUSE_MAILBOX) {
                abuseEmail = attribute.getCleanValue().toString();
            } else {
                foundKey = attribute.getCleanValue().toString();
            }
        }

        final Parameters parameters = new Parameters(null, null, null, null, null, new AbusePKey(foundKey));

        return new AbuseResources(
                "abuse-contact",
                new Link("locator", String.format("http://rest.db.ripe.net/abuse-contact/%s", key)),
                parameters,
                new AbuseContact(abuseEmail),
                new Link("locator", WhoisResources.TERMS_AND_CONDITIONS)
        );
    }

    public static AbuseResources mapAbuseContactError(final String errorMessage) {
        return new AbuseResources(errorMessage);
    }
}
