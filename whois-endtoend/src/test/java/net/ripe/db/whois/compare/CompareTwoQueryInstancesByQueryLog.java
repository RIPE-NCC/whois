package net.ripe.db.whois.compare;

import net.ripe.db.whois.common.ManualTest;
import net.ripe.db.whois.common.support.QueryLogEntry;
import net.ripe.db.whois.compare.common.ComparisonExecutorConfig;
import net.ripe.db.whois.compare.common.QueryReader;
import net.ripe.db.whois.compare.common.TargetInterface;
import net.ripe.db.whois.compare.common.ComparisonRunnerFactory;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

@Category(ManualTest.class)
public class CompareTwoQueryInstancesByQueryLog {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompareTwoQueryInstancesByQueryLog.class);

    @Test
    public void test_deployed_versions() throws Exception {
        LOGGER.info("Starting Whois port 43 comparison tests");

        new ComparisonRunnerFactory().createCompareResults(
                ComparisonExecutorConfig.PRE1,
                ComparisonExecutorConfig.PRE2,
                new QueryReader(new FileSystemResource("/export/opt/qrylog")) {
                    @Override
                    protected String getQuery(final String line) {
                        return QueryLogEntry.parse(line).getQueryString();
                    }
                },
                new File("target/qry/querylog"),
                TargetInterface.WHOIS)
             .runCompareTest();
    }
}
