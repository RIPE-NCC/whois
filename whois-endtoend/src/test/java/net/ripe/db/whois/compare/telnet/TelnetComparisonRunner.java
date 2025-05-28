package net.ripe.db.whois.compare.telnet;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.compare.common.AbstractComparisonRunner;
import net.ripe.db.whois.compare.common.ComparisonExecutor;
import net.ripe.db.whois.compare.common.QueryReader;
import net.ripe.db.whois.query.domain.MessageObject;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Predicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TelnetComparisonRunner extends AbstractComparisonRunner {
    private final ComparisonExecutor executor1;
    private final ComparisonExecutor executor2;

    public TelnetComparisonRunner(
            final QueryReader queryReader,
            final File targetDir,
            final ComparisonExecutor executor1,
            final ComparisonExecutor executor2,
            final Logger logger) throws UnknownHostException {

        super(queryReader, targetDir, executor1, executor2, logger);
        this.executor1 = executor1;
        this.executor2 = executor2;
    }

    @Override
    protected void preRunHook() throws IOException {
        TelnetClientUtils.getVersion(executor1.getExecutorConfig());
        TelnetClientUtils.getVersion(executor2.getExecutorConfig());

        final Long serial1 = TelnetClientUtils.getLatestSerialId(executor1.getExecutorConfig());
        final Long serial2 = TelnetClientUtils.getLatestSerialId(executor2.getExecutorConfig());

        assertThat("Serials must be the same", serial1, equalTo(serial2));
    }

    @Override
    protected ComparisonResult filterOutKnownDifferences(
            final List<ResponseObject> executor1Result,
            final List<ResponseObject> executor2Result) {

        final Predicate<ResponseObject> knownDifferencesPredicate = (input -> !(input instanceof MessageObject) || !input.toString().startsWith("% This query was served by"));

        final List<ResponseObject> responseObjects1 = Lists.newArrayList(Iterables.filter(executor1Result, knownDifferencesPredicate::test));
        final List<ResponseObject> responseObjects2 = Lists.newArrayList(Iterables.filter(executor2Result, knownDifferencesPredicate::test));

        return new ComparisonResult(responseObjects1, responseObjects2);
    }
}
