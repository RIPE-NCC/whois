package net.ripe.db.whois.update.dns.zonemaster;

import net.ripe.db.whois.update.dns.DnsCheckRequest;
import net.ripe.db.whois.update.dns.DnsCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.RecursiveAction;

public class DomainCheckAction extends RecursiveAction {

    private static final int sThreshold = 2;

    public DomainCheckAction(final DnsCheckRequest[] dnsCheckRequests, final int start, final int length, final Map<DnsCheckRequest, DnsCheckResponse> responseMap) {
        this.dnsCheckRequests = dnsCheckRequests;
        this.mStart = start;
        this.mLength = length;
        this.responseMap = responseMap;
    }

    @Override
    protected void compute() {
        if (mLength < sThreshold) {
            computeDirectly();
            return;
        }
        int split = mLength / 2;
        invokeAll(new DomainCheckAction(dnsCheckRequests, mStart, split, responseMap), new DomainCheckAction(dnsCheckRequests, mStart + split, mLength - split, responseMap));
    }

    private void computeDirectly() {

        for (int index = mStart; index < mStart + mLength; index++) {

            DnsCheckRequest dnsCheckRequest = dnsCheckRequests[index];
            DnsCheckResponse dnsCheckResponse = null;

            // Fire request
            StartDomainTestRequest req = new StartDomainTestRequest(dnsCheckRequest);
            String checkInstanceId = req.execute().readEntity(StartDomainTestResponse.class).getResult();

            String percentageComplete;
            try {
                do {
                    Thread.sleep(1_000);
                    // Poll 'test' until result is 100%
                    TestProgressRequest tpRequest = new TestProgressRequest(checkInstanceId);
                    percentageComplete = tpRequest.execute().readEntity(StartDomainTestResponse.class).getResult();
                } while (!"100".equals(percentageComplete));

                GetTestResultsRequest gtrRequest = new GetTestResultsRequest(checkInstanceId);

                GetTestResultsResponse gtrResponse = gtrRequest.execute().readEntity(GetTestResultsResponse.class);

                // Get the result and store message
                responseMap.put(dnsCheckRequest, dnsCheckResponse);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int mStart;
    private int mLength;
    private DnsCheckRequest[] dnsCheckRequests;
    private Map<DnsCheckRequest, DnsCheckResponse> responseMap;

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainCheckAction.class);

}
