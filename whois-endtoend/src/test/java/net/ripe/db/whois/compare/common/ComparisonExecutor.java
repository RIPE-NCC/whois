package net.ripe.db.whois.compare.common;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.support.QueryExecutorConfiguration;

import java.io.IOException;
import java.util.List;

public interface ComparisonExecutor {
    List<ResponseObject> getResponse(final String query) throws IOException;
    QueryExecutorConfiguration getExecutorConfig();
}
