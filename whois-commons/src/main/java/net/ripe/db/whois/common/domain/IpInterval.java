package net.ripe.db.whois.common.domain;

import net.ripe.db.whois.common.etree.Interval;
import net.ripe.db.whois.common.rpsl.AttributeType;

import java.net.Inet4Address;
import java.net.InetAddress;

public abstract class IpInterval<K extends Interval<K>> implements Interval<K> {
    static String removeTrailingDot(final String address) {
        if (address.endsWith(".")) {
            return address.substring(0, address.length() - 1);
        }

        return address;
    }

    public abstract AttributeType getAttributeType();

    public static IpInterval<?> parse(final CIString prefix) {
        return parse(prefix.toString());
    }

    public static IpInterval<?> parse(final String prefix) {
        if (prefix.indexOf(':') == -1) {
            return Ipv4Resource.parse(prefix);
        }

        return Ipv6Resource.parse(prefix);
    }

    public static IpInterval<?> asIpInterval(InetAddress address) {
        if (address instanceof Inet4Address) {
            return new Ipv4Resource(address);
        }

        return Ipv6Resource.parse(address);
    }

    public abstract InetAddress beginAsInetAddress();

    public abstract int getPrefixLength();
}
