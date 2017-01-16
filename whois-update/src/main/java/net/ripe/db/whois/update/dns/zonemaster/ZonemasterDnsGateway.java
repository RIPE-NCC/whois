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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.ripe.db.whois.common.Messages.Type.ERROR;

@DeployedProfile
//@Primary
@Component
public class ZonemasterDnsGateway implements DnsGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZonemasterDnsGateway.class);

    private static String PERCENTAGE_COMPLETE = "100";
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

                final String checkInstanceId = makeRequest(dnsCheckRequest);
                LOGGER.debug("Started domain test for {} with checkInstanceId: {}", dnsCheckRequest.getDomain(), checkInstanceId);

                // TODO: [ES] may run forever
                do {
                    LOGGER.debug("computeDirectly sleeping for one second");
                    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                } while (!PERCENTAGE_COMPLETE.equals(testProgress(checkInstanceId)));

                LOGGER.debug("computeDirectly detected a Zonemaster result \\o/");

                final GetTestResultsResponse testResults = getResults(checkInstanceId);
                final List<Message> errorMessages = getErrorsFromResults(testResults);

                LOGGER.debug("computeDirectly found {} error messages for checkInstanceId: {}", errorMessages.size(), checkInstanceId);
                final DnsCheckResponse dnsCheckResponse = new DnsCheckResponse(errorMessages);

                // Get the result and store message
                responseMap.put(dnsCheckRequest, dnsCheckResponse);
            }
        }

        /**
         * @return check instance id
         */
        private String makeRequest(final DnsCheckRequest dnsCheckRequest) {
            return zonemasterRestClient
                .sendRequest(new StartDomainTestRequest(dnsCheckRequest))
                .readEntity(StartDomainTestResponse.class)
                    .getResult();
        }

        /**
         * @return percentage complete
         */
        private String testProgress(final String id) {
            return zonemasterRestClient
                .sendRequest(new TestProgressRequest(id))
                .readEntity(StartDomainTestResponse.class).getResult();
        }

        private GetTestResultsResponse getResults(final String id) {
            return zonemasterRestClient
                .sendRequest(new GetTestResultsRequest(id))
                .readEntity(GetTestResultsResponse.class);
        }

        private List<Message> getErrorsFromResults(final GetTestResultsResponse testResults) {
            return Arrays.stream(testResults.getResult().getResults())
                    .filter(m->ERROR_LEVELS.contains(m.getLevel()))
                    .map(m->new Message(ERROR, m.getMessage()))
                    .collect(Collectors.toList());
        }
    }

}
