package net.ripe.db.whois.update.dns;


import com.google.common.collect.Sets;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ZonemasterDnsChecker extends DnsCheckerSupport {

    @Override
    public void checkAll(UpdateRequest updateRequest, UpdateContext updateContext) {
        Set<DnsCheckRequest> dnsCheckRequestSet = Sets.newLinkedHashSet();
        for (Update update : updateRequest.getUpdates()) {
            if (isDnsCheckRequired(update)) {
                dnsCheckRequestSet.add(createDnsCheckRequest(update));
            }
        }

        if (dnsCheckRequestSet.isEmpty()) {
            return;
        }

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ZonemasterDnsChecker.class);

}
