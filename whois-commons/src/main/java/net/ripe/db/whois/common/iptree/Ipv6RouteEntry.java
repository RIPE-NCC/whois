package net.ripe.db.whois.common.iptree;

import net.ripe.db.whois.common.ip.Ipv6Resource;

import java.util.regex.Matcher;

public class Ipv6RouteEntry extends RouteEntry<Ipv6Resource> {
    public Ipv6RouteEntry(final Ipv6Resource key, final int objectId, final String origin) {
        super(key, objectId, origin);
    }

    public static Ipv6RouteEntry parse(final String pkey, final int objectId) {
        final Matcher pkeyMatcher = parsePkey(pkey);

        final Ipv6Resource prefix = Ipv6Resource.parse(pkeyMatcher.group(1));
        final String origin = pkeyMatcher.group(2).toUpperCase();

        return new Ipv6RouteEntry(prefix, objectId, origin);
    }
}
