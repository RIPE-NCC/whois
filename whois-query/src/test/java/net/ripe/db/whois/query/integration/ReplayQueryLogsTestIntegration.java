package net.ripe.db.whois.query.integration;

import net.ripe.db.whois.common.ManualTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.support.QueryExecutorConfiguration;
import net.ripe.db.whois.common.support.QueryLogEntry;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.support.QueryExecutor;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Category(ManualTest.class)
public class ReplayQueryLogsTestIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReplayQueryLogsTestIntegration.class);
    private static String whoisHost;
    private static int whoisPort;
    private static Resource queryLog;
    private static int delayBetweenQueries;
    private static int nrThreads;

    @Before
    public void setUp() throws Exception {
        whoisHost = "whois-pre";
        whoisPort = 43;

        queryLog = new ClassPathResource("replay.queries");
        delayBetweenQueries = 0;
        nrThreads = 1;
    }

    @Test
    public void test_replay() throws IOException, InterruptedException {
        new Replay(whoisHost, whoisPort, queryLog, delayBetweenQueries, nrThreads).replay();
    }

    static class Replay {
        private final QueryExecutor queryExecutor;
        private final Resource queryLog;
        private final int delayBetweenQueries;
        private final ExecutorService executorService;
        private final AccessControlListManager accessControlListManager;

        public Replay(final String whoisHost, final int whoisPort, final Resource queryLog, final int delayBetweenQueries, final int nrThreads) throws UnknownHostException {
            this.queryLog = queryLog;
            this.delayBetweenQueries = delayBetweenQueries;
            this.executorService = Executors.newFixedThreadPool(nrThreads);
            this.accessControlListManager = mock(AccessControlListManager.class);

            when(accessControlListManager.requiresAcl(any(RpslObject.class), any(Source.class))).thenReturn(false);

            queryExecutor = new QueryExecutor(new QueryExecutorConfiguration("WHO-IS", whoisHost, whoisPort), accessControlListManager, LOGGER);
        }

        public void replay() throws IOException, InterruptedException {
            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new InputStreamReader(queryLog.getInputStream()));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    final QueryLogEntry logEntry = QueryLogEntry.parse(line);

                    if (logEntry != null) {
                        replayEntry(logEntry);
                        sleepDelay();
                    }
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }

            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.MINUTES);
        }

        private void replayEntry(final QueryLogEntry logEntry) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String queryString = logEntry.getQueryString().replaceAll("-k", "").trim();
                        if (StringUtils.isNotEmpty(queryString)) {
                            LOGGER.info("Executing: {}", queryString);
                            queryExecutor.getWhoisResponse(queryString);
                        }
                    } catch (IOException e) {
                        LOGGER.error("Unexpected", e);
                    }
                }
            });
        }

        private void sleepDelay() throws InterruptedException {
            if (delayBetweenQueries != 0) {
                Thread.sleep(delayBetweenQueries);
            }
        }
    }
}
