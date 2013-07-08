package net.ripe.db.whois.nrtm.integration;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.pipeline.ChannelUtil;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.nrtm.NrtmServer;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class NrtmConcurrencyTestIntegration extends AbstractNrtmIntegrationBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmConcurrencyTestIntegration.class);

    private static final int NUM_THREADS = 20;
    private static final int WAIT_FOR_CLIENT_THREADS = 10;
    private static final int MIN_RANGE = 21486000;
    private static final int MID_RANGE = 21486049;  // 21486050 is a person in nrtm_sample.sql
    private static final int MAX_RANGE = 21486100;

    private static List<NrtmTestThread> testThreads;

    @BeforeClass
    public static void setInterval() {
        System.setProperty("nrtm.update.interval", "1");
    }

    @Before
    public void before() throws Exception {
        waitForRunningThreadsToFinish();
        loadSerials(0, Integer.MAX_VALUE);
        nrtmServer.start();
    }

    @After
    public void after() {
        nrtmServer.stop(true);
    }

    @Test
    public void dontHangOnHugeAutNumObject() throws Exception {
        LOGGER.info("Entering dontHangOnHugeAutNumObject()");
        String response = DummyWhoisClient.query(NrtmServer.port, String.format("-g TEST:3:%d-%d", MIN_RANGE, MAX_RANGE), 5 * 1000);

        assertTrue(response, response.contains(String.format("ADD %d", MIN_RANGE)));  // serial 21486000 is a huge aut-num
        assertTrue(response, response.contains(String.format("DEL %d", MIN_RANGE + 1)));
        LOGGER.info("Exiting dontHangOnHugeAutNumObject()");
    }

    @Test
    public void dontHangOnHugeAutNumObjectKeepalive() throws Exception {
        LOGGER.info("Entering dontHangOnHugeAutNumObjectKeepalive()");
        CountDownLatch countDownLatch = new CountDownLatch(1);

        // initial serial range
        setSerial(MIN_RANGE + 1, MIN_RANGE + 1);
        String query = String.format("-g TEST:3:%d-LAST -k", MIN_RANGE + 1);

        NrtmTestThread thread = new NrtmTestThread(query, MIN_RANGE + 1, countDownLatch);

        // junit will now wait until test finishes in @Before incase it thinks test is finished (and test threads still runnning)
        testThreads = Lists.newArrayList(thread);

        thread.start();
        countDownLatch.await(5, TimeUnit.SECONDS);
        assertThat(thread.delCount, is(1));

        // expand serial range to include huge aut-num object
        countDownLatch = new CountDownLatch(1);
        thread.setLastSerial(MIN_RANGE + 4);
        setSerial(MIN_RANGE + 1, MIN_RANGE + 4);
        countDownLatch.await(5, TimeUnit.SECONDS);

        assertThat(thread.addCount, is(1));
        assertThat(thread.delCount, is(3));

        thread.stop = true;
        LOGGER.info("Exiting dontHangOnHugeAutNumObjectKeepalive()");
    }

    @Test
    public void manySimultaneousClientsReadingManyObjects() throws InterruptedException {
        LOGGER.info("Entering manySimultaneousClientsReadingManyObjects()");
        // 1st part: clients request MIN to LAST with -k flag, but we provide half of the available serials only
        final List<NrtmTestThread> threads = Lists.newArrayList();

        // junit will now wait until test finishes in @Before incase it thinks test is finished (and test threads still runnning)
        testThreads = threads;
        CountDownLatch countDownLatch = new CountDownLatch(NUM_THREADS);

        setSerial(MIN_RANGE, MID_RANGE);

        String query = String.format("-g TEST:3:%d-LAST -k", MIN_RANGE);

        for (int i = 0; i < NUM_THREADS; i++) {
            NrtmTestThread thread = new NrtmTestThread(query, MID_RANGE, countDownLatch);
            threads.add(thread);
            thread.start();
        }

        countDownLatch.await(WAIT_FOR_CLIENT_THREADS, TimeUnit.SECONDS);

        for (NrtmTestThread thread : threads) {
            if (thread.error != null) {
                fail("Thread reported error: " + thread.error);
            }
            thread.setLastSerial(MAX_RANGE);
        }

        // 2nd part: clients get all of the serials (-k part of server kicks in)
        countDownLatch = new CountDownLatch(NUM_THREADS);

        // update MAX serial
        setSerial(MIN_RANGE, MAX_RANGE);

        countDownLatch.await(WAIT_FOR_CLIENT_THREADS, TimeUnit.SECONDS);

        // check results
        int addResult = threads.get(0).addCount;
        int delResult = threads.get(0).delCount;
        for (NrtmTestThread thread : threads) {
            thread.stop = true;
            if (thread.error != null) {
                LOGGER.error("Thread reported error: " + thread.error);
                fail("Thread reported error: " + thread.error);
            }

            if (!thread.isAlive()) {
                fail("Thread is no longer running, but didn't report an error?");
            }

            if (thread.addCount != addResult || thread.delCount != delResult) {
                fail("ADD/DEL counts mismatch: " + Iterables.transform(threads, new Function<NrtmTestThread, String>() {
                    @Override
                    public String apply(NrtmTestThread input) {
                        return input.addCount + "/" + input.delCount;
                    }
                }));
            }
        }

        LOGGER.info("ADD: {}, DEL: {}", addResult, delResult);
        LOGGER.info("Exiting manySimultaneousClientsReadingManyObjects()");
    }

    private void setSerial(int min, int max) {
        truncateTables();
        loadSerials(min, max);
    }

    private void loadSerials(int min, int max) {
        LOGGER.info("Setting serial: {} - {}", min, max);
        loadScripts(whoisTemplate, "nrtm_sample.sql");
        final int dropped = whoisTemplate.update("DELETE FROM serials WHERE serial_id < ? OR serial_id > ?", min, max);
        LOGGER.info("Dropped {} rows", dropped);
        whoisTemplate.update("UPDATE last SET timestamp = ?", System.currentTimeMillis() / 1000);
        whoisTemplate.update("UPDATE history SET timestamp = ?", System.currentTimeMillis() / 1000);
    }

    private void truncateTables() {
        whoisTemplate.execute("TRUNCATE TABLE serials");
        whoisTemplate.execute("TRUNCATE TABLE history");
        whoisTemplate.execute("TRUNCATE TABLE last");
    }

    private void waitForRunningThreadsToFinish() {
        if (testThreads != null) {
            for (Thread thread : testThreads) {
                try {
                    thread.join();
                } catch (InterruptedException ie) {}
            }
        }
    }


    static class NrtmTestThread extends Thread {
        volatile String error;
        volatile int addCount;
        volatile int delCount;
        volatile boolean stop = false;
        final String query;
        int lastSerial;
        CountDownLatch countDownLatch;

        public NrtmTestThread(String query, int lastSerial, CountDownLatch countDownLatch) {
            this.query = query;
            this.lastSerial = lastSerial;
            this.countDownLatch = countDownLatch;
        }

        public void setLastSerial(int lastSerial) {
            this.lastSerial = lastSerial;
        }

        @Override
        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;
            Socket socket = null;

            try {
                socket = new Socket("localhost", NrtmServer.port);
                socket.setSoTimeout(1000);

                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), ChannelUtil.BYTE_ENCODING));

                out.println(query);

                for (; ; ) {
                    try {
                        String line = in.readLine();

                        if (line == null) {
                            error = "unexpected end of stream.";
                            return;
                        }

                        if (line.startsWith("%ERROR:")) {
                            error = line;
                            return;
                        }

                        if (line.startsWith("ADD ")) {
                            addCount++;
                            LOGGER.info("addCount++ now set to: " + addCount);
                            LOGGER.info("line: " + line);
                            signalLatch(line.substring(4));
                        }

                        if (line.startsWith("DEL ")) {
                            delCount++;
                            LOGGER.info("delcount++ now set to: " + delCount);
                            LOGGER.info("line: " + line);
                            signalLatch(line.substring(4));
                        }

                    } catch (SocketTimeoutException ignored) {
                    }

                    if (stop) {
                        return;
                    }
                }
            } catch (Exception e) {
                error = e.getMessage();
                LOGGER.error("Exception in NrtmTestThread",e);
            } finally {
                IOUtils.closeQuietly(out);
                IOUtils.closeQuietly(in);
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        private void signalLatch(String serial) {
            if (Integer.parseInt(serial) >= lastSerial) {
                countDownLatch.countDown();
            }
        }

    }
}
