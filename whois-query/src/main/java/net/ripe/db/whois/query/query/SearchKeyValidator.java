package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
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

        for (final ObjectType objectType : query.getObjectTypes()) {
            for (final AttributeType attributeType : ObjectTemplate.getTemplate(objectType).getLookupAttributes()) {
                if (AttributeMatcher.fetchableBy(attributeType, query)) {
                    return;
                }
            }
        }

        if (query.hasObjectTypesSpecified() || query.getObjectTypes().isEmpty()) {
            messages.add(QueryMessages.invalidSearchKey());
        } else {
            messages.add(QueryMessages.unsupportedQuery());
        }
    }
}
