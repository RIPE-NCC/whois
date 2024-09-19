package net.ripe.db.whois.common.grs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.HttpHeaders;
import net.ripe.commons.ip.Asn;
import net.ripe.commons.ip.AsnRange;
import net.ripe.commons.ip.Ipv4;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.commons.ip.Ipv6;
import net.ripe.commons.ip.Ipv6Range;
import net.ripe.commons.ip.SortedRangeSet;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

public class RsngAuthoritativeResourceWorker {

    protected final Logger logger;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    final SortedRangeSet<Asn, AsnRange> autNums = new SortedRangeSet<>();
    final SortedRangeSet<Ipv4, Ipv4Range> ipv4Space = new SortedRangeSet<>();
    final SortedRangeSet<Ipv6, Ipv6Range> ipv6Space = new SortedRangeSet<>();

    private final String rsngBaseUrl;
    private final Client client;
    private final String apiKey;
    private final ExecutorService executorService;

        RsngAuthoritativeResourceWorker(final Logger logger, final String rsngBaseUrl, final Client client, final ExecutorService executorService, final String apiKey) {
        this.logger = logger;
        this.client = client;
        this.rsngBaseUrl = rsngBaseUrl;
        this.apiKey = apiKey;
        this.executorService = executorService;
    }

    public AuthoritativeResource load() {

        CompletableFuture.allOf(

                CompletableFuture.supplyAsync(() -> getRsngDelegations("/resource-services/asn-delegations"), executorService).
                        thenAccept( asnDelegations -> {
                            try {
                                getResponse(asnDelegations).forEach(asnDelegation -> autNums.add(AsnRange.parse(asnDelegation.get("range").asText())));
                            } catch (IOException e) {
                                logger.warn("failure in fetching asn delegations  {}", e.getCause() );
                                throw new CompletionException(e);
                            }
                        }),
                CompletableFuture.supplyAsync(() -> getRsngDelegations("/resource-services/ipv4-delegations"), executorService).
                        thenAccept( ipv4Delegations -> {
                            try {
                                getResponse(ipv4Delegations).forEach(ipv4Delegation -> ipv4Space.add( Ipv4Range.parse(ipv4Delegation.get("range").asText())));
                            } catch (IOException e) {
                                logger.warn("failure in fetching ipv4 delegations {}", e.getCause() );
                                throw new CompletionException(e);
                            }
                        }),
                CompletableFuture.supplyAsync(() -> getRsngDelegations("/resource-services/ipv6-delegations"), executorService).
                        thenAccept( ipv6Delegations -> {
                            try {
                                getResponse(ipv6Delegations).forEach(ipv6Delegation -> ipv6Space.add( Ipv6Range.parse(ipv6Delegation.get("range").asText())));
                            } catch (IOException e) {
                                logger.warn("failure in fetching ipv6 delegations {}", e.getCause() );
                                throw new CompletionException(e);
                            }
                        })
        ).join();

        return  new AuthoritativeResource(autNums, ipv4Space, ipv6Space);
    }

    private JsonNode getResponse(String rsngDelegations) throws IOException {
        return OBJECT_MAPPER.readValue(rsngDelegations, JsonNode.class).get("response").get("results");
    }

    private String getRsngDelegations(final String url) {
        return client.target(rsngBaseUrl)
                .path(url)
                .queryParam("page-size", "200000")  //Rsng does not have an api without pagination. So putting maximum allowed page size
                .queryParam("page-number", "1")
                .request()
                .header(HttpHeaders.ACCEPT, "application/json")
                .header("ncc-internal-api-key", apiKey)
                .get(String.class);
    }
}
