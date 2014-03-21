package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.query.QueryMessages;

class InverseValidator implements QueryValidator {
    @Override
    public void validate(final Query query, final Messages messages) {
        if (query.isInverse()) {
            final String auth = query.getSearchValue().toUpperCase();
            if (auth.startsWith("SSO ") || auth.startsWith("MD5-PW ")) {
                messages.add(QueryMessages.inverseSearchNotAllowed());
            }
        }
    }
}
