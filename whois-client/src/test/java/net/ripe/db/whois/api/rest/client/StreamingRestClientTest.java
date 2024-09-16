package net.ripe.db.whois.api.rest.client;

import net.ripe.db.whois.api.rest.domain.WhoisResources;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class StreamingRestClientTest {

    private static final int NUM_THREADS = 10;

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

    @Test
    public void read_error_response() {
        final WhoisResources whoisResources = StreamingRestClient.unMarshalError(
                new ByteArrayInputStream((
                        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                        "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
                        "<link xlink:type=\"locator\" xlink:href=\"http://localhost:57744/search?query-ng=bla\"/>" +
                        "<errormessages><errormessage severity=\"Error\" text=\"Query param 'query-string' cannot be empty\"/></errormessages>" +
                        "<terms-and-conditions xlink:type=\"locator\" xlink:href=\"https://docs.db.ripe.net/terms-conditions.html\"/>" +
                        "</whois-resources>\n").getBytes()));

        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().get(0).getText(), is("Query param 'query-string' cannot be empty"));
    }
}
