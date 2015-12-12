package net.ripe.db.whois.compare;

import net.ripe.db.whois.common.ManualTest;
import net.ripe.db.whois.compare.common.ComparisonExecutorConfig;
import net.ripe.db.whois.compare.common.QueryReader;
import net.ripe.db.whois.compare.common.TargetInterface;
import net.ripe.db.whois.compare.common.ComparisonRunnerFactory;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

@Category(ManualTest.class)
public class CompareTwoQueryInstancesByFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompareTwoQueryInstancesByFile.class);

    @Test
    public void test_deployed_versions() throws Exception {
        LOGGER.info("Starting Whois port 43 comparison tests");

        FileUtils.deleteDirectory(new File("target/qry/comparison_file"));

        new ComparisonRunnerFactory().createCompareResults(
                ComparisonExecutorConfig.PRE1,
                ComparisonExecutorConfig.PRE2,
                new QueryReader(new ClassPathResource("comparison_queries")) {
                    @Override
                    protected String getQuery(final String line) {
                        return line;
                    }
                },
                new File("target/qry/comparison_file"),
                TargetInterface.WHOIS
              ).runCompareTest();
    }
}
