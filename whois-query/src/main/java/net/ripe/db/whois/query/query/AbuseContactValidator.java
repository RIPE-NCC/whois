package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.attrs.AutNum;
import net.ripe.db.whois.query.QueryMessages;

class AbuseContactValidator implements QueryValidator {
    @Override
    public void validate(final Query query, final Messages messages) {
        if (!query.isBriefAbuseContact()) {
            return;
        }

        if (query.getIpKeyOrNull() == null) {
            try {
                AutNum.parse(query.getSearchValue());
            } catch (final Exception ignored) {
                messages.add(QueryMessages.malformedQuery());
            }
        }
    }
}
