package net.ripe.db.whois.update.dns;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class DnsCheckerImpl extends DnsCheckerSupport {

    private final DnsGateway dnsGateway;

    @Autowired
    public DnsCheckerImpl(final DnsGateway dnsGateway) {
        this.dnsGateway = dnsGateway;
    }

    public void checkAll(final UpdateRequest updateRequest, final UpdateContext updateContext) {
        Set<DnsCheckRequest> dnsCheckRequestSet = Sets.newLinkedHashSet();

        for (Update update : updateRequest.getUpdates()) {
            if (isDnsCheckRequired(update)) {
                dnsCheckRequestSet.add(createDnsCheckRequest(update));
            }
        }

        if (dnsCheckRequestSet.isEmpty()) {
            return;
        }

        final Map<DnsCheckRequest, DnsCheckResponse> dnsCheckResponseMap = dnsGateway.performDnsChecks(dnsCheckRequestSet);

        for (Map.Entry<DnsCheckRequest, DnsCheckResponse> entry : dnsCheckResponseMap.entrySet()) {
            final DnsCheckRequest dnsCheckRequest = entry.getKey();
            final DnsCheckResponse dnsCheckResponse = entry.getValue();

            updateContext.addDnsCheckResponse(dnsCheckRequest, dnsCheckResponse);

            for (final Message message : dnsCheckResponse.getMessages()) {
                updateContext.addMessage(dnsCheckRequest.getUpdate(), message);
            }
        }
    }

}
