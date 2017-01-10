package net.ripe.db.whois.update.dns.zonemaster;

import net.ripe.db.whois.update.dns.DnsCheckRequest;
import net.ripe.db.whois.update.dns.DnsCheckResponse;

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
            DnsCheckResponse response = null;

            // Fire request
            StartDomainTestRequest req = new StartDomainTestRequest(dnsCheckRequest);

            // Poll 'test' until result is 100%

            // Get the result and store message
            responseMap.put(dnsCheckRequest, response);
        }

    }

    private int mStart;
    private int mLength;
    private DnsCheckRequest[] dnsCheckRequests;
    private Map<DnsCheckRequest, DnsCheckResponse> responseMap;
}
