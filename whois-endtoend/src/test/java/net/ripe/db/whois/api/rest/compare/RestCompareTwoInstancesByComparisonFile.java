package net.ripe.db.whois.api.rest.compare;

import net.ripe.db.whois.common.ManualTest;
import net.ripe.db.whois.query.endtoend.compare.QueryReader;
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
                    protected String getQuery(final String line) {
                        return line;
                    }
                },
                new File("target/qry/comparison_rest_file"),
                1).runCompareTest();
    }
}
