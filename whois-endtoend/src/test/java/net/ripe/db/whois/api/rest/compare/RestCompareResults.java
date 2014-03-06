package net.ripe.db.whois.api.rest.compare;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.query.endtoend.compare.CompareResults;
import net.ripe.db.whois.query.endtoend.compare.ComparisonConfiguration;
import net.ripe.db.whois.query.endtoend.compare.ComparisonExecutor;
import net.ripe.db.whois.query.endtoend.compare.query.KnownDifferencesPredicate;
import net.ripe.db.whois.query.endtoend.compare.QueryReader;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static net.ripe.db.whois.query.endtoend.compare.ComparisonPrinter.writeDifferences;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RestCompareResults implements CompareResults {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestCompareResults.class);

    private final ComparisonConfiguration config1;
    private final ComparisonConfiguration config2;
    private final RestExecutor restExecutor1;
    private final RestExecutor restExecutor2;
    private final ExecutorService executorService;
    private final QueryReader queryReader;
    private final File targetDir;
    private final int logEntries;

    public RestCompareResults(final RestExecutorConfiguration config1, final RestExecutorConfiguration config2, final QueryReader queryReader, final File targetDir, final int logEntries) throws UnknownHostException {
        this.config1 = config1;
        this.config2 = config2;
        this.restExecutor1 = new RestExecutor(config1);
        this.restExecutor2 = new RestExecutor(config2);
        this.executorService = Executors.newFixedThreadPool(2);
        this.queryReader = queryReader;
        this.targetDir = targetDir;
        this.logEntries = logEntries;
    }

    @Override
    public void runCompareTest() throws Exception {
        LOGGER.info("Diffs saved in: {}", targetDir.getAbsolutePath());
        assertFalse("Dir should not exist: " + targetDir.getAbsolutePath(), targetDir.exists());
        assertTrue("Unable to create: " + targetDir.getAbsolutePath(), targetDir.mkdirs());
        assertTrue(new File(targetDir, "0_deltas_go_here.txt").createNewFile());

        int nrQueries = 0;
        int failedQueries = 0;
        for (final String queryString : queryReader.getQueries()) {
            if (StringUtils.isBlank(queryString) || queryString.startsWith("#")) {
                continue;
            }

            if (logEntries == 1) {
                LOGGER.info("Compare query: {}", queryString);
            } else if (++nrQueries % logEntries == 0) {
                LOGGER.info("Compared {} queries", nrQueries);
            }

            final Future<List<ResponseObject>> queryExecutor1Future = executeQuery(restExecutor1, queryString);
            final Future<List<ResponseObject>> queryExecutor2Future = executeQuery(restExecutor2, queryString);

            final List<ResponseObject> queryExecutor1Result = queryExecutor1Future.get();
            final List<ResponseObject> queryExecutor2Result = queryExecutor2Future.get();

            final KnownDifferencesPredicate knownDifferencesPredicate = new KnownDifferencesPredicate();
            final List<ResponseObject> responseObjects1 = Lists.newArrayList(Iterables.filter(queryExecutor1Result, knownDifferencesPredicate));
            final List<ResponseObject> responseObjects2 = Lists.newArrayList(Iterables.filter(queryExecutor2Result, knownDifferencesPredicate));

            final Patch patch = DiffUtils.diff(responseObjects1, responseObjects2);
            final List<Delta> deltas = patch.getDeltas();
            if (!deltas.isEmpty()) {
                writeDifferences(targetDir, queryString, queryExecutor1Result, queryExecutor2Result, deltas);
                failedQueries++;
                LOGGER.error("Query '{}' has differences", queryString);
            }
        }

        assertThat("Number of failed queries", failedQueries, Matchers.is(0));
    }

    public Future<List<ResponseObject>> executeQuery(final ComparisonExecutor queryExecutor, final String queryString) {
        return executorService.submit(new Callable<List<ResponseObject>>() {
            @Override
            public List<ResponseObject> call() throws Exception {
                return queryExecutor.getResponse(queryString);
            }
        });
    }


}
