package net.ripe.db.whois.update.dns;


import com.google.common.collect.Maps;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import net.ripe.db.whois.update.dns.zonemaster.DomainCheckAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

@DeployedProfile
//@Primary
@Component
public class ZonemasterDnsGateway implements DnsGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZonemasterDnsGateway.class);

    @Override
    public Map<DnsCheckRequest, DnsCheckResponse> performDnsChecks(final Set<DnsCheckRequest> dnsCheckRequests) {
        final Map<DnsCheckRequest, DnsCheckResponse> dnsResults = Maps.newHashMap();
        DomainCheckAction domainCheckAction = new DomainCheckAction(dnsCheckRequests.toArray(new DnsCheckRequest[0]), 0, dnsCheckRequests.size(), dnsResults);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(domainCheckAction);
        return dnsResults;
    }
}
