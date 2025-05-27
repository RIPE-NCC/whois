package net.ripe.db.whois.update.dns.zonemaster;


import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.common.ApplicationVersion;
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
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.ProcessingException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.ripe.db.whois.common.Messages.Type.ERROR;

@DeployedProfile
@Component
public class ZonemasterDnsGateway implements DnsGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZonemasterDnsGateway.class);

    private static final int TEST_PROGRESS_SLEEP_SECONDS = 5;
    private static final int TEST_PROGRESS_MAXIMUM_RETRIES = ((60 * 10) / TEST_PROGRESS_SLEEP_SECONDS);

    private static final String PERCENTAGE_COMPLETE = "100";

    private static final ImmutableList<String> ERROR_LEVELS = ImmutableList.of("CRITICAL", "ERROR");

    private final ZonemasterRestClient zonemasterRestClient;
    private final ApplicationVersion applicationVersion;

    public ZonemasterDnsGateway(final ZonemasterRestClient zonemasterRestClient, final ApplicationVersion applicationVersion) {
        this.zonemasterRestClient = zonemasterRestClient;
        this.applicationVersion = applicationVersion;
    }

    @Override
    public Map<DnsCheckRequest, DnsCheckResponse> performDnsChecks(final Set<DnsCheckRequest> dnsCheckRequests) {
        return dnsCheckRequests
            .parallelStream()
            .collect(Collectors.toMap(
                dnsCheckRequest -> dnsCheckRequest,
                new ZonemasterFunction()));
    }

    private class ZonemasterFunction implements Function<DnsCheckRequest, DnsCheckResponse> {

        @Override
        public DnsCheckResponse apply(final DnsCheckRequest dnsCheckRequest) {
            try {
                final String id = makeRequest(dnsCheckRequest);
                testProgressUntilComplete(id);
                final GetTestResultsResponse testResults = getResults(id);
                return new DnsCheckResponse(getErrorsFromResults(testResults));
            } catch (ZonemasterTimeoutException e) {
                LOGGER.error("Timeout performing DNS check using zonemaster");
                return new DnsCheckResponse(UpdateMessages.dnsCheckTimeout());
            } catch (ZonemasterException e) {
                LOGGER.error("Error from Zonemaster: {}", e.getMessage());
                return new DnsCheckResponse(UpdateMessages.dnsCheckError());
            } catch (ProcessingException e) {
                LOGGER.error("Error making request to Zonemaster, due to {}: {}", e.getClass().getName(), e.getMessage());
                return new DnsCheckResponse(UpdateMessages.dnsCheckError());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return new DnsCheckResponse(UpdateMessages.dnsCheckError());
            }
        }

        /**
         * Call start_domain_test API method.
         * @return check instance id
         */
        private String makeRequest(final DnsCheckRequest dnsCheckRequest) {
            final StartDomainTestRequest request = new StartDomainTestRequest(dnsCheckRequest);
            request.setClientVersion(applicationVersion.getVersion());

            final StartDomainTestResponse response = zonemasterRestClient
                .sendRequest(request)
                .readEntity(StartDomainTestResponse.class);

            if (response.getError() != null) {
                throw new ZonemasterException(response.getError().getMessage());
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
            throw new ZonemasterTimeoutException("Request timeout for id " + id);
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
                throw new ZonemasterException(response.getError().getMessage());
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
                throw new ZonemasterException(response.getError().getMessage());
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

    private class ZonemasterTimeoutException extends IllegalStateException {
        public ZonemasterTimeoutException(final String s) {
            super(s);
        }
    }

    private class ZonemasterException extends IllegalStateException {
        public ZonemasterException(final String s) {
            super(s);
        }
    }
}
