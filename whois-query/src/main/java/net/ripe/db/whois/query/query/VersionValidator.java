package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.QueryMessages;

class VersionValidator implements QueryValidator {
    @Override
    public void validate(final Query query, final Messages messages) {
        if (query.isVersionList() || query.isObjectVersion()) {
            if (query.hasObjectTypesSpecified()) {
                for (ObjectType type : query.getObjectTypes()) {
                    // We don't allow person/role object history
                    if (type == ObjectType.PERSON || type == ObjectType.ROLE) {
                        messages.add(QueryMessages.unsupportedVersionObjectType());
                    }
                }
            }
        }
    }
}
