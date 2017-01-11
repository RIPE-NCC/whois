package net.ripe.db.whois.update.dns.zonemaster;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import net.ripe.db.whois.update.dns.DnsCheckRequest;
import net.ripe.db.whois.update.dns.DnsCheckResponse;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
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


            LOGGER.debug("StartDomainTestRequest.asJson(): " + req.asJson());


            // Poll 'test' until result is 100%

            // Get the result and store message
            responseMap.put(dnsCheckRequest, response);
        }

    }

    private static Client createClient() {
        final JacksonJaxbJsonProvider jsonProvider = new JacksonJaxbJsonProvider();
        jsonProvider.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        jsonProvider.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .register(jsonProvider)
                .build();
    }

    private int mStart;
    private int mLength;
    private DnsCheckRequest[] dnsCheckRequests;
    private Map<DnsCheckRequest, DnsCheckResponse> responseMap;

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainCheckAction.class);

}
