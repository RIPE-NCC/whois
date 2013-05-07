package net.ripe.db.whois.query.dao;

import net.ripe.db.whois.common.iptree.Ipv6Entry;

import java.util.List;

public interface Inet6numDao {
    List<Ipv6Entry> findByNetname(String netname);
}
