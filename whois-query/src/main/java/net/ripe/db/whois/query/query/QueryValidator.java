package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.Messages;

interface QueryValidator {
    void validate(Query query, Messages messages);
}
