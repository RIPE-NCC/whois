package net.ripe.db.whois.internal.api.acl;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

final class AclServiceHelper {

    private AclServiceHelper() {
        // do not instantiate helper
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

    public static String decode(final String value) {
        try {
            return URLDecoder.decode(value, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unknown character encoding");
        }
    }
}
