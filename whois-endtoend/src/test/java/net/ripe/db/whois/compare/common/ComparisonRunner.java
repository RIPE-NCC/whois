package net.ripe.db.whois.compare.common;

import net.ripe.db.whois.common.domain.ResponseObject;

import java.util.List;
import java.util.concurrent.Future;

public interface ComparisonRunner {
    Future<List<ResponseObject>> executeQuery(final ComparisonExecutor queryExecutor, final String queryString);
    void runCompareTest() throws Exception;
}
