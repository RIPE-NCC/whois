package net.ripe.db.whois.query.endtoend;

import net.ripe.db.whois.common.ManualTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

@Category(ManualTest.class)
public class RestCompareTwoInstancesByComparisonFile {

    @Test
    public void test_deployed_versions() throws Exception {
        new RestCompareResults(
                RestExecutorConfiguration.PRE1,
                RestExecutorConfiguration.PRE2,
                new QueryReader(new ClassPathResource("comparison_rest")) {
                    @Override
                    String getQuery(final String line) {
                        return line;
                    }
                },
                new File("target/qry/comparison_rest_file"),
                1).runCompareTest();
    }
}
