package net.ripe.db.whois.query.endtoend.compare.query;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.common.support.QueryExecutorConfiguration;
import net.ripe.db.whois.query.endtoend.compare.CompareResults;
import net.ripe.db.whois.query.endtoend.compare.ComparisonExecutor;
import net.ripe.db.whois.query.endtoend.compare.QueryReader;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.query.endtoend.compare.ComparisonPrinter.writeDifferences;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CompareQueryResults implements CompareResults {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompareQueryResults.class);
    private static final Pattern VERSION_PATTERN = Pattern.compile("(?m)^% whois-server-(.*)$");
    private static final Pattern SERIAL_PATTERN = Pattern.compile("(?m)^\\w+:\\d+:X:\\d+-(\\d+)$");

    private final QueryExecutorConfiguration config1;
    private final QueryExecutorConfiguration config2;
    private final QueryExecutor queryExecutor1;
    private final QueryExecutor queryExecutor2;
    private final ExecutorService executorService;
    private final QueryReader queryReader;
    private final File targetDir;
    private final int logEntries;

    public CompareQueryResults(final QueryExecutorConfiguration config1, final QueryExecutorConfiguration config2, final QueryReader queryReader, final File targetDir, final int logEntries) throws UnknownHostException {
        this.config1 = config1;
        this.config2 = config2;
        this.queryExecutor1 = new QueryExecutor(config1);
        this.queryExecutor2 = new QueryExecutor(config2);
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

        logVersion(config1);
        logVersion(config2);

        Long serial1 = logSerial(config1);
        Long serial2 = logSerial(config2);
        assertEquals("Serials must be the same", serial1, serial2);

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

            final Future<List<ResponseObject>> queryExecutor1Future = executeQuery(queryExecutor1, queryString);
            final Future<List<ResponseObject>> queryExecutor2Future = executeQuery(queryExecutor2, queryString);
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

    @Override
    public Future<List<ResponseObject>> executeQuery(final ComparisonExecutor queryExecutor, final String queryString) {
        return executorService.submit(new Callable<List<ResponseObject>>() {
            @Override
            public List<ResponseObject> call() throws Exception {
                return queryExecutor.getResponse(queryString);
            }
        });
    }

    private void logVersion(final QueryExecutorConfiguration configuration) throws IOException {
        final TelnetWhoisClient client = new TelnetWhoisClient(configuration.getHost(), configuration.getQueryPort());
        final String response = client.sendQuery("-q version");
        final Matcher matcher = VERSION_PATTERN.matcher(response);
        if (!matcher.find()) {
            throw new RuntimeException("version string not found in whois query response of " + configuration.getIdentifier());
        }
        LOGGER.warn(" ***** Server {} is running version {} ***** ", configuration.getIdentifier(), matcher.group(1));
    }

    private long logSerial(final QueryExecutorConfiguration configuration) throws IOException {
        final TelnetWhoisClient client = new TelnetWhoisClient(configuration.getHost(), configuration.getNrtmPort());
        final String response = client.sendQuery("-q sources");
        final Matcher matcher = SERIAL_PATTERN.matcher(response);
        if (!matcher.find()) {
            throw new RuntimeException("serial string not found in whois nrtm response of " + configuration.getIdentifier());
        }

        final Long serial = Long.parseLong(matcher.group(1));
        LOGGER.warn(" ***** Server {} is running on DB with serial {} ***** ", configuration.getIdentifier(), serial);
        return serial;
    }
}
