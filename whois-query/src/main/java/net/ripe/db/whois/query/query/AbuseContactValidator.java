package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.query.domain.QueryMessages;

class AbuseContactValidator implements QueryValidator {
    @Override
    public void validate(final Query query, final Messages messages) {
        if (!query.isAbuseContact()) {
            return;
        }

        try {
            new AttributeParser.AutNumParser().parse(query.getSearchValue());
        } catch (final Exception ignored) {
            if (query.getIpKeyOrNull() == null) {
                messages.add(QueryMessages.malformedQuery());
            }
        }
    }
}
