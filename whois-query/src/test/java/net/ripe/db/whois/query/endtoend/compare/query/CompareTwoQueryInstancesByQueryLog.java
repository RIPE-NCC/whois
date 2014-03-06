package net.ripe.db.whois.query.endtoend.compare.query;

import net.ripe.db.whois.common.ManualTest;
import net.ripe.db.whois.common.support.QueryExecutorConfiguration;
import net.ripe.db.whois.common.support.QueryLogEntry;
import net.ripe.db.whois.query.endtoend.compare.QueryReader;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

@Category(ManualTest.class)
public class CompareTwoQueryInstancesByQueryLog {

    @Test
    @Ignore
    public void test_deployed_versions() throws Exception {
        new CompareQueryResults(
                QueryExecutorConfiguration.PRE1,
                QueryExecutorConfiguration.PRE2,
                new QueryReader(new FileSystemResource("/export/opt/qrylog")) {
                    @Override
                    protected String getQuery(final String line) {
                        return QueryLogEntry.parse(line).getQueryString();
                    }
                },
                new File("target/qry/querylog"),
                2500).runCompareTest();
    }
}
