package net.ripe.db.whois.common.iptree;

import net.ripe.db.whois.common.ip.Ipv4Resource;

import java.util.regex.Matcher;

public class Ipv4RouteEntry extends RouteEntry<Ipv4Resource> {
    public Ipv4RouteEntry(final Ipv4Resource key, final int objectId, final String origin) {
        super(key, objectId, origin);
    }

    public static Ipv4RouteEntry parse(final String pkey, final int objectId) {
        final Matcher pkeyMatcher = parsePkey(pkey);

        final Ipv4Resource prefix = Ipv4Resource.parse(pkeyMatcher.group(1));
        final String origin = pkeyMatcher.group(2).toUpperCase();

        return new Ipv4RouteEntry(prefix, objectId, origin);
    }
}
