package net.ripe.db.whois.common.ip;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;

import java.io.Serial;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;

public abstract sealed class IpInterval<K extends Interval<K>> implements Interval<K>, Serializable permits Ipv4Resource, Ipv6Resource {

    @Serial
    private static final long serialVersionUID = 2309955325901882655L;

    public static String removeTrailingDot(final String address) {
        if (address.endsWith(".")) {
            return address.substring(0, address.length() - 1);
        }

        return address;
    }

    public static String addTrailingDot(final String address){
        if (!address.endsWith(".")){
            return address.concat(".");
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

    public static IpInterval<?> parseReverseDomain(final String reverse) {
        final String result = removeTrailingDot(reverse).toLowerCase();

        if (result.endsWith(Ipv4Resource.IPV4_REVERSE_DOMAIN)) {
            return Ipv4Resource.parseReverseDomain(result);
        }

        return Ipv6Resource.parseReverseDomain(result);
    }

    public static IpInterval<?> asIpInterval(final InetAddress address) {
        if (address instanceof Inet4Address) {
            return new Ipv4Resource(address);
        }

        return Ipv6Resource.parse(address);
    }

    public static boolean isIANADefaultBlock(final CIString key) {
        return key.equals("::/0") || key.equals("0.0.0.0 - 255.255.255.255");
    }

    public abstract InetAddress beginAsInetAddress();

    public abstract InetAddress endAsInetAddress();

    public abstract int getPrefixLength();
}
