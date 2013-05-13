package net.ripe.db.whois.query.dao;

import net.ripe.db.whois.common.iptree.Ipv4Entry;

import java.util.List;

public interface InetnumDao {
    List<Ipv4Entry> findByNetname(String netname);
}
