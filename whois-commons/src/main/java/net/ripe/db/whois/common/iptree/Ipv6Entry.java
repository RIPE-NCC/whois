package net.ripe.db.whois.common.iptree;

import net.ripe.db.whois.common.domain.Ipv6Resource;

public class Ipv6Entry extends IpEntry<Ipv6Resource> {
    public Ipv6Entry(final Ipv6Resource key, final int objectId) {
        super(key, objectId);
    }
}
