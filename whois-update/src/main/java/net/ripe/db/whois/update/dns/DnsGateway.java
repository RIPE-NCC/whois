package net.ripe.db.whois.update.dns;

import java.util.Map;
import java.util.Set;

public interface DnsGateway {
    DnsCheckResponse performDnsCheck(DnsCheckRequest dnsCheckRequest);

    Map<DnsCheckRequest, DnsCheckResponse> performDnsChecks(Set<DnsCheckRequest> dnsCheckRequests);
}
