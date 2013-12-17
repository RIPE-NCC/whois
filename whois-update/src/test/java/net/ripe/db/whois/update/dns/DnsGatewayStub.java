package net.ripe.db.whois.update.dns;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.profiles.TestingProfile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@TestingProfile
@Component
public class DnsGatewayStub implements DnsGateway, Stub {
    private final Set<DnsCheckRequest> dnsCheckRequests = Sets.newLinkedHashSet();
    private final Set<CIString> checkedUpdates = Sets.newLinkedHashSet();
    private final Map<CIString, List<Message>> responses = Maps.newHashMap();

    @Override
    public void reset() {
        dnsCheckRequests.clear();
        checkedUpdates.clear();
        responses.clear();
    }

    public void addResponse(final CIString domain, final Message... messages) {
        final List<Message> previous = responses.put(domain, Lists.newArrayList(messages));
        if (previous != null) {
            throw new IllegalStateException(String.format("Existing response for domain %s, check the test", domain));
        }
    }

    @Override
    public Map<DnsCheckRequest, DnsCheckResponse> performDnsChecks(final Set<DnsCheckRequest> dnsCheckRequests) {
        Map<DnsCheckRequest, DnsCheckResponse> dnsResults = Maps.newHashMap();
        for (DnsCheckRequest dnsCheckRequest : dnsCheckRequests) {
            this.dnsCheckRequests.add(dnsCheckRequest);

            final CIString domain = ciString(dnsCheckRequest.getDomain());
            checkedUpdates.add(domain);

            List<Message> messages = responses.get(domain);
            if (messages == null) {
                messages = Collections.emptyList();
            }

            dnsResults.put(dnsCheckRequest, new DnsCheckResponse(messages));
        }

        return dnsResults;
    }

    public Set<CIString> getCheckedUpdates() {
        return checkedUpdates;
    }
}
