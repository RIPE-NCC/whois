package net.ripe.db.whois.compare.rest;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.compare.common.AbstractComparisonRunner;
import net.ripe.db.whois.compare.common.ComparisonExecutor;
import net.ripe.db.whois.compare.common.QueryReader;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.UnknownHostException;
import java.util.List;

public class RestComparisonRunner extends AbstractComparisonRunner {

    public RestComparisonRunner(
            final QueryReader queryReader,
            final File targetDir,
            final ComparisonExecutor executor1,
            final ComparisonExecutor executor2) throws UnknownHostException {

        super(queryReader, targetDir, executor1, executor2, LoggerFactory.getLogger(RestComparisonRunner.class));
    }

    @Override
    protected void preRunHook() {

    }

    @Override
    @SuppressWarnings("unchecked")
    protected ComparisonResult filterOutKnownDifferences(
            final List<ResponseObject> executor1Result,
            final List<ResponseObject> executor2Result) {

        return new ComparisonResult(executor1Result, executor2Result);
    }
}
