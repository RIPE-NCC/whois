package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.query.domain.QueryMessages;

class BriefValidator implements QueryValidator {
    @Override
    public void validate(final Query query, final Messages messages) {
        if (!query.isBrief()) {
            return;
        }

        if (query.getIpKeyOrNull() == null) {
            messages.add(QueryMessages.malformedQuery());
        }
    }
}
