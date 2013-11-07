package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class HierarchyLookupIpv4 extends HierarchyLookup<Ipv4Resource, Ipv4Entry> {

    @Autowired
    public HierarchyLookupIpv4(final Ipv4Tree ipv4Tree, final RpslObjectDao rpslObjectDao) {
        super(ipv4Tree, rpslObjectDao);
    }

    @Override
    public ObjectType getSupportedType() {
        return ObjectType.INETNUM;
    }

    @Override
    public Ipv4Resource createResource(final String key) {
        return Ipv4Resource.parse(key);
    }
}
