package net.ripe.db.whois.nrtm.integration;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.Timestamp;
import net.ripe.db.whois.common.pipeline.ChannelUtil;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.nrtm.NrtmServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class NrtmConcurrencyTestIntegration extends AbstractNrtmIntegrationBase {

    private static final int NUM_THREADS = 100;
    private static final int MIN_RANGE = 21486000;
    private static final int MID_RANGE = 21486049;  // 21486050 is a person in nrtm_sample.sql
    private static final int MAX_RANGE = 21486100;

    private CountDownLatch countDownLatch;

    @BeforeAll
    public static void setNrtmProperties() {
        System.setProperty("whois.limit.connectionsPerIp", "100");
        System.setProperty("nrtm.update.interval", "1");
    }

    @AfterAll
    public static void clearNrtmProperties() {
        System.clearProperty("nrtm.update.interval");
        System.clearProperty("whois.limit.connectionsPerIp");
    }

    @BeforeEach
    public void before() {
        loadSerials(0, Integer.MAX_VALUE);
        nrtmServer.start();
    }

    @AfterEach
    public void after() {
        nrtmServer.stop(true);
    }

    @Test
    @Disabled // FIXME [SB] fix this test
    public void dontHangOnHugeAutNumObject() {
        String response = TelnetWhoisClient.queryLocalhost(nrtmServer.getPort(), String.format("-g TEST:3:%d-%d", MIN_RANGE, MAX_RANGE), 5 * 1000);

        assertThat(response, containsString(String.format("ADD %d", MIN_RANGE)));  // serial 21486000 is a huge aut-num
        assertThat(response, containsString(String.format("DEL %d", MIN_RANGE + 1)));
    }

    @Test
    @Disabled // FIXME [SB] fix this test
    public void dontHangOnHugeAutNumObjectKeepalive() throws Exception {
        countDownLatch = new CountDownLatch(1);

        // initial serial range
        setSerial(MIN_RANGE + 1, MIN_RANGE + 1);
        String query = String.format("-g TEST:3:%d-LAST -k", MIN_RANGE + 1);

        NrtmTestThread thread = new NrtmTestThread(query, MIN_RANGE + 1);
        thread.start();
        countDownLatch.await(10L, TimeUnit.SECONDS);
        assertThat(thread.delCount, is(1));

        // expand serial range to include huge aut-num object
        countDownLatch = new CountDownLatch(1);
        thread.setLastSerial(MIN_RANGE + 4);
        setSerial(MIN_RANGE + 1, MIN_RANGE + 4);
        countDownLatch.await(10L, TimeUnit.SECONDS);

        assertThat(thread.addCount, is(1));
        assertThat(thread.delCount, is(3));

        thread.stop = true;
    }

    @Test
    public void manySimultaneousClientsReadingManyObjects() throws InterruptedException {
        // 1st part: clients request MIN to LAST with -k flag, but we provide half of the available serials only
        final List<NrtmTestThread> threads = Lists.newArrayList();
        countDownLatch = new CountDownLatch(NUM_THREADS);

        setSerial(MIN_RANGE, MID_RANGE);

        String query = String.format("-g TEST:3:%d-LAST -k", MIN_RANGE);

        for (int i = 0; i < NUM_THREADS; i++) {
            NrtmTestThread thread = new NrtmTestThread(query, MID_RANGE);
            threads.add(thread);
            thread.start();
        }

        countDownLatch.await(10L, TimeUnit.SECONDS);

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

        countDownLatch.await(10L, TimeUnit.SECONDS);

        // check results

        for (NrtmTestThread thread : threads) {
            thread.stop = true;

            if (thread.error != null) {
                fail("Thread reported error: " + thread.error);
            }

            if (!thread.isAlive()) {
                fail("Thread is no longer running, but didn't report an error?");
            }
        }

        final Set<Integer> delCount = threads.stream().map(thread -> thread.delCount).collect(Collectors.toSet());
        if (delCount.size() != 1) {
            fail("DEL count mismatch: " + delCount);
        }

        final Set<Integer> addCount = threads.stream().map(thread -> thread.addCount).collect(Collectors.toSet());
        if (addCount.size() != 1) {
            fail("ADD count mismatch: " + addCount);
        }
    }

    private void setSerial(int min, int max) {
        truncateTables();
        loadSerials(min, max);
    }

    private void loadSerials(int min, int max) {
        loadScripts(whoisTemplate, "nrtm_sample.sql");
        whoisTemplate.update("DELETE FROM serials WHERE serial_id < ? OR serial_id > ?", min, max);
        whoisTemplate.update("UPDATE last SET timestamp = ?", Timestamp.from(testDateTimeProvider.getCurrentDateTime()).getValue());
        whoisTemplate.update("UPDATE history SET timestamp = ?", Timestamp.from(testDateTimeProvider.getCurrentDateTime()).getValue());
    }

    private void truncateTables() {
        whoisTemplate.execute("TRUNCATE TABLE serials");
        whoisTemplate.execute("TRUNCATE TABLE history");
        whoisTemplate.execute("TRUNCATE TABLE last");
    }

    private class NrtmTestThread extends Thread {
        volatile String error;
        volatile int addCount;
        volatile int delCount;
        volatile boolean stop = false;
        final String query;
        int lastSerial;

        public NrtmTestThread(String query, int lastSerial) {
            this.query = query;
            this.lastSerial = lastSerial;
        }

        public void setLastSerial(int lastSerial) {
            this.lastSerial = lastSerial;
        }

        @Override
        public void run() {
            try (final Socket socket = new Socket("localhost", nrtmServer.getPort())) {
                socket.setSoTimeout(1000);

                try (final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), ChannelUtil.BYTE_ENCODING))) {

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
                                signalLatch(line.substring(4));
                            }

                            if (line.startsWith("DEL ")) {
                                delCount++;
                                signalLatch(line.substring(4));
                            }

                        } catch (SocketTimeoutException ignored) {
                        }

                        if (stop) {
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                error = e.getMessage();
            }
        }

        private void signalLatch(String serial) {
            if (Integer.parseInt(serial) >= lastSerial) {
                countDownLatch.countDown();
            }
        }

    }
}
