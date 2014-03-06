package net.ripe.db.whois.query.endtoend.compare.query;

import net.ripe.db.whois.common.ManualTest;
import net.ripe.db.whois.common.support.QueryExecutorConfiguration;
import net.ripe.db.whois.query.endtoend.compare.QueryReader;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

@Category(ManualTest.class)
public class CompareTwoQueryInstancesByComparisonFile {

    @Test
    public void test_deployed_versions() throws Exception {
        new CompareQueryResults(
                QueryExecutorConfiguration.PRE1,
                QueryExecutorConfiguration.PRE2,
                new QueryReader(new ClassPathResource("comparison_queries")) {
                    @Override
                    protected String getQuery(final String line) {
                        return line;
                    }
                },
                new File("target/qry/comparison_file"),
                1).runCompareTest();
    }
}
