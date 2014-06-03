package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.common.ClockDateTimeProvider;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RndAccessControlListManager extends AccessControlListManager {
    @Autowired
    public RndAccessControlListManager(final IpRanges ipRanges) {
        super(new ClockDateTimeProvider(), null, null, null, ipRanges);
    }
}
