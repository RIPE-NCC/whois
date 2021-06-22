package net.ripe.db.whois.common.grs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.commons.ip.Asn;
import net.ripe.commons.ip.AsnRange;
import net.ripe.commons.ip.Ipv4;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.commons.ip.Ipv6;
import net.ripe.commons.ip.Ipv6Range;
import net.ripe.commons.ip.SortedRangeSet;
import org.slf4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

public class AuthoritativeResourceWorker {

    protected final Logger logger;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    final SortedRangeSet<Asn, AsnRange> autNums = new SortedRangeSet<>();
    final SortedRangeSet<Ipv4, Ipv4Range> ipv4Space = new SortedRangeSet<>();
    final SortedRangeSet<Ipv6, Ipv6Range> ipv6Space = new SortedRangeSet<>();

    private final String rsngBaseUrl;
    private final Client client;
    private final String apiKey;
    private final ExecutorService executorService;

    AuthoritativeResourceWorker(final Logger logger, final String rsngBaseUrl, final Client client, final ExecutorService executorService, final String apiKey) {
        this.logger = logger;
        this.client = client;
        this.rsngBaseUrl = rsngBaseUrl;
        this.apiKey = apiKey;
        this.executorService = executorService;
    }

    public AuthoritativeResource load() {
        CompletableFuture<String> asnDelegations  = CompletableFuture.supplyAsync(() -> getRsngDelegations("/resource-services/asn-delegations?page-size=200000&page-number=1"), executorService);
        CompletableFuture<String> ipv4Delegations  = CompletableFuture.supplyAsync(() -> getRsngDelegations("/resource-services/ipv4-delegations?page-size=200000&page-number=1"), executorService);
        CompletableFuture<String> ipv6Delegations  = CompletableFuture.supplyAsync(() -> getRsngDelegations("/resource-services/ipv6-delegations?page-size=200000&page-number=1"), executorService);

        allOfTerminateOnFailure(asnDelegations, ipv4Delegations, ipv6Delegations).thenRun(() -> {
                try {
                    getResponse(asnDelegations).forEach(asnDelegation -> autNums.add( AsnRange.parse(asnDelegation.get("range").asText())));
                    getResponse(ipv4Delegations).forEach(ipv4Delegation -> ipv4Space.add( Ipv4Range.parse(ipv4Delegation.get("range").asText())));
                    getResponse(ipv6Delegations).forEach(ipv6Delegation -> ipv6Space.add( Ipv6Range.parse(ipv6Delegation.get("range").asText())));

                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            });

        return  new AuthoritativeResource(autNums, ipv4Space, ipv6Space);
    }

    private JsonNode getResponse(CompletableFuture<String> rsngDelegations) throws IOException {
        return OBJECT_MAPPER.readValue(rsngDelegations.join(), JsonNode.class).get("response").get("results");
    }

    private String getRsngDelegations(final String url) {
        final String response =  client.target(rsngBaseUrl)
                .path(url)
                .request()
                //.header(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate")
                .header("X-API_KEY", apiKey)
                .get(String.class);

        logger.info("response form rsng for {}, {}", url, response);
    }

    public static CompletableFuture allOfTerminateOnFailure(CompletableFuture<?>... futures) {
        CompletableFuture<?> failure = new CompletableFuture();
        for (CompletableFuture<?> f: futures) {
            f.exceptionally(ex -> {
                failure.completeExceptionally(ex);
                return null;
            });
        }
        return CompletableFuture.anyOf(failure, CompletableFuture.allOf(futures));
    }
}
