package net.ripe.db.whois.query.endtoend.compare;

import net.ripe.db.whois.common.domain.ResponseObject;

import java.util.List;
import java.util.concurrent.Future;

public interface CompareResults {
    Future<List<ResponseObject>> executeQuery(final ComparisonExecutor queryExecutor, final String queryString);
    void runCompareTest() throws Exception;
}
