package net.ripe.db.whois.api.acl;

import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;

final class AclServiceHelper {
    private AclServiceHelper() {
    }

    public static IpInterval<?> getNormalizedPrefix(final String prefix) {
        final IpInterval<?> ipInterval = IpInterval.parse(prefix);

        if (ipInterval instanceof Ipv6Resource && ipInterval.getPrefixLength() != 64) {
            throw new IllegalArgumentException("IPv6 must be a /64 prefix range");
        }

        if (ipInterval instanceof Ipv4Resource && ipInterval.getPrefixLength() != 32) {
            throw new IllegalArgumentException("IPv4 must be a single address");
        }

        return ipInterval;
    }
}
