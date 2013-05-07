package net.ripe.db.whois.query.endtoend;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.common.support.QueryExecutorConfiguration;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class CompareQueryResults {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompareQueryResults.class);
    private static final Pattern WHOIS_SERVER_PATTERN = Pattern.compile("(?m)^% whois-server-(.*)$");

    private final QueryExecutorConfiguration config1;
    private final QueryExecutorConfiguration config2;
    private final QueryExecutor queryExecutor1;
    private final QueryExecutor queryExecutor2;
    private final ExecutorService executorService;
    private final QueryReader queryReader;
    private final File targetDir;
    private final int logEntries;

    private static int filenameSuffix = 1;

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

    public void runCompareTest() throws Exception {
        LOGGER.info("Diffs saved in: {}", targetDir.getAbsolutePath());
        assertFalse("Dir should not exist: ls -la " + targetDir.getAbsolutePath(), targetDir.exists());
        assertTrue("Unable to create: " + targetDir.getAbsolutePath(), targetDir.mkdirs());
        assertTrue(new File(targetDir, "0_deltas_go_here.txt").createNewFile());

        logVersion(config1);
        logVersion(config2);

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
                writeDifferences(queryString, queryExecutor1Result, queryExecutor2Result, deltas);
                failedQueries++;
                LOGGER.error("Query '{}' has differences", queryString);
            }
        }

        assertThat("Number of failed queries", failedQueries, Matchers.is(0));
    }

    private Future<List<ResponseObject>> executeQuery(final QueryExecutor queryExecutor, final String queryString) {
        return executorService.submit(new Callable<List<ResponseObject>>() {
            @Override
            public List<ResponseObject> call() throws Exception {
                return queryExecutor.getWhoisResponse(queryString);
            }
        });
    }

    private void writeDifferences(final String query, final List<ResponseObject> responseObjects1, final List<ResponseObject> responseObjects2, final List<Delta> deltas) throws IOException {
        final String filenameBase = String.format("%d_%%s.txt", filenameSuffix++);

        writeObjects(query, new File(targetDir, String.format(filenameBase, "1")), responseObjects1);
        writeObjects(query, new File(targetDir, String.format(filenameBase, "2")), responseObjects2);
        writeDeltas(query, new File(targetDir, String.format(filenameBase, "DELTA")), deltas);
    }

    private void writeObjects(final String query, final File file, final List<ResponseObject> result) throws IOException {
        BufferedOutputStream os = null;

        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            os.write(query.getBytes(Charsets.UTF_8));
            os.write("\n\n".getBytes(Charsets.UTF_8));

            for (final ResponseObject responseObject : result) {
                responseObject.writeTo(os);
                os.write("\n".getBytes(Charsets.UTF_8));
            }
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    private void writeDeltas(final String query, final File file, final List<Delta> deltas) throws IOException {
        LOGGER.info("Creating {}", file.getAbsolutePath());

        BufferedOutputStream os = null;

        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            os.write(query.getBytes(Charsets.UTF_8));
            os.write("\n\n".getBytes(Charsets.UTF_8));

            for (final Delta delta : deltas) {
                final Chunk original = delta.getOriginal();
                final Chunk revised = delta.getRevised();

                os.write(String.format("\n\n" +
                        "---------- 1 (position %d, size %d) ----------\n\n%s\n\n" +
                        "---------- 2 (position %d, size %d) ----------\n\n%s\n\n",
                        original.getPosition(),
                        original.size(),
                        original.getLines(),
                        revised.getPosition(),
                        revised.size(),
                        revised.getLines()
                ).getBytes(Charsets.UTF_8));
            }
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    private void logVersion(final QueryExecutorConfiguration configuration) throws IOException {
        final DummyWhoisClient client = new DummyWhoisClient(configuration.getHost(), configuration.getPort());
        final String response = client.sendQuery("-q version");
        final Matcher matcher = WHOIS_SERVER_PATTERN.matcher(response);
        if (!matcher.find()) {
            throw new RuntimeException("version string not found in whois query response of "+configuration.getIdentifier());
        }
        LOGGER.warn(" ***** Server "+configuration.getIdentifier()+" is running version "+matcher.group(1));
    }
}
