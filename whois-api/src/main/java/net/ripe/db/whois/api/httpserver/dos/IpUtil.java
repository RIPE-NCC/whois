package net.ripe.db.whois.api.httpserver.dos;

import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;

import java.util.Set;

public class IpUtil {

    public static boolean isExistingIp(final String candidate, final Set<Ipv4Resource> ipv4Resources, final Set<Ipv6Resource> ipv6Resources){
        final IpInterval<?> parsed = IpInterval.parse(candidate);
        return switch (parsed) {
            case Ipv4Resource ipv4Resource -> {
                for (Ipv4Resource entry : ipv4Resources) {
                    if (entry.contains(ipv4Resource)) {
                        yield true;
                    }
                }
                yield false;
            }
            case Ipv6Resource ipv6Resource -> {
                for (Ipv6Resource entry : ipv6Resources) {
                    if (entry.contains(ipv6Resource)) {
                        yield true;
                    }
                }
                yield false;
            }
            default -> false;
        };
    }
}
