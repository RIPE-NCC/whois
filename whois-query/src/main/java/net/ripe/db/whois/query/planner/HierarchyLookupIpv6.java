package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class HierarchyLookupIpv6 extends HierarchyLookup<Ipv6Resource, Ipv6Entry> {

    @Autowired
    public HierarchyLookupIpv6(final Ipv6Tree ipv6Tree, final RpslObjectDao rpslObjectDao) {
        super(ipv6Tree, rpslObjectDao);
    }

    @Override
    public ObjectType getSupportedType() {
        return ObjectType.INET6NUM;
    }

    @Override
    public Ipv6Resource createResource(final String key) {
        return Ipv6Resource.parse(key);
    }
}
