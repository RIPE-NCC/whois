package net.ripe.db.whois.common.iptree;

import net.ripe.db.whois.common.ip.Ipv4Resource;

public class Ipv4Entry extends IpEntry<Ipv4Resource> {
    public Ipv4Entry(final Ipv4Resource key, final int objectId) {
        super(key, objectId);
    }
}
