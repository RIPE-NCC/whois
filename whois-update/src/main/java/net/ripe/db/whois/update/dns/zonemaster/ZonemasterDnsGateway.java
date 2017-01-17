package net.ripe.db.whois.update.dns.zonemaster;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.math.IntMath;
import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import net.ripe.db.whois.update.dns.DnsCheckRequest;
import net.ripe.db.whois.update.dns.DnsCheckResponse;
import net.ripe.db.whois.update.dns.DnsGateway;
import net.ripe.db.whois.update.dns.zonemaster.domain.GetTestResultsRequest;
import net.ripe.db.whois.update.dns.zonemaster.domain.GetTestResultsResponse;
import net.ripe.db.whois.update.dns.zonemaster.domain.StartDomainTestRequest;
import net.ripe.db.whois.update.dns.zonemaster.domain.StartDomainTestResponse;
import net.ripe.db.whois.update.dns.zonemaster.domain.TestProgressRequest;
import net.ripe.db.whois.update.dns.zonemaster.domain.TestProgressResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.ripe.db.whois.common.Messages.Type.ERROR;

// TODO: [ES] handle failure cases (e.g. server down, timeouts etc).
// TODO: [ES] DnsCheckRequest glue is never read, is it needed?
@DeployedProfile
//@Primary      // TODO: [ES] DnsGatewayImpl is deployed instead
@Component
public class ZonemasterDnsGateway implements DnsGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZonemasterDnsGateway.class);

    private static final int TEST_PROGRESS_SLEEP_SECONDS = 1;
    private static final int TEST_PROGRESS_MAXIMUM_RETRIES = 60 * 5;

    private static final String PERCENTAGE_COMPLETE = "100";
    private static final int FORK_THRESHOLD = 4;

    private static final ImmutableList<String> ERROR_LEVELS = ImmutableList.of("CRITICAL", "ERROR");

    private final ZonemasterRestClient zonemasterRestClient;

    public ZonemasterDnsGateway(final ZonemasterRestClient zonemasterRestClient) {
        this.zonemasterRestClient = zonemasterRestClient;
    }

    @Override
    public Map<DnsCheckRequest, DnsCheckResponse> performDnsChecks(final Set<DnsCheckRequest> dnsCheckRequests) {
        final Map<DnsCheckRequest, DnsCheckResponse> dnsResults = Maps.newHashMap();
        (new ForkJoinPool()).invoke(new DomainCheckAction(Lists.newArrayList(dnsCheckRequests), dnsResults));
        return dnsResults;
    }

    private class DomainCheckAction extends RecursiveAction {

        private final List<DnsCheckRequest> dnsCheckRequests;
        private final Map<DnsCheckRequest, DnsCheckResponse> responseMap;

        public DomainCheckAction(
                final List<DnsCheckRequest> dnsCheckRequests,
                final Map<DnsCheckRequest, DnsCheckResponse> responseMap) {
            this.dnsCheckRequests = dnsCheckRequests;
            this.responseMap = responseMap;
        }

        @Override
        protected void compute() {
            if (dnsCheckRequests.size() < FORK_THRESHOLD) {
                computeDirectly();
            } else {
                final List<List<DnsCheckRequest>> split = split(dnsCheckRequests);
                invokeAll(
                        new DomainCheckAction(split.get(0), responseMap),
                        new DomainCheckAction(split.get(1), responseMap));
            }
        }

        private List<List<DnsCheckRequest>> split(final List<DnsCheckRequest> dnsCheckRequests) {
            final int size = IntMath.divide(dnsCheckRequests.size(), 2, RoundingMode.UP);
            return Lists.partition(dnsCheckRequests, size);
        }

        private void computeDirectly() {
            for (final DnsCheckRequest dnsCheckRequest : dnsCheckRequests) {
                final String id = makeRequest(dnsCheckRequest);
                testProgressUntilComplete(id);
                final GetTestResultsResponse testResults = getResults(id);
                final DnsCheckResponse dnsCheckResponse = new DnsCheckResponse(getErrorsFromResults(testResults));
                responseMap.put(dnsCheckRequest, dnsCheckResponse);
            }
        }

        /**
         * Call start_domain_test API method.
         * @return check instance id
         */
        private String makeRequest(final DnsCheckRequest dnsCheckRequest) {
            final StartDomainTestRequest request = new StartDomainTestRequest(dnsCheckRequest);

            final StartDomainTestResponse response = zonemasterRestClient
                .sendRequest(request)
                .readEntity(StartDomainTestResponse.class);

            if (response.getError() != null) {
                LOGGER.warn("makeRequest error: {}", response.getError().toString());
                throw new IllegalArgumentException(response.getError().getMessage());
            }

            return response.getResult();
        }

        private void testProgressUntilComplete(final String id) {
            for (int retries = 0; retries < TEST_PROGRESS_MAXIMUM_RETRIES ; retries++) {
                Uninterruptibles.sleepUninterruptibly(TEST_PROGRESS_SLEEP_SECONDS, TimeUnit.SECONDS);
                if (PERCENTAGE_COMPLETE.equals(testProgress(id))) {
                    return;
                }
            }
            throw new IllegalStateException("Request timeout for id " + id);
        }

        /**
         * Call test_progress API method.
         * @return percentage complete
         */
        private String testProgress(final String id) {
            final TestProgressResponse response = zonemasterRestClient
                .sendRequest(new TestProgressRequest(id))
                .readEntity(TestProgressResponse.class);

            if (response.getError() != null) {
                LOGGER.warn("testProgress error: {}", response.getError().toString());
                throw new IllegalArgumentException(response.getError().getMessage());
            }

            return response.getResult();
        }

        /**
         * Call get_test_results API method.
         * @param id
         * @return API response
         */
        private GetTestResultsResponse getResults(final String id) {
            final GetTestResultsResponse response = zonemasterRestClient
                .sendRequest(new GetTestResultsRequest(id))
                .readEntity(GetTestResultsResponse.class);

            if (response.getError() != null) {
                LOGGER.warn("getResults error: {}", response.getError().toString());
                throw new IllegalArgumentException(response.getError().getMessage());
            }

            return response;
        }

        private List<Message> getErrorsFromResults(final GetTestResultsResponse testResults) {
            return testResults.getResult().getResults().stream()
                .filter(m->ERROR_LEVELS.contains(m.getLevel()))
                .map(m->new Message(ERROR, m.getMessage()))
                .collect(Collectors.toList());
        }
    }

}
