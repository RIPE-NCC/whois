package net.ripe.db.whois.query.executor;

import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.query.Query;

public interface QueryExecutor {
    boolean isAclSupported();

    boolean supports(Query query);

    void execute(Query query, ResponseHandler responseHandler);
}
