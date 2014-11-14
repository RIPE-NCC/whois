package net.ripe.db.whois.api.rest.compare;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.query.endtoend.compare.CompareResults;
import net.ripe.db.whois.query.endtoend.compare.ComparisonConfiguration;
import net.ripe.db.whois.query.endtoend.compare.ComparisonExecutor;
import net.ripe.db.whois.query.endtoend.compare.QueryReader;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static net.ripe.db.whois.query.endtoend.compare.ComparisonPrinter.writeDifferences;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RestCompareResults implements CompareResults {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestCompareResults.class);

    public enum QueryType {
        ALL, XML, JSON
    }

    private final ComparisonConfiguration config1;
    private final ComparisonConfiguration config2;
    private final RestExecutor restExecutor1;
    private final RestExecutor restExecutor2;
    private final ExecutorService executorService;
    private final QueryReader queryReader;
    private final File targetDir;
    private QueryType queryType;

    public RestCompareResults(final RestExecutorConfiguration config1,
                              final RestExecutorConfiguration config2,
                              final QueryReader queryReader, final File targetDir,
                              final QueryType queryType) throws UnknownHostException {
        this.config1 = config1;
        this.config2 = config2;
        this.restExecutor1 = new RestExecutor(config1);
        this.restExecutor2 = new RestExecutor(config2);
        this.executorService = Executors.newFixedThreadPool(2);
        this.queryReader = queryReader;
        this.targetDir = targetDir;
        this.queryType = queryType;
    }

    @Override
    public void runCompareTest() throws Exception {
        LOGGER.info("Diffs saved in: {}", targetDir.getAbsolutePath());
        assertFalse("Dir should not exist: " + targetDir.getAbsolutePath(), targetDir.exists());
        assertTrue("Unable to create: " + targetDir.getAbsolutePath(), targetDir.mkdirs());
        assertTrue(new File(targetDir, "0_deltas_go_here.txt").createNewFile());

        int failedQueries = 0;
        FluentIterable<String> queries = FluentIterable
                .from(queryReader.getQueries())
                .filter(new Predicate<String>() {
                    @Override
                    public boolean apply(@Nullable String input) {
                        return !(input == null || (StringUtils.isBlank(input) || input.startsWith("#")));
                    }
                });

        // do only XML or JSON if needed
        if (queryType == QueryType.XML) {
            queries = queries.filter(new Predicate<String>() {
                @Override
                public boolean apply(@Nullable String input) {
                    return !(input != null && input.contains(".json"));
                }
            });
        } else if (queryType == QueryType.JSON) {
            queries = queries.filter(new Predicate<String>() {
                @Override
                public boolean apply(@Nullable String input) {
                    return input != null && input.contains(".json");
                }
            });
        }

        if (queries.size() == 1) {
            LOGGER.info("Compare query: {}", queries.get(0));
        }

        for (final String queryString : queries) {
            final Future<List<ResponseObject>> queryExecutor1Future = executeQuery(restExecutor1, queryString);
            final Future<List<ResponseObject>> queryExecutor2Future = executeQuery(restExecutor2, queryString);

            final List<ResponseObject> queryExecutor1Result = queryExecutor1Future.get();
            final List<ResponseObject> queryExecutor2Result = queryExecutor2Future.get();

            final Patch patch = DiffUtils.diff(queryExecutor1Result, queryExecutor2Result);
            final List<Delta> deltas = patch.getDeltas();
            if (!deltas.isEmpty()) {
                writeDifferences(targetDir, queryString, queryExecutor1Result, queryExecutor2Result, deltas);
                failedQueries++;
                LOGGER.error("Query '{}' has differences", queryString);
            }
        }

        LOGGER.info("Compared {} queries", queries.size());
        assertThat("Number of failed queries", failedQueries, is(0));
    }

    public Future<List<ResponseObject>> executeQuery(final ComparisonExecutor comparisonExecutor, final String queryString) {
        return executorService.submit(new Callable<List<ResponseObject>>() {
            @Override
            public List<ResponseObject> call() throws Exception {
                return comparisonExecutor.getResponse(queryString);
            }
        });
    }
}
