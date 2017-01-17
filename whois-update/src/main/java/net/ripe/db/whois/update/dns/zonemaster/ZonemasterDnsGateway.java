package net.ripe.db.whois.update.dns.zonemaster;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.ripe.db.whois.common.Messages.Type.ERROR;

// TODO: [ES] DnsCheckRequest glue is never read, is it needed?
@DeployedProfile
//@Primary      // TODO: [ES] DnsGatewayImpl is deployed instead
@Component
public class ZonemasterDnsGateway implements DnsGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZonemasterDnsGateway.class);

    private static final int TEST_PROGRESS_SLEEP_SECONDS = 1;
    private static final int TEST_PROGRESS_MAXIMUM_RETRIES = 60 * 5;

    private static final String PERCENTAGE_COMPLETE = "100";
    private static final int THRESHOLD = 4;
    private static final ImmutableList<String> ERROR_LEVELS = ImmutableList.of("CRITICAL", "ERROR");

    private final ZonemasterRestClient zonemasterRestClient;

    public ZonemasterDnsGateway(final ZonemasterRestClient zonemasterRestClient) {
        this.zonemasterRestClient = zonemasterRestClient;
    }

    @Override
    public Map<DnsCheckRequest, DnsCheckResponse> performDnsChecks(final Set<DnsCheckRequest> dnsCheckRequests) {
        final Map<DnsCheckRequest, DnsCheckResponse> dnsResults = Maps.newHashMap();
        DomainCheckAction domainCheckAction = new DomainCheckAction(Lists.newArrayList(dnsCheckRequests), 0, dnsCheckRequests.size(), dnsResults);      // TODO: size - 1 ?
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(domainCheckAction);
        return dnsResults;
    }

    private class DomainCheckAction extends RecursiveAction {

        private int mStart;
        private int mLength;
        private List<DnsCheckRequest> dnsCheckRequests;
        private Map<DnsCheckRequest, DnsCheckResponse> responseMap;

        public DomainCheckAction(
                final List<DnsCheckRequest> dnsCheckRequests,
                final int start,
                final int length,
                final Map<DnsCheckRequest, DnsCheckResponse> responseMap) {

            this.dnsCheckRequests = dnsCheckRequests;
            this.mStart = start;
            this.mLength = length;
            this.responseMap = responseMap;
        }

        @Override
        protected void compute() {
            if (mLength < THRESHOLD) {
                computeDirectly();
                return;
            }
            int split = mLength / 2;
            invokeAll(new DomainCheckAction(dnsCheckRequests, mStart, split, responseMap),
                    new DomainCheckAction(dnsCheckRequests, mStart + split, mLength - split, responseMap));
        }

        private void computeDirectly() {

            LOGGER.info("computeDirectly called with start {} and length {}", mStart, mStart + mLength);
            for (int index = mStart; index < mStart + mLength; index++) {

                final DnsCheckRequest dnsCheckRequest = dnsCheckRequests.get(index);

                final String id = makeRequest(dnsCheckRequest);
                LOGGER.info("Started domain test for {} with id: {}", dnsCheckRequest.getDomain(), id);

                testProgressUntilComplete(id);

                LOGGER.info("computeDirectly detected a Zonemaster result \\o/");

                final GetTestResultsResponse testResults = getResults(id);
                final List<Message> errorMessages = getErrorsFromResults(testResults);

                LOGGER.debug("computeDirectly found {} error messages for checkInstanceId: {}", errorMessages.size(), id);
                final DnsCheckResponse dnsCheckResponse = new DnsCheckResponse(errorMessages);

                // Get the result and store message
                responseMap.put(dnsCheckRequest, dnsCheckResponse);
            }
        }

        /**
         * Call start_domain_test API method.
         * @return check instance id
         */
        private String makeRequest(final DnsCheckRequest dnsCheckRequest) {
            final StartDomainTestRequest request = new StartDomainTestRequest(dnsCheckRequest);

            LOGGER.info("makeRequest request : {}", request.toString());

            final StartDomainTestResponse response = zonemasterRestClient
                .sendRequest(request)
                .readEntity(StartDomainTestResponse.class);

            if (response.getError() != null) {
                LOGGER.warn("makeRequest error: {}", response.getError().toString());
                throw new IllegalArgumentException(response.getError().getMessage());
            }

            LOGGER.info("makeRequest result ok : {}", response.getResult());
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
            LOGGER.info("testProgress request : {}", id);

            final TestProgressResponse response = zonemasterRestClient
                .sendRequest(new TestProgressRequest(id))
                .readEntity(TestProgressResponse.class);

            if (response.getError() != null) {
                LOGGER.warn("testProgress error: {}", response.getError().toString());
                throw new IllegalArgumentException(response.getError().getMessage());
            }

            LOGGER.info("testProgress ok : {}%", response.getResult());
            return response.getResult();
        }

        /**
         * Call get_test_results API method.
         * @param id
         * @return API response
         */
        private GetTestResultsResponse getResults(final String id) {
            LOGGER.info("getResults request : {}", id);

            final GetTestResultsResponse response = zonemasterRestClient
                .sendRequest(new GetTestResultsRequest(id))
                .readEntity(GetTestResultsResponse.class);

            LOGGER.info("getResults response is : {}", response.toString());
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
