package net.ripe.db.whois.api.rest.client;

import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StreamingRestClientTest {

    private static final int NUM_THREADS = 10;

    // TODO: [ES] concurrency issue in StreamingRestClient (NullPointerExceptions).
    @Ignore
    @Test
    public void concurrent_test() throws Exception {
        final AtomicInteger exceptions = new AtomicInteger();

        final ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        for (int thread = 0; thread < NUM_THREADS; thread++) {
            executorService.submit(new Runnable() {
                @Override public void run() {
                    final StreamingRestClient streamingRestClient = new StreamingRestClient(
                            (new ByteArrayInputStream(
                                ("<whois-resources>\n" +
                                "    <objects>\n" +
                                "        <object type=\"person\">\n" +
                                "            <source id=\"RIPE\"/>\n" +
                                "            <attributes>\n" +
                                "                <attribute name=\"person\" value=\"Pauleth Palthen\"/>\n" +
                                "                <attribute name=\"address\" value=\"Singel 258\"/>\n" +
                                "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                                "                <attribute name=\"e-mail\" value=\"noreply@ripe.net\"/>\n" +
                                "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                                "                <attribute name=\"nic-hdl\" value=\"PP1-TEST\"/>\n" +
                                "                <attribute name=\"changed\" value=\"ppalse@ripe.net 20101228\"/>\n" +
                                "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                                "                <attribute name=\"password\" value=\"test\"/>\n" +
                                "            </attributes>\n" +
                                "        </object>\n" +
                                "    </objects>\n" +
                                "</whois-resources>").getBytes())));
                    while (streamingRestClient.hasNext()) {
                        try {
                            streamingRestClient.next();
                        } catch (Exception e) {
                            exceptions.incrementAndGet();
                        }
                    }
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(exceptions.get(), is(0));
    }
}