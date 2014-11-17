package net.ripe.db.whois.compare;

import net.ripe.db.whois.common.ManualTest;
import net.ripe.db.whois.compare.common.ComparisonExecutorConfig;
import net.ripe.db.whois.compare.common.TargetInterface;
import net.ripe.db.whois.compare.common.ComparisonRunnerFactory;
import net.ripe.db.whois.compare.telnet.TelnetClientUtils;
import net.ripe.db.whois.compare.telnet.NrtmReader;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static net.ripe.db.whois.compare.common.ComparisonExecutorConfig.PRE1;
import static net.ripe.db.whois.compare.common.ComparisonExecutorConfig.PRE2;

@Category(ManualTest.class)
public class CompareTwoNrtmInstances {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompareTwoNrtmInstances.class);

    @Test
    public void test_deployed_versions() throws Exception {

        FileUtils.deleteDirectory(new File("target/qry/nrtm_comparison"));

        final ComparisonExecutorConfig config1 = PRE1;
        final ComparisonExecutorConfig config2 = PRE2;

        LOGGER.info("Starting NRTM comparison tests");
        new ComparisonRunnerFactory().createCompareResults(
                config1,
                config2,
                new NrtmReader(getLatestSerialId(config1, config2)),
                new File("target/qry/nrtm_comparison"),
                TargetInterface.NRTM)
              .runCompareTest();
    }

    private long getLatestSerialId(final ComparisonExecutorConfig config1, final ComparisonExecutorConfig config2) throws IOException {

        final long latestSerialId1 = TelnetClientUtils.getLatestSerialId(config1);
        final long latestSerialId2 = TelnetClientUtils.getLatestSerialId(config2);
        assertThat("Latest serial_id must be the same in both servers", latestSerialId1, is(latestSerialId2));

        return latestSerialId1;
    }
}


