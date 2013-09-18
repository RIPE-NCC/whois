package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.query.domain.QueryMessages;

class SearchKeyValidator implements QueryValidator {
    @Override
    public void validate(final Query query, final Messages messages) {
        // No search key required for queries below
        if (query.isHelp() || query.isTemplate() || query.isVerbose() || query.isSystemInfo() || query.hasOnlyKeepAlive()) {
            return;
        }

        if (query.getSearchValue().isEmpty()) {
            messages.add(QueryMessages.noSearchKeySpecified());
        }

        // We don't check attributes for inverse queries, but search value is required
        if (query.isInverse()) {
            return;
        }

        if (query.getObjectTypes().isEmpty()) {
            messages.add(QueryMessages.invalidSearchKey());
        }
    }
}
